package eu.bde.sc4pilot.fcd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.zip.ZipInputStream;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;


public class FcdTaxiSource implements SourceFunction<FcdTaxiEvent>{
	
	//private final int watermarkDelayMSecs = 1000;
	private int maxDelayMsecs = 1000;
	private long watermarkDelayMSecs = (maxDelayMsecs < 10000) ? 10000 : maxDelayMsecs;
	private final String dataFilePath;
	private final int servingSpeed;

	private transient BufferedReader reader;
	private transient InputStream zipStream;
	
	
	public FcdTaxiSource(String dataFilePath, int maxEventDelaySecs, int servingSpeedFactor) {
		this.dataFilePath = dataFilePath;
		this.maxDelayMsecs = maxEventDelaySecs * 1000;
		this.servingSpeed = servingSpeedFactor;
	}
	
	@Override
	public void run(SourceContext<FcdTaxiEvent> sourceContext) throws Exception {

		zipStream = new ZipInputStream(new FileInputStream(dataFilePath));
		reader = new BufferedReader(new InputStreamReader(zipStream, "UTF-8"));
		
		generateStream(sourceContext);

		this.reader.close();
		this.reader = null;
		this.zipStream.close();
		this.zipStream = null;

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
			if (this.zipStream != null) {
				this.zipStream.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeException("Could not cancel SourceFunction", ioe);
		} finally {
			this.reader = null;
			this.zipStream = null;
		}
	}

}
