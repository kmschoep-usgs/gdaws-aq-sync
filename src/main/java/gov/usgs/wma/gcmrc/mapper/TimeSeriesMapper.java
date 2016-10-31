package gov.usgs.wma.gcmrc.mapper;

import java.util.List;
import java.util.Map;

import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

/**
 *
 * @author thongsav
 */
public interface TimeSeriesMapper {
	List<TimeSeriesRecord> getTimeSeries(Map<String, Object> parms);
}
