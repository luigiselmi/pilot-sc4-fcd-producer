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
		
		int cellId = geo.getCellId(3,2);
		assertTrue(cellId == 10);
	}
    
	@Test
	public void testGetCellId() {
		int cellId = geo.getCellId(3,3);
		assertTrue(cellId == 11);
	}

	@Test
	public void testIsWithinBoundingBox() {
		assertTrue(geo.isWithinBoundingBox(22.90, 40.69));
	
	}

	@Test
	public void testGetLatitudeGrid() {
		int row = geo.getLatitudeGrid(40.56);
		assertTrue(row == 3); 
	}

	@Test
	public void testGetLongitudeGrid() {
		int column = geo.getLongitudeGrid(22.925);
		assertTrue(column == 2);
	}
    
	@Test
	public void testMapToGridCell() {
		assertTrue(geo.mapToGridCell(22.925, 40.56) == 10);
	}
}
