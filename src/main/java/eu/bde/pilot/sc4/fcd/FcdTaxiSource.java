package eu.bde.pilot.sc4.fcd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FcdTaxiSource implements SourceFunction<FcdTaxiEvent>{
  private static final Logger log = LoggerFactory.getLogger(FcdTaxiSource.class);
	//private final int watermarkDelayMSecs = 1000;
	private int maxDelayMsecs = 1000;
	private long watermarkDelayMSecs = (maxDelayMsecs < 10000) ? 10000 : maxDelayMsecs;
	private final String dataFilePath;
	private final int servingSpeed;

	private transient BufferedReader reader;
	private transient InputStream inputStream = null;
	
	
	public FcdTaxiSource(String dataFilePath, int maxEventDelaySecs, int servingSpeedFactor) {
		this.dataFilePath = dataFilePath;
		this.maxDelayMsecs = maxEventDelaySecs * 1000;
		this.servingSpeed = servingSpeedFactor;
	}
	/**
	 * Reads txt file
	 */
	@Override
	public void run(SourceContext<FcdTaxiEvent> sourceContext) throws Exception {
	  
	  if(dataFilePath.endsWith(".gz"))
	  {
	    readGzipFile(sourceContext);
	  }
	  else {
	    readTextFile(sourceContext);
	  }

	}
	/**
	 * Reads text file
	 * @param sourceContext
	 * @throws Exception
	 */
	private void readTextFile(SourceContext<FcdTaxiEvent> sourceContext) throws Exception {
	  if(dataFilePath.startsWith("hdfs:")) {
      Configuration conf = new Configuration();
      FileSystem fs = FileSystem.get(URI.create(dataFilePath), conf);
      inputStream = fs.open(new Path(dataFilePath));
    }
    else {
      inputStream = new FileInputStream(dataFilePath);
    }
    
    reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    
    generateStream(sourceContext);

    this.reader.close();
    this.reader = null;
    this.inputStream.close();
    this.inputStream = null;
	}

	/**
	 * Unzip the gzipped files and creates a copy. 
	 */
  private void readGzipFile(SourceContext<FcdTaxiEvent> sourceContext) throws Exception {
    
    if(dataFilePath.startsWith("hdfs:")) {
      Path inputPath = new Path(dataFilePath);
      Configuration conf = new Configuration();
      FileSystem fs = FileSystem.get(URI.create(dataFilePath), conf);
      CompressionCodecFactory factory = new CompressionCodecFactory(conf);
      CompressionCodec codec = factory.getCodec(inputPath);
      if (codec == null) {
          System.err.println("No codec found for " + dataFilePath);
          System.exit(1);
      }
      String outputUri =
          CompressionCodecFactory.removeSuffix(dataFilePath, codec.getDefaultExtension());
      InputStream in = null;
      OutputStream out = null;
      try {
          in = codec.createInputStream(fs.open(inputPath));
          out = fs.create(new Path(outputUri));
          IOUtils.copyBytes(in, out, conf);
      } finally {
          IOUtils.closeStream(in);
          IOUtils.closeStream(out);
      }
      
      inputStream = fs.open(new Path(outputUri));
    }
    
    reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    
    generateStream(sourceContext);

    this.reader.close();
    this.reader = null;
    this.inputStream.close();
    this.inputStream = null;

  }
	
	private void generateStream(SourceContext<FcdTaxiEvent> sourceContext) throws Exception {
		long servingStartTime = Calendar.getInstance().getTimeInMillis();
		long dataStartTime = 0;
	    long nextWatermark = 0;
	    long nextWatermarkServingTime = 0;

	    // read the first ride event
	    if (reader.ready()) {
	      String line = reader.readLine();
	      if (line != null) {
	        FcdTaxiEvent event = FcdTaxiEvent.fromString(line);

	        // set time of first event
	        dataStartTime = event.timestamp.getMillis();
	        // initialize watermarks
	        nextWatermark = dataStartTime + watermarkDelayMSecs;
	        nextWatermarkServingTime = toServingTime(servingStartTime, dataStartTime, nextWatermark);
	        // emit first event
	        sourceContext.collectWithTimestamp(event, event.timestamp.getMillis());
	      }
	    }
	    else {
	      return;
	    }

	    // read all following events
	    while (reader.ready()) {
	      String line = reader.readLine();
	      if (line != null) {

	        // read event
	        FcdTaxiEvent event = FcdTaxiEvent.fromString(line);

	        long eventTime = event.timestamp.getMillis();
	        long now = Calendar.getInstance().getTimeInMillis();
	        long eventServingTime = toServingTime(servingStartTime, dataStartTime, eventTime);

	        // get time to wait until event and next watermark needs to be emitted
	        long eventWait = eventServingTime - now;
	        long watermarkWait = nextWatermarkServingTime - now;

	        if (eventWait < watermarkWait) {
	          // wait to emit next event
	          Thread.sleep((eventWait > 0) ? eventWait : 0);
	        }
	        else if (eventWait > watermarkWait) {
	        	// wait to emit watermark
	            Thread.sleep((watermarkWait > 0) ? watermarkWait : 0);
	            // emit watermark
	            sourceContext.emitWatermark(new Watermark(nextWatermark));
	            // schedule next watermark
	            nextWatermark = nextWatermark + watermarkDelayMSecs;
	            nextWatermarkServingTime = toServingTime(servingStartTime, dataStartTime, nextWatermark);
	            // wait to emit event
	            long remainWait = eventWait - watermarkWait;
	            Thread.sleep((remainWait > 0) ? remainWait : 0);
	        }
	        else if (eventWait == watermarkWait) {
	        	// wait to emit watermark
	            Thread.sleep( (watermarkWait > 0) ? watermarkWait : 0);
	            // emit watermark
	            sourceContext.emitWatermark(new Watermark(nextWatermark - 1));
	            // schedule next watermark
	            nextWatermark = nextWatermark + watermarkDelayMSecs;
	            nextWatermarkServingTime = toServingTime(servingStartTime, dataStartTime, nextWatermark);
	        }
	        // emit event
	        sourceContext.collectWithTimestamp(event, event.timestamp.getMillis());
	      }
	}
	}
	
	public long toServingTime(long servingStartTime, long dataStartTime, long eventTime) {
		long dataDiff = eventTime - dataStartTime;
		return servingStartTime + (dataDiff / this.servingSpeed);
	}
	
	@Override
	public void cancel() {
		try {
			if (this.reader != null) {
				this.reader.close();
			}
			if (this.inputStream != null) {
				this.inputStream.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeException("Could not cancel SourceFunction", ioe);
		} finally {
			this.reader = null;
			this.inputStream = null;
		}
	}

}
