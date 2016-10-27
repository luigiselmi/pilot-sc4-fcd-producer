package eu.bde.sc4pilot.fcd;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09;

import com.esotericsoftware.minlog.Log;

public class FcdProducer {
  
	private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
	public static final String KAFKA_TOPIC = "historic-fcd";

	public static void main(String[] args) throws Exception {

		ParameterTool params = ParameterTool.fromArgs(args);
		String input = params.getRequired("input");

		final int maxEventDelay = 60;       // events are out of order by max 60 seconds
		final int servingSpeedFactor = 600; // events of 10 minute are served in 1 second

		// set up streaming execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		// start the data generator
		DataStream<FcdTaxiEvent> taxiEventStream = env.addSource(new FcdTaxiSource(input, maxEventDelay, servingSpeedFactor));
		
		
		// filter out taxies with speed == 0
		DataStream<FcdTaxiEvent> filteredTaxiEvent = taxiEventStream
				.filter(new FcdFilter());
		
		// write the filtered data to a Kafka sink
		filteredTaxiEvent.addSink(new FlinkKafkaProducer09<>(
				LOCAL_KAFKA_BROKER,
				KAFKA_TOPIC,
				new FcdTaxiSchema()));
 
		// run the pipeline
		env.execute("Write Historic FCD Taxi from FS to Kafka");
	}
	
	public static class FcdFilter implements FilterFunction<FcdTaxiEvent> {

		@Override
		public boolean filter(FcdTaxiEvent event) throws Exception {
			System.out.println("Device Id: " + event.deviceId);
			return event.speed > 0.0;
			
		}
    }
}
