package eu.bde.sc4pilot.fcd;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09;



public class FcdProducer {
  
	private static final String LOCAL_KAFKA_BROKER = "localhost:9092";
	public static final String CLEANSED_RIDES_TOPIC = "historic-taxi";

	public static void main(String[] args) throws Exception {

		ParameterTool params = ParameterTool.fromArgs(args);
		String input = params.getRequired("input");

		final int maxEventDelay = 60;       // events are out of order by max 60 seconds
		final int servingSpeedFactor = 600; // events of 10 minute are served in 1 second

		// set up streaming execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		// start the data generator
		DataStream<FcdTaxiEvent> taxiEvent = env.addSource(new FcdTaxiSource(input, servingSpeedFactor));

		

		// write the filtered data to a Kafka sink
		taxiEvent.addSink(new FlinkKafkaProducer09<>(
				LOCAL_KAFKA_BROKER,
				CLEANSED_RIDES_TOPIC,
				new FcdTaxiSchema()));

		// run the cleansing pipeline
		env.execute("Historic FCD Taxi from FS to Kafka");
	}
}
