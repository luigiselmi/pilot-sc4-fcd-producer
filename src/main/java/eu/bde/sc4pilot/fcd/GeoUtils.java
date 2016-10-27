package eu.bde.sc4pilot.fcd;

public class GeoUtils {
	
	// Bounding box of the area of Thessaloniki 
	// The bounding box is determined by two points (LON_WEST, LAT_NORTH) and (LON_EAST, LAT_SOUTH)
	// The reference point of the grid is (LON_WEST, LAT_NORTH)
	public double LON_EAST = 23.05;
	public double LON_WEST = 22.80;
	public double LAT_NORTH = 40.70;
	public double LAT_SOUTH = 40.50;

	// bounding box angles widths
	public double LON_DEG = LON_EAST - LON_WEST;
	public double LAT_DEG = LAT_NORTH - LAT_SOUTH;
	
	// Number of grid lines along the longitude and latitude
	public int LON_GRID_LINES = 10;
	public int LAT_GRID_LINES = 10;

	// angular distances between grid lines along the longitude and the latitude
	public double DELTA_LON = LON_DEG / LON_GRID_LINES;
	public double DELTA_LAT = LAT_DEG / LAT_GRID_LINES;
	
	public int [][] grid; 
	
	public GeoUtils(){
		
	}
	
	public void initGrid(int rows, int columns) {
		grid = new int[rows][columns];
		int counter = 0;
		int lastCell = 0;
		for(int i = 0; i < columns; i++) {
			lastCell += counter;
			for(int j = 0; j < rows; j++) {
				int cellId = 1 + i + j + lastCell;
				grid[i][j] = cellId;
				counter = j;
			}
			
		}
	}
	
	public int getCellId(int i, int j) {
		return grid[i - 1][j - 1];
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
	public boolean isWithinBoundingBox(double lon, double lat) {
        
         return (lon > LON_WEST && lon < LON_EAST) && (lat > LAT_SOUTH && lat < LAT_NORTH); 
	}
	
	/**
	 * Maps a location specified by latitude and longitude values to a cell of a
	 * grid covering the bounding box.
	 * @param lon longitude of the location to map
	 * @param lat latitude of the location to map
	 *
	 * @return id of mapped grid cell.
	 */
	public int mapToGridCell(double lon, double lat) {
		int cellId = 0;
		
		return cellId;
	}
	
	/**
	 * Return the number of grid lines along the latitude from 
	 * the reference latitude LAT_NORTH
	 * @param lat
	 * @return
	 */
	public int getLatitudeGrid(double lat) {
		int latGrid = 0;
		for (int i = 0; i < LAT_GRID_LINES; i++) {
			double upperLatGridLine = LAT_NORTH - DELTA_LAT*i;
			double lowerLatGridLine = upperLatGridLine - DELTA_LAT;
			if ( lat < upperLatGridLine && lat > lowerLatGridLine){
				latGrid = i;
			}
		}
		
		return latGrid;
	}
	/**
	 * Returns the number of grid lines along the latitude from
	 * the reference longitude LON_WEST
	 * @param lon
	 * @return
	 */
	public int getLongitudeGrid(double lon) {
		int lonGrid = 0;
		for (int i = 0; i < LON_GRID_LINES; i++) {
			double lowerLonGridLine = LON_WEST + DELTA_LON*i;
			double upperLonGridLine = lowerLonGridLine + DELTA_LON;
			if ( lon > lowerLonGridLine && lon < upperLonGridLine){
				lonGrid = i;
			}
		}
		
		return lonGrid;
	}

}
