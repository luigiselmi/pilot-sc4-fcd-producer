package eu.bde.sc4pilot.fcd;

import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class FcdTaxiEvent {
	
	private static transient DateTimeFormatter timeFormatter =
			DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withLocale(Locale.US).withZoneUTC();
	
	private int deviceId = -1;
	private DateTime timestamp;
	private double lon = 0.0;
	private double lat = 0.0;
	private double altitude = 0.0;
	private int speed = 0;
	private double orientation = 0.0;
	private int transfer = 0;
	

	FcdTaxiEvent() {}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("test");

		return sb.toString();
	}
	
	public static FcdTaxiEvent fromString(String line) {

		String[] tokens = line.split(",");
		if (tokens.length != 8) {
			throw new RuntimeException("Invalid record: " + line);
		}

		FcdTaxiEvent event = new FcdTaxiEvent();

		try {
			event.deviceId = Integer.parseInt(tokens[0]);
			event.timestamp = DateTime.parse(tokens[1], timeFormatter);
			event.lon = tokens[2].length() > 0 ? Double.parseDouble(tokens[2]) : 0.0f;
			event.lat = tokens[3].length() > 0 ? Double.parseDouble(tokens[3]) : 0.0f;
			event.altitude = tokens[4].length() > 0 ? Double.parseDouble(tokens[4]) : 0.0f;
			event.speed = tokens[5].length() > 0 ? Integer.parseInt(tokens[5]) : 0;
			event.orientation = tokens[6].length() > 0 ? Double.parseDouble(tokens[7]) : 0.0f;
			event.transfer = tokens[7].length() > 0 ? Integer.parseInt(tokens[7]) : 0;

		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Invalid record: " + line, nfe);
		}

		return event;
	}

}
