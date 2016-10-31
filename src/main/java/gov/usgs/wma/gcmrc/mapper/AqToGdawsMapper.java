package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;

/**
 *
 * @author zmoore
 */
public interface AqToGdawsMapper {
	void insertTimeseriesData(Map<String, Object> parms);
}
