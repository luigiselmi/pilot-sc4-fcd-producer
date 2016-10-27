package eu.bde.sc4pilot.fcd;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class GeoUtilsTest {
	
	GeoUtils geo;

	@Before
	public void setUp() throws Exception {
		geo = new GeoUtils();
		geo.initGrid(4,4);
	}

	@Test
	public void testInitGrid() {
		
		int cellId = geo.getCellId(4,4);
		assertTrue(cellId == 16);
	}
    
	@Test
	public void testGetCellId() {
		int cellId = geo.getCellId(2,4);
		assertTrue(cellId == 8);
	}

	@Test
	public void testIsWithinBoundingBox() {
		assertTrue(geo.isWithinBoundingBox(22.90, 40.69));
	
	}
    /*
	@Test
	public void testMapToGridCell() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLatitudeGrid() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLongitudeGrid() {
		fail("Not yet implemented");
	}
    */
}
