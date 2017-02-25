package eu.bde.pilot.sc4.fcd;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;

/**
 * This class provides the Flink execution plan for the ingestion of historical 
 * floating car data from the file system.
 * Flink subtasks list:
 * 
 * 1) source(), reads the records from the file system
 * 2) sink(), sends the records into a Kafka topic
 *  
 * @author Luigi Selmi
 *
 */
public class FlinkFcdProducer {

  private static final String KAFKA_BROKER = "localhost:9092";
  
  public static void main(String[] args) throws Exception {

    ParameterTool params = ParameterTool.fromArgs(args);
    if (params.getNumberOfParameters() < 2) {
      throw new IllegalArgumentException("The application needs two arguments: the path of the file from which it has to \n"
          + "fetch the data, and the Kafka topic to which it has to send the records. \n");
    }
    String path = params.getRequired("path"); //path to the data file
    String topic = params.getRequired("topic");

    final int maxEventDelay = 60;       // events are out of order by max 60 seconds
    final int servingSpeedFactor = 600; // events of 10 minute are served in 1 second

    // set up streaming execution environment
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

    // 1) source(), start the data generator
    DataStream<FcdTaxiEvent> taxiEventStream = env.addSource(new FcdTaxiSource(path, maxEventDelay, servingSpeedFactor));
    
    // 2) sink(), write the data to a Kafka topic (sink) using the avro binary format
    FlinkKafkaProducer010<FcdTaxiEvent> producer = new FlinkKafkaProducer010<FcdTaxiEvent>(
        KAFKA_BROKER,
        topic,
        new FcdTaxiSchema());
    
    taxiEventStream.addSink(producer);
   
    
    // run the pipeline
    env.execute("Ingestion of Historical FCD Taxi Data");
  }

}
