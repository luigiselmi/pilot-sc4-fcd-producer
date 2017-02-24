package eu.bde.pilot.sc4.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.util.Collector;
import org.joda.time.DateTime;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.bde.pilot.sc4.fcd.FcdTaxiEvent;

/**
 * Provides some methods to transform Java collections into R data frame
 * @author Luigi Selmi
 *
 */
public class RUtil {
  
  private final Logger log = LoggerFactory.getLogger(RUtil.class);
  
  // database column names
  public static final String DEVICE_ID = "device_random_id";
  public static final String TIMESTAMP = "recorded_timestamp";
  public static final String LONGITUDE = "lon";
  public static final String LATITUDE = "lat";
  public static final String ALTITUDE = "altitude";
  public static final String SPEED = "speed";
  public static final String ORIENTATION = "orientation";
  public static final String TRANSFER = "transfer";
  public static final String ROAD_SEGMENT = "osmids";
  
  /**
   *   
   * @param is
   * @param header
   * @return
   * @throws MalformedURLException
   * @throws IOException
   */
  public GpsColumns readCsv(InputStream is, boolean header) throws MalformedURLException, IOException {
    ArrayList<String> csvLines = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String recordLine;
    String headerLine;
    if(header) 
      headerLine = reader.readLine();
    while((recordLine = reader.readLine()) != null) {
      csvLines.add(recordLine);
    }
    
    int size = csvLines.size();
    
    GpsColumns gps = new GpsColumns(size);
    
    int [] deviceId = new int[size];
    String [] insertedTimestamp = new String[size];
    double [] longitude = new double[size];
    double [] latitude = new double[size]; 
    double [] altitude = new double[size];
    int [] speed = new int[size];
    double [] orientation = new double[size];
    String [] recordedTimestamp = new String[size];
    int [] valid = new int[size];
    int [] zoneid = new int[size];
    int [] transfer = new int[size];
    String [] unixTimestamp = new String[size];

    
    Iterator<String> icsvLines = csvLines.iterator();
    int recordNumber = 0;
    while (icsvLines.hasNext()) {
      String [] fields = icsvLines.next().split(",");
      deviceId[recordNumber] = Integer.valueOf(fields[0]);
      insertedTimestamp[recordNumber] = fields[1];
      longitude[recordNumber] = Double.valueOf(fields[2]);
      latitude[recordNumber] = Double.valueOf(fields[3]);
      altitude[recordNumber] = Double.valueOf(fields[4]);
      speed[recordNumber] = Integer.valueOf(fields[5]);
      orientation[recordNumber] = Double.valueOf(fields[6]);
      recordedTimestamp[recordNumber] = fields[7];
      valid[recordNumber] = Integer.valueOf(fields[8]);
      zoneid[recordNumber] = Integer.valueOf(fields[9]);
      transfer[recordNumber] = Integer.valueOf(fields[10]);
      unixTimestamp[recordNumber] = fields[11];
      recordNumber++;
    }
    
    gps.setDeviceId(deviceId);
    gps.setInsertedTimestamp(insertedTimestamp);
    gps.setLongitude(longitude);
    gps.setLatitude(latitude);
    gps.setAltitude(altitude);
    gps.setSpeed(speed);
    gps.setRecordedTimestamp(recordedTimestamp);
    gps.setValid(valid);
    gps.setZoneid(zoneid);
    gps.setTransfer(transfer);
    gps.setUnixTimestamp(unixTimestamp);
    
    return gps;
  }
  /**
   * Creates a R data frame from a CSV file  
   * @param is
   * @param header
   * @return
   * @throws MalformedURLException
   * @throws IOException
   */
  public RList createRListFromCsv(InputStream is, boolean header) throws MalformedURLException, IOException  {
    RList rlist = new RList();
    GpsColumns gps = readCsv(is, header);
  
    rlist.put(GpsColumns.names[0],new REXPInteger(gps.getDeviceId()));
    rlist.put(GpsColumns.names[1],new REXPString(gps.getInsertedTimestamp()));
    rlist.put(GpsColumns.names[2],new REXPDouble(gps.getLongitude()));
    rlist.put(GpsColumns.names[3],new REXPDouble(gps.getLatitude()));
    rlist.put(GpsColumns.names[4],new REXPDouble(gps.getAltitude()));
    rlist.put(GpsColumns.names[5],new REXPInteger(gps.getSpeed()));
    rlist.put(GpsColumns.names[6],new REXPDouble(gps.getOrientation()));
    rlist.put(GpsColumns.names[7],new REXPString(gps.getRecordedTimestamp()));
    rlist.put(GpsColumns.names[8],new REXPInteger(gps.getValid()));
    rlist.put(GpsColumns.names[9],new REXPInteger(gps.getZoneid()));
    rlist.put(GpsColumns.names[10],new REXPInteger(gps.getTransfer()));
    rlist.put(GpsColumns.names[11],new REXPString(gps.getUnixTimestamp()));
   
    return rlist;
  }
  /**
   * Creates an RList from a tuple of Fcd Taxi Events
   * @param event
   * @return
   * @throws REXPMismatchException 
   */
  public static RList createRList(Iterable<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> events, boolean header) throws REXPMismatchException {
    RList rlist = new RList();
    
    int [] deviceId;
    String [] timestamp;
    double [] longitude;
    double [] latitude; 
    double [] altitude;
    double [] speed;
    double [] orientation;
    int [] transfer;
    
    ArrayList<Integer> deviceIdList = new ArrayList<Integer>();
    ArrayList<String> timestampList = new ArrayList<String>();
    ArrayList<Double> longitudeList = new ArrayList<Double>();
    ArrayList<Double> latitudeList = new ArrayList<Double>();
    ArrayList<Double> altitudeList = new ArrayList<Double>();
    ArrayList<Double> speedList = new ArrayList<Double>();
    ArrayList<Double> orientationList = new ArrayList<Double>();
    ArrayList<Integer> transferList = new ArrayList<Integer>();
    
    for(Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String> event: events) {
      
      deviceIdList.add(event.f0);
      timestampList.add(event.f1);
      longitudeList.add(event.f2);
      latitudeList.add(event.f3);
      altitudeList.add(event.f4);
      speedList.add(event.f5);
      orientationList.add(event.f6);
      transferList.add(event.f7);
      
    }
    
    int size = deviceIdList.size();
      
    deviceId = deviceIdList.stream().mapToInt(x -> x).toArray();
    System.out.println("deviceId: " + deviceId.length);
    timestamp = timestampList.toArray(new String[size]);
    System.out.println("timestamp: " + timestamp.length);
    longitude = longitudeList.stream().mapToDouble(x -> x).toArray();
    System.out.println("longitude: " + longitude.length);
    latitude = latitudeList.stream().mapToDouble(x -> x).toArray();
    System.out.println("latitude: " + latitude.length);
    altitude = altitudeList.stream().mapToDouble(x -> x).toArray();
    System.out.println("altitude: " + altitude.length);
    speed = speedList.stream().mapToDouble(x -> x).toArray();
    System.out.println("speed: " + speed.length);
    orientation = orientationList.stream().mapToDouble(x -> x).toArray();
    System.out.println("orientation: " + orientation.length);
    transfer = transferList.stream().mapToInt(x -> x).toArray();
    System.out.println("transfer: " + transfer.length);
    
    rlist.put(DEVICE_ID, new REXPInteger(deviceId));
    rlist.put(TIMESTAMP, new REXPString(timestamp));
    rlist.put(LONGITUDE, new REXPDouble(longitude));
    rlist.put(LATITUDE, new REXPDouble(latitude));
    rlist.put(ALTITUDE, new REXPDouble(altitude));
    rlist.put(SPEED, new REXPDouble(speed));
    rlist.put(ORIENTATION, new REXPDouble(orientation));
    rlist.put(TRANSFER, new REXPInteger(transfer));
    
    System.out.println("deviceid in Rlist: " + rlist.at(DEVICE_ID).length());
    
    return rlist;
  }
  
