package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;

/**
 *
 * @author zmoore
 */
public interface AqToGdawsMapper {
	void insertTimeseriesDataToStage(Map<String, Object> parms);
	void deleteOverlappingData(Map<String, Object> parms);
	void copyStageToMain(Map<String, Object> parms);
	void emptyStage(Map<String, Object> parms);	
	void analyzeTable(Map<String, Object> parms);
}
