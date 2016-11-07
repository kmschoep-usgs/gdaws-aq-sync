package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;

/**
 *
 * @author zmoore
 */
public interface TimeSeriesTranslationMapper {
	Map<Integer, Integer> getAqGdawsApprovalMap(Map<String, Object> parms);
	Map<String, Integer> getAqGdawsQualifierMap(Map<String, Object> parms);
}
