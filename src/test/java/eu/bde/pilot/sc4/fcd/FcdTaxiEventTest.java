package eu.bde.pilot.sc4.fcd;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;

public class FcdTaxiEventTest {

  String jsonRecord; // string containing one json record (the first one)
  String csvRecord; // string containing one csv record (the first one)
  
  @Before
  public void setUp() throws Exception {
    jsonRecord = getJsonRecord();
    csvRecord = getCsvRecord(); 
  }
  /*
   * Parses the json data and select the 1st record
   */
  private String getJsonRecord() throws IOException {
    InputStream jsonStream = Resources.getResource("gps-sample-data.json").openStream();
    BufferedReader jsonReader = new BufferedReader(new InputStreamReader(jsonStream));
    String jsonLine;
    StringBuffer records = new StringBuffer();
    while ( (jsonLine = jsonReader.readLine()) != null ) {
      records.append(jsonLine);
    }
    String jsonStringRecords = records.toString();
    ArrayList<String> jsonRecords = FcdTaxiEventUtils.getJsonRecords(jsonStringRecords);
    return jsonRecords.get(0);
  }
  /*
   * Parse the csv data and select the 1st record
   */
  private String getCsvRecord() throws IOException {
    InputStream csvStream = Resources.getResource("fcd-sample-data.csv").openStream();
    BufferedReader csvReader = new BufferedReader(new InputStreamReader(csvStream));
    String csvLine;
    StringBuffer records = new StringBuffer();
    while ( (csvLine = csvReader.readLine()) != null ) {
      records.append(csvLine);
      break;
    }
    return csvLine;
  }

  @Test
  public void testFromJsonString() throws IOException {
    
    FcdTaxiEvent event = FcdTaxiEvent.fromJsonString(jsonRecord);
    assertTrue(event.deviceId == 79163);
    
  }
  
  @Test
  public void testFromString() {
    FcdTaxiEvent event = FcdTaxiEvent.fromString(csvRecord);
    assertTrue(event.deviceId == 84037);
  }


  @Test
  public void testFromBinary() throws IOException {
    FcdTaxiEvent event = FcdTaxiEventUtils.fromJsonString(jsonRecord);
    byte [] avro = event.toBinary();
    FcdTaxiEvent copy = FcdTaxiEvent.fromBinary(avro);
    assertTrue(copy.equals(event));
    
  }

}
