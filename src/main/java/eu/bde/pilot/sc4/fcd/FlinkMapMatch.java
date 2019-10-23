package eu.bde.pilot.sc4.fcd;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09;
import org.apache.flink.util.Collector;
import org.rosuda.REngine.REXPMismatchException;

import eu.bde.pilot.sc4.utils.Geohash;
import eu.bde.pilot.sc4.utils.MapMatch;


/**
 * This class provides the execution plan for the map matching of taxis 
 * to the road segments.
 * Flink subtasks list:
 * 1) source(), reads the records from the file system
 * 2) mapGeohash(), compute the geohash from the coordinates pair
 * 3) keyBy(geohash).window(5 min.).apply(mapMatch()), map-match 
 *    the coordinates' pairs within a time window
 * 4) KeyBy(road_segment).apply(average speed, flow)
 * 5) sink(), store in Kafka topics (one for each road segment)
 *  
 * @author Luigi Selmi
 *
 */
public class FlinkMapMatch {
  
	private static final String KAFKA_BROKER = "localhost:9092";
	public static final String KAFKA_TOPIC = "historic-fcd";
	private static final String R_SERVER = "localhost";
	private static final int R_PORT = 6311;    

	public static void main(String[] args) throws Exception {

		ParameterTool params = ParameterTool.fromArgs(args);
		String input = params.getRequired("input"); //path to the data file
		String topic = params.getRequired("topic");

		final int maxEventDelay = 60;       // events are out of order by max 60 seconds
		final int servingSpeedFactor = 600; // events of 10 minute are served in 1 second

		// set up streaming execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		// Start the data generator
		DataStream<FcdTaxiEvent> taxiEventStream = env.addSource(new FcdTaxiSource(input, maxEventDelay, servingSpeedFactor));
		
		// 1st subtask compute the geohash of the coordinate pairs
		DataStream<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> streamGeohashTuples = taxiEventStream.map(new GeohashFunction());
		
	  // 2nd subtask, map-match coordinates (longitude, latitude) pairs to road segments
		DataStream<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> roadSegmentStream = streamGeohashTuples
		.keyBy(8) // keyby geohash
		.timeWindow(Time.minutes(2))
		.apply(new MapMatcher());
		
	  // 3rd subtask, keyBy road segment and compute number of vehicles (flow) and average speed in the time window 
    DataStream<Tuple4<String,String,Double,Integer>> roadSegmentSpeedAndFlowStream = roadSegmentStream
        .keyBy(8)
        .timeWindow(Time.minutes(2))
        .apply(new AverageSpeed());
		
		
    roadSegmentSpeedAndFlowStream.print();
		
    // Write the data to a Kafka topic (sink) using the avro binary format
    /*
    FlinkKafkaProducer09<FcdTaxiEvent> producer = new FlinkKafkaProducer09<FcdTaxiEvent>(
        KAFKA_BROKER,
        topic,
        new FcdTaxiSchema());
   */ 
    //roadSegmentSpeedAndFlowStream.addSink(producer);
    
		// run the pipeline
		env.execute("Historic FCD Taxi Data");
	}
	
	public static class GeohashFunction implements MapFunction<FcdTaxiEvent, Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> {
	  @Override
    public Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String> map(
      FcdTaxiEvent event)
      throws Exception {
	    
	    Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String> tp9 = new Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>();
	    // compute the geohash
	    String geohash = Geohash.encodeBase32(event.lat, event.lon, 20); // bits = 20 -> precision 4
	    
	    tp9.f0 = event.deviceId;
	    tp9.f1 = event.timestamp.toString();
	    tp9.f2 = event.lon;
	    tp9.f3 = event.lat;
	    tp9.f4 = event.altitude;
	    tp9.f5 = event.speed;
	    tp9.f6 = event.orientation;
	    tp9.f7 = event.transfer;
	    tp9.f8 = geohash;
	    
	    return tp9;
	    
	  }
	}
	
	/**
   * Match the coordinates' pair of a vehicle to a road segment
   */
  public static class MapMatcher implements WindowFunction<
    Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>, 
    Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>,
    Tuple,
    TimeWindow> 
  {
    
    @SuppressWarnings("unchecked")
    @Override
    public void apply(
        Tuple key,
        TimeWindow window,
        Iterable<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> in,
        Collector<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> out) throws MalformedURLException, IOException, REXPMismatchException
    {
        MapMatch matcher = new MapMatch(R_SERVER, R_PORT);
        // map match the coordinates pairs 
        out = matcher.mapMatch(in,out);
        
        
    }
    
  }
  
  public static class AverageSpeed implements WindowFunction<
    Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>,
    Tuple4<String,String,Double,Integer>,
    Tuple,
    TimeWindow> {
  
    @Override
    public void apply(
      Tuple key,
      TimeWindow window,
      Iterable<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> records,
      Collector<Tuple4<String, String, Double, Integer>> out) throws Exception {
      
      int recordsCounter = 0; // number of records (or number of vehicles)
      String roadSegmentId = "N/A"; // OpenStreetMap identifier of a road segment (between two junctions)
      String timestamp = "N/A";
      double speedAccumulator = 0.0;
      for (Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String> tuple9: records){
        double speed = tuple9.f5;
        recordsCounter++;
        speedAccumulator += speed;
        roadSegmentId = tuple9.f8;
        timestamp = tuple9.f1;
      }
    
      double averageSpeed = speedAccumulator / recordsCounter; 
      out.collect(new Tuple4<>(roadSegmentId,timestamp, averageSpeed,recordsCounter));
    }
  }
}
