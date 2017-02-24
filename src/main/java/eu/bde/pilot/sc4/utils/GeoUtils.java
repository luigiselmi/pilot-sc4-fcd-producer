package eu.bde.pilot.sc4.utils;

/**
 * 
 * @author Luigi Selmi
 *
 */

public class GeoUtils {
	
	// Bounding box of the area of Thessaloniki 
	// The bounding box is determined by two points (LON_WEST, LAT_NORTH) and (LON_EAST, LAT_SOUTH)
	// The reference point of the grid is (LON_WEST, LAT_NORTH)
	public static double LON_EAST = 23.05;
	public static double LON_WEST = 22.80;
	public static double LAT_NORTH = 40.70;
	public static double LAT_SOUTH = 40.50;

	// bounding box angles widths
	public static double LON_DEG = LON_EAST - LON_WEST;
	public static double LAT_DEG = LAT_NORTH - LAT_SOUTH;
	
	// Number of grid lines along the longitude and latitude
	public static int LON_GRID_LINES = 4;
	public static int LAT_GRID_LINES = 4;

	// angular distances between grid lines along the longitude and the latitude
	public static double DELTA_LON = LON_DEG / LON_GRID_LINES;
	public static double DELTA_LAT = LAT_DEG / LAT_GRID_LINES;
	
	/**
	 * The grid is a matrix that contains the value of the cell ids. For example
	 * if the grid is a 4x4 matrix
	 *  
	 * |  1  2  3  4 |
	 * |  5  6  7  8 |
	 * |  9 10 11 12 |
	 * | 13 14 15 16 |
	 * 
	 * a point (lon,lat) that is in the cell(2,3) will have a cell id = 7  
	 * @param rows
	 * @param columns
	 */
	public static int [][] initGrid() {
		int [][] grid = new int[LAT_GRID_LINES][LON_GRID_LINES];
		int counter = 0;
		int lastCell = 0;
		for(int i = 0; i < LON_GRID_LINES; i++) {
			lastCell += counter;
			for(int j = 0; j < LAT_GRID_LINES; j++) {
				int cellId = 1 + i + j + lastCell;
				grid[i][j] = cellId;
				counter = j;
			}
			
		}
		
		return grid;
	}
	
	/**
	 * Checks if a location specified by longitude and latitude values is
	 * within the bounding box of Thessaloniki.
	 *
	 * @param lon longitude of the location to check
	 * @param lat latitude of the location to check
	 *
	 * @return true if the location is within the bounding box, otherwise false.
	 */
	public static boolean isWithinBoundingBox(double lon, double lat) {
        
         return (lon > LON_WEST && lon < LON_EAST) && (lat > LAT_SOUTH && lat < LAT_NORTH); 
	}
	
	/**
	 * Maps a location specified by latitude and longitude values to a cell of a
	 * grid covering the bounding box. Retuns the id of the cell in which the point is located
	 * If the point is outside the bounding box, returns 0
	 * @param lon longitude of the location to map
	 * @param lat latitude of the location to map
	 *
	 * @return id of mapped grid cell.
	 */
	public static int mapToGridCell(double lon, double lat, int [][] grid) {
		int cellId = 0;
		int row = getLatitudeGrid(lat);
		int column = getLongitudeGrid(lon);
		if(isWithinBoundingBox(lon,lat))
		  cellId = grid[row - 1][column - 1];
		
		return cellId;
	}
	
	/**
	 * Return the number of grid lines along the latitude from 
	 * the reference latitude LAT_NORTH
	 * @param lat
	 * @return
	 */
	public static int getLatitudeGrid(double lat) {
		int latGrid = 0;
		for (int i = 0; i < LAT_GRID_LINES; i++) {
			double upperLatGridLine = LAT_NORTH - DELTA_LAT*i;
			double lowerLatGridLine = upperLatGridLine - DELTA_LAT;
			if ( lat <= upperLatGridLine && lat > lowerLatGridLine)
				return latGrid = i + 1;
		}
		
		return latGrid;
	}
	/**
	 * Returns the number of grid lines along the latitude from
	 * the reference longitude LON_WEST
	 * @param lon
	 * @return
	 */
	public static int getLongitudeGrid(double lon) {
		int lonGrid = 0;
		for (int i = 0; i < LON_GRID_LINES; i++) {
			double lowerLonGridLine = LON_WEST + DELTA_LON*i;
			double upperLonGridLine = lowerLonGridLine + DELTA_LON;
			if ( lon >= lowerLonGridLine && lon < upperLonGridLine)
				return lonGrid = i + 1;
			
		}
		
		return lonGrid;
	}
	
	public static int getCellId(int row, int column, int [][] grid){
		return grid[row - 1][column - 1];
	}

}
