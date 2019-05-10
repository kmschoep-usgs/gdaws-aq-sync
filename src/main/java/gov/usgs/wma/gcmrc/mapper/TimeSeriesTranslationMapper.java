package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;
import java.util.List;

/**
 *
 * @author zmoore
 */
public interface TimeSeriesTranslationMapper {
	List<Map<String, Integer>> getAqGdawsApprovalMap(Map<String, Object> parms);
	List<Map<String, Object>> getAqGdawsQualifierMap(Map<String, Object> parms);
}
