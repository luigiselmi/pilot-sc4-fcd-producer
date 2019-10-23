package eu.bde.pilot.sc4.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.flink.api.java.tuple.Tuple7;
import org.apache.flink.api.java.tuple.Tuple9;
import org.apache.flink.util.Collector;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.bde.pilot.sc4.fcd.FcdTaxiEvent;


/**
 * This class is used to connect to a R daemon from Java and execute R functions.
 * @author Luigi Selmi
 *
 */

public class MapMatch {
  
  
  private final Logger log = LoggerFactory.getLogger(MapMatch.class);
  public static final String DEVICE_ID = "device_random_id";
  private RConnection conn;
  private String host;
  private int port;
  
  public MapMatch(String host, int port) {
    this.host = host;
    this.port = port;
    conn = initRserve(host, port);
    
  }
  
  /**
   * This method requires three parameters in the following order
   * 1) The data set to be map matched 
   * 2) IP address of Rserve. The default value is localhost
   * 3) Port number of Rserve. The default value is 6311
   * @param urlDataset
   * @param rserveHost IP address of Rserve. The default value is localhost
   * @param rservePort Port number of Rserve. The default value is 6311
   * @throws MalformedURLException
   * @throws IOException
   * @throws REXPMismatchException 
   */
  public Collector<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> mapMatch(
      Iterable<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> events,
      Collector<Tuple9<Integer,String,Double,Double,Double,Double,Double,Integer,String>> matches
      ) throws MalformedURLException, IOException, REXPMismatchException 
    { 
    RList list = null;
    // Rserve root folder. Contains the R script with the functions used by the client (this class)
    // and the geographical data for the map matching
    final String RSERVE_HOME = "/home/sc4pilot/rserve";
    
    try {  
      
      // transform from a tuples collection to a data frame
      list = RUtil.createRList(events, false);
      int numOfEvents = list.at(DEVICE_ID).length();
      log.info("Number of FCD taxi events: " + numOfEvents);
      // Evaluates R commands
      //conn.eval("setwd('" + RSERVE_HOME + "')"); //to be removed, it must be done in the R docker image
      //conn.voidEval("loadPackages()");  //to be removed, it must be done in the R docker image
      //conn.voidEval("road<-readGeoData()");  //to be removed, it must be done in the R docker image
      conn.assign("gdata", REXP.createDataFrame(list));
      //conn.voidEval("initgps<-initGpsData(gpsdata)");
      //conn.voidEval("gdata<-readGpsData(gpsdata)");
      RList matchesList = conn.eval("match(gdata,\"localhost\",5432,\"thessaloniki\",\"postgres\",\"password\")").asList();
      if(matchesList.size() > 0) {
        int numOfMatches = matchesList.at(DEVICE_ID).length();
        log.info("Number of matches: " + numOfMatches);
        // transform from a data frame to a tuples collection
        if(numOfMatches > 0) 
          RUtil.createFcdTaxiEventsFromRList(matchesList,matches);
      }
      
    } catch (RserveException e) {   
      e.printStackTrace();
    } catch (REXPMismatchException e) {     
      e.printStackTrace();
    } finally {
      conn.close();
      log.info("Rserve: closed connection.");
    }
    
    return matches;
  
  }
  
  public RConnection initRserve(String rserveHost, int rservePort) {
    RConnection c = null;
    try {
      c = new RConnection(rserveHost, rservePort);
      c.voidEval("loadPackages()");
      REXP x = c.eval("R.version.string");
      log.info(x.asString());
      
    } catch (RserveException e) {     
      e.printStackTrace();
    } catch (REXPMismatchException e) {     
      e.printStackTrace();
    }
    return c;
  }

}
