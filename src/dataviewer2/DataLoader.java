package dataviewer2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class DataLoader {
	
	// data storage
    private final String m_dataFile;
    private DataViewer dv;
    private TemperatureRecord tempRecord;
    private DebugManager debugManager;
	
	public DataLoader(DataViewer dv, String dataFile, TemperatureRecord tempRecord, DebugManager debugManager) {
		// save the data file name for later use if user switches country
    	m_dataFile = dataFile;
    	
    	this.dv = dv;
    	this.tempRecord = tempRecord;
    	this.debugManager = debugManager;
	}

	public void loadData() throws FileNotFoundException {
		// reset the data storage in case this is a re-load
	    dv.setDataRaw(new ArrayList<List<Object>>());
	    dv.setDataStates(new TreeSet<String>());
	    dv.setDataCountries(new TreeSet<String>());
	    dv.setDataYears(new TreeSet<Integer>());
	    dv.setPlotData(null);
	    
	    
    	try (Scanner scanner = new Scanner(new File(m_dataFile))) {
    	    boolean skipFirst = true;
    	    while (scanner.hasNextLine()) {
    	    	String line = scanner.nextLine();
    	    	
    	    	if(!skipFirst) {
    	    		List<Object> record = tempRecord.getRecordFromLine(line);
    	    		if(record != null) {
    	    			dv.getDataRaw().add(record);
    	    		}
    	    	}
    	    	else {
    	    		skipFirst = false;
    	    	}
    	    }
    	    // update selections (not including country) for the newly loaded data
            dv.setSelectedState(dv.getDataStates().first());
            dv.setSelectedStartYear(dv.getDataYears().first());
            dv.setSelectedEndYear(dv.getDataYears().last());

            debugManager.info("loaded %d data records", dv.getDataRaw().size());
            debugManager.info("loaded data for %d states", dv.getDataStates().size());
            debugManager.info("loaded data for %d years [%d, %d]", dv.getDataYears().size(), dv.getSelectedStartYear(), 
            		dv.getSelectedEndYear());
    	}
    }
}
