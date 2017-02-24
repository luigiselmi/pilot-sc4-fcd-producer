package eu.bde.pilot.sc4.fcd;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;


public class FlinkFcdProducer {

  private static final String KAFKA_BROKER = "localhost:9092";
  
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
    
 // 2) Write the data to a Kafka topic (sink) using the avro binary format
    FlinkKafkaProducer010<FcdTaxiEvent> producer = new FlinkKafkaProducer010<FcdTaxiEvent>(
        KAFKA_BROKER,
        topic,
        new FcdTaxiSchema());
    
    taxiEventStream.addSink(producer);
   
    
    // run the pipeline
    env.execute("Ingestion of Historical FCD Taxi Data");
  }

}
