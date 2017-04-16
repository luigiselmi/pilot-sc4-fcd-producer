package eu.bde.pilot.sc4.fcd;

import java.io.InputStream;
import java.util.Properties;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple1;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.util.Collector;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import eu.bde.pilot.sc4.utils.GeoUtils;


/**
 * This class provides the execution plan of a Flink job. It reads the records
 * from a Kafka topic, determines from which cells the message were originated
 * within a bounding box, split the data according to the cell number and 
 * counts the number of events in each cell (records in a time window)
 * Flink subtasks list:
 * 1) source(), reads the data from a Kafka topic
 * 2) mapToGrid(), computes the cell in which the records are originated
 * 3) keyBy(cell number).window(5 min.).apply(count_events), makes one partition 
 *    for each cell, computes the number of records in each cell within the time window 
 * 4) sink(), print the data
 *  
 * @author Luigi Selmi
 *
 */
public class FlinkFcdConsumer {
	
  private static String KAFKA_TOPIC_PARAM_NAME = "topic";
  private static String KAFKA_TOPIC_PARAM_VALUE = null;
  private static String TIME_WINDOW_PARAM_NAME = "window";
  private static int TIME_WINDOW_PARAM_VALUE = 0;
  private static final int MAX_EVENT_DELAY = 60; // events are at most 60 sec out-of-order.
  private static final Logger log = LoggerFactory.getLogger(FlinkFcdConsumer.class);

  public static void main(String[] args) throws Exception {
	  
	ParameterTool parameter = ParameterTool.fromArgs(args);
    
    if (parameter.getNumberOfParameters() < 2) {
      throw new IllegalArgumentException("The application needs two arguments, the name of the kafka topic from which it has to \n"
          + "fetch the data, and the size of the window, in seconds. \n");
    }
    
    KAFKA_TOPIC_PARAM_VALUE = parameter.get(KAFKA_TOPIC_PARAM_NAME);
    TIME_WINDOW_PARAM_VALUE = parameter.getInt(TIME_WINDOW_PARAM_NAME, TIME_WINDOW_PARAM_VALUE);
    
    Properties properties = null;
    
    try (InputStream props = Resources.getResource("consumer.props").openStream()) {
      properties = new Properties();
      properties.load(props);
      
    }
    
    // set up streaming execution environment
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    
    // set the time characteristic to include an event in a window (event time|ingestion time|processing time) 
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
    //env.getConfig().setAutoWatermarkInterval(1000);
    
    // create a Kafka consumer
	  FlinkKafkaConsumer010<FcdTaxiEvent> consumer = new FlinkKafkaConsumer010<FcdTaxiEvent>(
			KAFKA_TOPIC_PARAM_VALUE,
			new FcdTaxiSchema(),
			properties);
	
	  // assign a timestamp extractor to the consumer
	  consumer.assignTimestampsAndWatermarks(new FcdTaxiTSExtractor());
	
	  // create a FCD event data stream
	  DataStream<FcdTaxiEvent> events = env.addSource(consumer);
	
	  // Counts the events that happen in any cell within the bounding box
	  DataStream<Tuple3<Integer, Integer, String>> boxBoundedEvents = events
			// match each event within the bounding box to grid cell
			.map(new GridCellMatcher())
			// partition by cell
			.keyBy(0)
			// build time window
			.timeWindow(Time.minutes(TIME_WINDOW_PARAM_VALUE))
			.apply(new EventCounter());
	
    
	  boxBoundedEvents.print();
    
    env.execute("Read Historic Floating Cars Data from Kafka");
  
  
  }
  
  /**
   * Counts the number of events.
   */
  public static class EventCounter implements WindowFunction<
	  Tuple2<Integer, Boolean>,       // input type (cell id, is within bb)
	  Tuple3<Integer, Integer, String>, // output type (cell id, counts, window time)
	  Tuple,                          // key type
	  TimeWindow>                     // window type
	{
    private static transient DateTimeFormatter timeFormatter =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    
	  @SuppressWarnings("unchecked")
	  @Override
	  public void apply(
		  Tuple key,
		  TimeWindow window,
		  Iterable<Tuple2<Integer, Boolean>> gridCells,
		  Collector<Tuple3<Integer, Integer, String>> out) throws Exception {

		  int cellId = ((Tuple1<Integer>)key).f0;
		  String windowTime = timeFormatter.print(window.getEnd());
		
		  // counts all the records from the same cell within the bounding box
		  // or outside (cell id = 0)
		  int cnt = 0;
		  for(Tuple2<Integer, Boolean> c : gridCells) {
			  cnt += 1;
		  }

		  out.collect(new Tuple3<>(cellId, cnt, windowTime));
	  }
  }
  
  /**
	 * Maps taxi ride to grid cell and event type.
	 * Start records use departure location, end record use arrival location.
	 */
	public static class GridCellMatcher implements MapFunction<FcdTaxiEvent, Tuple2<Integer, Boolean>> {
		
		int [][] grid = GeoUtils.initGrid();
		@Override
		public Tuple2<Integer, Boolean> map(FcdTaxiEvent event) throws Exception {
			return new Tuple2<>(
					GeoUtils.mapToGridCell(event.lon, event.lat, grid),
					GeoUtils.isWithinBoundingBox(event.lon, event.lat)
			);
		}
	}
  
  /**
   * Assigns timestamps to FCD Taxi records.
   * Watermarks are a fixed time interval behind the max timestamp and are periodically emitted.
  */
  public static class FcdTaxiTSExtractor extends BoundedOutOfOrdernessTimestampExtractor<FcdTaxiEvent> {
	
	  public FcdTaxiTSExtractor() {
		  super(Time.seconds(MAX_EVENT_DELAY));
	  }
	
	  @Override
	  public long extractTimestamp(FcdTaxiEvent event) {
			return event.timestamp.getMillis();
	  }
  }
    

}