  /**
   * Creates an R data frame from an ArrayLst.
   * @return
   */
  public RList createRListFromList(ArrayList<GpsRecord> gpsrecords, boolean header) {
    RList rlist = new RList();
    
    int size = gpsrecords.size();
    
    int [] deviceId = new int[size];
    String [] insertedTimestamp = new String[size];
    double [] longitude = new double[size];
    double [] latitude = new double[size]; 
    double [] altitude = new double[size];
    int [] speed = new int[size];
    double [] orientation = new double[size];
    String [] recordedTimestamp = new String[size];
    int [] valid = new int[size];
    int [] zoneid = new int[size];
    int [] transfer = new int[size];
    String [] unixTimestamp = new String[size];
    String [] links = new String[size];
    
    Iterator<GpsRecord> igpsrecords = gpsrecords.iterator();
    int recordNumber = 0;
    while (igpsrecords.hasNext()) {
      GpsRecord gpsrecord = igpsrecords.next();
      deviceId[recordNumber] = Integer.valueOf( gpsrecord.getDeviceId() );
      insertedTimestamp[recordNumber] = gpsrecord.getTimestamp();
      longitude[recordNumber] = gpsrecord.getLon();
      latitude[recordNumber] = gpsrecord.getLat();
      altitude[recordNumber] = gpsrecord.getAltitude();
      speed[recordNumber] = gpsrecord.getSpeed();
      orientation[recordNumber] = gpsrecord.getOrientation();
      recordedTimestamp[recordNumber] = gpsrecord.getTimestamp();
      valid[recordNumber] = 1; // not set in the json gps data. taking the most common value from sample csv data set
      zoneid[recordNumber] = 20; // not set in the json gps data. taking the most common value from sample csv data set
      transfer[recordNumber] = gpsrecord.getTransfer();
      unixTimestamp[recordNumber] = gpsrecord.getTimestamp(); // not set in the json gps data, taking the same as recorded_timestamp
      recordNumber++;
    }
    
    rlist.put(GpsColumns.names[0],new REXPInteger(deviceId));
    rlist.put(GpsColumns.names[1],new REXPString(insertedTimestamp));
    rlist.put(GpsColumns.names[2],new REXPDouble(longitude));
    rlist.put(GpsColumns.names[3],new REXPDouble(latitude));
    rlist.put(GpsColumns.names[4],new REXPDouble(altitude));
    rlist.put(GpsColumns.names[5],new REXPInteger(speed));
    rlist.put(GpsColumns.names[6],new REXPDouble(orientation));
    rlist.put(GpsColumns.names[7],new REXPString(recordedTimestamp));
    rlist.put(GpsColumns.names[8],new REXPInteger(valid));
    rlist.put(GpsColumns.names[9],new REXPInteger(zoneid));
    rlist.put(GpsColumns.names[10],new REXPInteger(transfer));
    rlist.put(GpsColumns.names[11],new REXPString(unixTimestamp));
   
    return rlist;
  }
  /**
   * Transforms an RList to an array of GpsRecord
   * @param rlist
   * @return
   * @throws REXPMismatchException
   */
  public static ArrayList<GpsRecord> createListFromRList(RList rlist) throws REXPMismatchException {
    ArrayList<GpsRecord> gpsrecords = new ArrayList<GpsRecord>();
    int [] deviceId = rlist.at(GpsColumns.names[0]).asIntegers();
    String [] insertedTimestamp = rlist.at(GpsColumns.names[1]).asStrings();       
    double [] longitude = rlist.at(GpsColumns.names[2]).asDoubles();
    double [] latitude = rlist.at(GpsColumns.names[3]).asDoubles();
    double [] altitude = rlist.at(GpsColumns.names[4]).asDoubles();
    int [] speed = rlist.at(GpsColumns.names[5]).asIntegers();
    double [] orientation = rlist.at(GpsColumns.names[6]).asDoubles();
    String [] recordedTimestamp = rlist.at(GpsColumns.names[7]).asStrings();
    int [] valid = rlist.at(GpsColumns.names[8]).asIntegers();
    int [] zoneid = rlist.at(GpsColumns.names[9]).asIntegers();
    int [] transfer = rlist.at(GpsColumns.names[10]).asIntegers();
    String [] unixTimestamp = rlist.at(GpsColumns.names[11]).asStrings();
    String [] link = rlist.at(15).asStrings();
    
    for (int i = 0; i < rlist.size(); i++) {      
      GpsRecord gpsrecord = new GpsRecord();
      gpsrecord.setDeviceId(deviceId[i]);
      gpsrecord.setTimestamp(insertedTimestamp[i]);
      gpsrecord.setLon(longitude[i]);
      gpsrecord.setLat(latitude[i]);
      gpsrecord.setAltitude(altitude[i]);
      gpsrecord.setSpeed(speed[i]);
      gpsrecord.setOrientation(orientation[i]);
      gpsrecord.setTransfer(transfer[i]);
      gpsrecord.setLink(link[i]);
      gpsrecords.add(gpsrecord);
    }
    
    return gpsrecords;
  }
  
