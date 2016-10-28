package eu.bde.sc4pilot.fcd;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class GeoUtilsTest {
	
	int [][] grid;

	@Before
	public void setUp() throws Exception {
		grid = GeoUtils.initGrid();
	}

	@Test
	public void testInitGrid() {
		
		int cellId = GeoUtils.getCellId(3,2,grid);
		assertTrue(cellId == 10);
	}
    
	@Test
	public void testGetCellId() {
		int cellId = GeoUtils.getCellId(3,3,grid);
		assertTrue(cellId == 11);
	}

	@Test
	public void testIsWithinBoundingBox() {
		assertTrue(GeoUtils.isWithinBoundingBox(22.90, 40.69));
	
	}

	@Test
	public void testGetLatitudeGrid() {
		int row = GeoUtils.getLatitudeGrid(40.56);
		assertTrue(row == 3); 
	}

	@Test
	public void testGetLongitudeGrid() {
		int column = GeoUtils.getLongitudeGrid(22.924);
		assertTrue(column == 2);
	}
    
	@Test
	public void testMapToGridCell() {
		assertTrue(GeoUtils.mapToGridCell(22.924, 40.56, grid) == 10);
	}
	
	@Test
	public void testOffBoundingBox() {
		assertTrue(GeoUtils.mapToGridCell(22.00, 40.80, grid) == 0);
	}
	
}
