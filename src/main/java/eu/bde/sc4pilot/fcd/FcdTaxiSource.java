package eu.bde.sc4pilot.fcd;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.flink.streaming.api.functions.source.SourceFunction;


public class FcdTaxiSource implements SourceFunction<FcdTaxiEvent>{
	
	private final String dataFilePath;
	private final int servingSpeed;

	private transient BufferedReader reader;
	private transient InputStream gzipStream;
	
	
	public FcdTaxiSource(String dataFilePath, int servingSpeedFactor) {
		this.dataFilePath = dataFilePath;
		this.servingSpeed = servingSpeedFactor;
	}
	
	@Override
	public void run(SourceContext<FcdTaxiEvent> sourceContext) throws Exception {

		gzipStream = new GZIPInputStream(new FileInputStream(dataFilePath));
		reader = new BufferedReader(new InputStreamReader(gzipStream, "UTF-8"));

		this.reader.close();
		this.reader = null;
		this.gzipStream.close();
		this.gzipStream = null;

	}
	
	@Override
	public void cancel() {
		try {
			if (this.reader != null) {
				this.reader.close();
			}
			if (this.gzipStream != null) {
				this.gzipStream.close();
			}
		} catch(IOException ioe) {
			throw new RuntimeException("Could not cancel SourceFunction", ioe);
		} finally {
			this.reader = null;
			this.gzipStream = null;
		}
	}

}
