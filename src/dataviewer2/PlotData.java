package dataviewer2;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PlotData {
	
	private final static int RECORD_TEMPERATURE_IDX = 2;
	private final static int RECORD_MONTH_IDX = 1;
	private final static int RECORD_STATE_IDX = 3;
	private final static int RECORD_YEAR_IDX = 0;
	
	private DataViewer dv;
	
	public PlotData(DataViewer dv) {
		this.dv = dv;
	}

	public void updatePlotData() {
		//debug("raw data: %s", m_rawData.toString());
		// plot data is a map where the key is the Month, and the value is a sorted map where the key
		// is the year. 
		dv.setPlotData(new TreeMap<Integer,SortedMap<Integer,Double>>());
		
		for(int month = 1; month <= 12; month++) {
			// any year/months not filled in will be null
			dv.getPlotData().put(month, new TreeMap<Integer,Double>());
		}
		// now run through the raw data and if it is related to the current state and within the current
		// years, put it in a sorted data structure, so that we 
		// find min/max year based on data 
		dv.setPlotMonthlyMaxValue(new TreeMap<Integer,Double>());
		dv.setPlotMonthyMinValue(new TreeMap<Integer,Double>());
		
		// initialize
		for(int i = 1; i <= 12; i++) {
			dv.getPlotMonthlyMaxValue().put(i, Double.MIN_VALUE);
			dv.getPlotMonthlyMinValue().put(i, Double.MAX_VALUE);
		}
		for(List<Object> rec : dv.getDataRaw()) {
			String state = (String)rec.get(RECORD_STATE_IDX);
			Integer year = (Integer)rec.get(RECORD_YEAR_IDX);
			
			// Check to see if they are the state and year range we care about
			if (state.equals(dv.getSelectedState()) && 
			   ((year.compareTo(dv.getSelectedStartYear()) >= 0 && year.compareTo(dv.getSelectedEndYear()) <= 0))) {
						
				// Ok, we need to add this to the list of values for the month
				Integer month = (Integer)rec.get(RECORD_MONTH_IDX);
				Double value = (Double)rec.get(RECORD_TEMPERATURE_IDX);
				
				if(!dv.getPlotMonthlyMinValue().containsKey(month) || value.compareTo(dv.getPlotMonthlyMinValue().get(month)) < 0) {
					dv.getPlotMonthlyMinValue().put(month, value);
				}
				if(!dv.getPlotMonthlyMaxValue().containsKey(month) || value.compareTo(dv.getPlotMonthlyMaxValue().get(month)) > 0) {
					dv.getPlotMonthlyMaxValue().put(month, value);
				}
	
				dv.getPlotData().get(month).put(year, value);
			}
		}
		//debug("plot data: %s", m_plotData.toString());
	}
	
}
