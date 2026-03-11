package dataviewer2;

import java.awt.Color;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.du.dudraw.Draw;
import edu.du.dudraw.DrawListener;

public class DrawManager {
	
	private final static String[] VISUALIZATION_MODES = { "Raw", "Extrema (within 10% of min/max)" };
	private final static double DATA_WINDOW_BORDER = 50.0;
	private final static double EXTREMA_PCT = 0.1;
	private final static int FILE_UNCERTAINTY_IDX = 2;
    private final static double	MENU_STARTING_X = 40.0;
	private final static double MENU_STARTING_Y = 90.0;
	private final static double MENU_ITEM_SPACING = 5.0;
	private final static String[] MONTH_NAMES = { "", // 1-based
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private final static double	TEMPERATURE_MAX_C = 30.0;
	private final static double	TEMPERATURE_MIN_C = -10.0;
	private final static double	TEMPERATURE_RANGE = TEMPERATURE_MAX_C - TEMPERATURE_MIN_C;
	private final static int VISUALIZATION_RAW_IDX = 0;
	private final static int VISUALIZATION_EXTREMA_IDX = 1;
	
	// Window-variables
	private final static int WINDOW_HEIGHT = 720;
	private final static String WINDOW_TITLE = "DataViewer Application";
	private final static int WINDOW_WIDTH = 1320; // should be a multiple of 12
    private Draw m_window;
    private DebugManager debugManager;
    
    private DataViewer dv;
	
	public DrawManager(DataViewer dv, DrawListener listener, DebugManager debugManager) {
		
		this.dv = dv;
		
		// Setup the DuDraw board
        m_window = new Draw(WINDOW_TITLE);
        m_window.setCanvasSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        m_window.enableDoubleBuffering(); // Too slow otherwise -- need to use .show() later
        
        // Add the mouse/key listeners
        m_window.addListener(listener);
       
        this.debugManager = debugManager;
	}
	
	public Draw getWindow() {
		return m_window;
	}

	public void drawMainMenu() {
    	m_window.clear(Color.WHITE);

    	String[] menuItems = {
    			"Type the menu number to select that option:",
    			"",
    			String.format("C     Set country: [%s]", dv.getSelectedCountry()),
    			String.format("T     Set state: [%s]", dv.getSelectedState()),
    			String.format("S     Set start year [%d]", dv.getSelectedStartYear()),
    			String.format("E     Set end year [%d]", dv.getSelectedEndYear()),
    			String.format("V     Set visualization [%s]", dv.getSelectedVisualization()),
    			String.format("P     Plot data"),
    			String.format("Q     Quit"),
    	};
    	
    	// enable drawing by "percentage" with the menu drawing
        m_window.setXscale(0, 100);
		m_window.setYscale(0, 100);
		
		// draw the menu
    	m_window.setPenColor(Color.BLACK);
		
		drawMenuItems(menuItems);
    }

	private void drawMenuItems(String[] menuItems) {
		double yCoord = MENU_STARTING_Y;
		
		for(int i=0; i<menuItems.length; i++) {
			m_window.textLeft(MENU_STARTING_X, yCoord, menuItems[i]);
			yCoord -= MENU_ITEM_SPACING;
		}
	}
    
    public void drawData(TreeMap<Integer, SortedMap<Integer,Double>> m_plotData) {
    	// Give a buffer around the plot window
        m_window.setXscale(-DATA_WINDOW_BORDER, WINDOW_WIDTH+DATA_WINDOW_BORDER);
		m_window.setYscale(-DATA_WINDOW_BORDER, WINDOW_HEIGHT+DATA_WINDOW_BORDER);

    	// gray background
    	m_window.clear(Color.LIGHT_GRAY);

    	// white plot area
		m_window.setPenColor(Color.WHITE);
		m_window.filledRectangle(WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0, WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0);  

    	m_window.setPenColor(Color.BLACK);
    	
    	double nCols = 12; // one for each month
    	double nRows = dv.getSelectedEndYear() - dv.getSelectedStartYear() + 1; // for the years
    	
    	debugManager.debug("nCols = %f, nRows = %f", nCols, nRows);
 		
        double cellWidth = WINDOW_WIDTH / nCols;
        double cellHeight = WINDOW_HEIGHT / nRows;
        
        debugManager.debug("cellWidth = %f, cellHeight = %f", cellWidth, cellHeight);
        
        boolean extremaVisualization = dv.getSelectedVisualization().equals(VISUALIZATION_MODES[VISUALIZATION_EXTREMA_IDX]);
        debugManager.info("visualization: %s (extrema == %b)", dv.getSelectedVisualization(), extremaVisualization);
        
        for(int month = 1; month <= 12; month++) {
            double fullRange = dv.getPlotMonthlyMaxValue().get(month) - dv.getPlotMonthlyMinValue().get(month);
            double extremaMinBound = dv.getPlotMonthlyMinValue().get(month) + EXTREMA_PCT * fullRange;
            double extremaMaxBound = dv.getPlotMonthlyMaxValue().get(month) - EXTREMA_PCT * fullRange;


            // draw the line separating the months and the month label
        	m_window.setPenColor(Color.BLACK);
        	double lineX = (month-1.0)*cellWidth;
        	m_window.line(lineX, 0.0, lineX, WINDOW_HEIGHT);
        	m_window.text(lineX+cellWidth/2.0, -DATA_WINDOW_BORDER/2.0, MONTH_NAMES[month]);
        	
        	// there should always be a map for the month
        	SortedMap<Integer,Double> monthData = m_plotData.get(month);
        	
        	for(int year = dv.getSelectedStartYear(); year <= dv.getSelectedEndYear(); year++) {

        		// month data structure might not have every year
        		if(monthData.containsKey(year)) {
        			Double value = monthData.get(year);
        			
        			double x = (month-1.0)*cellWidth + 0.5 * cellWidth;
        			double y = (year-dv.getSelectedStartYear())*cellHeight + 0.5 * cellHeight;
        			
        			Color cellColor = null;
        			
        			// get either color or grayscale depending on visualization mode
        			if(extremaVisualization && value > extremaMinBound && value < extremaMaxBound) {
        				cellColor = getDataColor(value, true);
        			}
        			else if(extremaVisualization) {
        				// doing extrema visualization, show "high" values in red "low" values in blue.
        				if(value >= extremaMaxBound) {
        					cellColor = Color.RED;
        				}
        				else {
        					cellColor = Color.BLUE;
        				}
        			}
        			else {
        				cellColor = getDataColor(value, false);
        			}
        			
        			// draw the rectangle for this data point
        			m_window.setPenColor(cellColor);
        			debugManager.trace("month = %d, year = %d -> (%f, %f) with %s", month, year, x, y, cellColor.toString());
        			m_window.filledRectangle(x, y, cellWidth/2.0, cellHeight/2.0);
        		}
        	}
        }
        
        // draw the labels for the y-axis
        m_window.setPenColor(Color.BLACK);

        double labelYearSpacing = (dv.getSelectedEndYear() - dv.getSelectedStartYear()) / 5.0;
        double labelYSpacing = WINDOW_HEIGHT/5.0;
        // spaced out by 5, but need both the first and last label, so iterate 6
        for(int i=0; i<6; i++) {
        	int year = (int)Math.round(i * labelYearSpacing + dv.getSelectedStartYear());
        	String text = String.format("%4d", year);
        	
        	m_window.textRight(0.0, i*labelYSpacing, text);
        	m_window.textLeft(WINDOW_WIDTH, i*labelYSpacing, text);
        }
     
        // draw rectangle around the whole data plot window
        m_window.rectangle(WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0, WINDOW_WIDTH/2.0, WINDOW_HEIGHT/2.0);
        
        // put in the title
        String title = String.format("%s, %s from %d to %d. Press 'M' for Main Menu.  Press 'Q' to Quit.",
        		dv.getSelectedState(), dv.getSelectedCountry(), dv.getSelectedStartYear(), dv.getSelectedEndYear());
        m_window.text(WINDOW_WIDTH/2.0, WINDOW_HEIGHT+DATA_WINDOW_BORDER/2.0, title);
	}
    
    /**
     * Return a Color object based on the value passed in.
     * @param value - controls the color
     * @param doGrayscale - if true, return a grayscale value (r, g, b are all equal);
     * 	otherwise return a range of red to green.
     * @return null is value is null, otherwise return a Color object
     */
    private Color getDataColor(Double value, boolean doGrayscale) {
    	if(null == value) {
    		return null;
    	}
    	double pct = (value - TEMPERATURE_MIN_C) / TEMPERATURE_RANGE;
    	debugManager.trace("converted %f raw value to %f %%", value, pct);
    
    	if (pct > 1.0) {
            pct = 1.0;
        }
        else if (pct < 0.0) {
            pct = 0.0;
        }
        int r, g, b;
        // Replace the color scheme with my own
        if (!doGrayscale) {
        	r = (int)(255.0 * pct);
        	g = 0;
        	b = (int)(255.0 * (1.0-pct));
        	
        } else {
        	// Grayscale for the middle extema
        	r = g = b = (int)(255.0 * pct);
        }
        
        debugManager.trace("converting %f to [%d, %d, %d]", value, r, g, b);

		return new Color(r, g, b);
	}

}
