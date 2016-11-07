package gov.usgs.wma.gcmrc.mapper;

import java.util.List;
import java.util.Map;

import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

/**
 *
 * @author thongsav, zmoore
 */
public interface TimeSeriesMapper {
	List<TimeSeriesRecord> getTimeSeries(Map<String, Object> parms);
	
	//AQ -> GDAWS Sync Methods
	void insertTimeseriesDataToStageTable(TimeSeriesRecord record);
	void deleteOverlappingDataInStarTable(Map<String, Object> parms);
	void copyStageTableToStarTable(Map<String, Object> parms);
	void emptyStageTable(Map<String, Object> parms);	
	void analyzeStageTable(Map<String, Object> parms);
}