  /**
   * Transforms an RList to a collection of FCD taxi events
   * @param rlist
   * @return
   * @throws REXPMismatchException
   */
  public static void createFcdTaxiEventsFromRList(RList rlist,
      Collector<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> out) throws REXPMismatchException 
  {
    int size = rlist.at(DEVICE_ID).length();
    
    int [] deviceId = rlist.at(DEVICE_ID).asIntegers();
    System.out.println("deviveId size: " + deviceId.length);
    String [] timestamp = rlist.at(TIMESTAMP).asStrings();       
    double [] longitude = Arrays.stream(rlist.at(LONGITUDE).asStrings()).mapToDouble(x -> Double.parseDouble(x)).toArray();
    double [] latitude = Arrays.stream(rlist.at(LATITUDE).asStrings()).mapToDouble(x -> Double.parseDouble(x)).toArray();
    double [] altitude = Arrays.stream(rlist.at(ALTITUDE).asStrings()).mapToDouble(x -> Double.parseDouble(x)).toArray();
    double [] speed = rlist.at(SPEED).asDoubles();
    double [] orientation = rlist.at(ORIENTATION).asDoubles();
    int [] transfer = rlist.at(TRANSFER).asIntegers();
    String [] roadSegment = rlist.at(ROAD_SEGMENT).asStrings();
    System.out.println("roadSegment size: " + roadSegment.length);
    
    for (int i = 0; i < size; i++) {  
      Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String> tp9 = new Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>();
      tp9.f0 = deviceId[i];
      tp9.f1 = timestamp[i];
      tp9.f2 = longitude[i];
      tp9.f3 = latitude[i];
      tp9.f4 = altitude[i];
      tp9.f5 = speed[i];
      tp9.f6 = orientation[i];
      tp9.f7 = transfer[i];
      tp9.f8 = roadSegment[i];
      out.collect(tp9);
    }
  }
}
