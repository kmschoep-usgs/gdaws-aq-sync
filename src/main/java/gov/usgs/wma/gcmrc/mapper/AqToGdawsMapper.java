package gov.usgs.wma.gcmrc.mapper;

import java.util.List;
import java.util.Map;

/**
 *
 * @author zmoore
 */
public interface AqToGdawsMapper {
	void insertAqTimeseries(Map<String, Object> parms);
}
