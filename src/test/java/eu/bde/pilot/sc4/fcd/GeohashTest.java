package eu.bde.pilot.sc4.fcd;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.apache.lucene.spatial.util.GeoHashUtils;

import eu.bde.pilot.sc4.utils.Geohash;

public class GeohashTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testEncodeBase32() {
		double lat = 40.626444;
		double lon = 22.948426;
		String geohash = "sx0r45yd1n4d";
		int bits = 10;
		String encode = Geohash.encodeBase32(lat, lon, bits);
		String geohashUpToBits = geohash.substring(0, bits/5);
		assertTrue(encode.equals(geohashUpToBits));
	}

	@Test
	public void testDecodeBase32() {
		String geohash = "sx0r45yd1n4d";
		double maxLat = Geohash.decodeBase32(geohash).maxLat();
		double minLat = Geohash.decodeBase32(geohash).minLat();
		double maxLon = Geohash.decodeBase32(geohash).maxLng();
		double minLon = Geohash.decodeBase32(geohash).minLng();
		double lat = (maxLat + minLat)/2;
		double lon = (maxLon + minLon)/2;
		System.out.println("Lat = " + lat + " Lon = " + lon);
	}
	
	@Test
	public void testLucene() {
		double lat = 40.626444;
		double lon = 22.948426;
		int level = 5;
		String geohash = "sx0r45yd1n4d";
		String encode = GeoHashUtils.stringEncode(lon, lat, level);
		String geohashUpToLevel = geohash.substring(0, level);
		assertTrue(encode.equals(geohashUpToLevel));
	}

}
