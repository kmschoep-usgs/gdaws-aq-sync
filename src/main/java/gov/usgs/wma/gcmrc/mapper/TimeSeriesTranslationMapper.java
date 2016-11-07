package gov.usgs.wma.gcmrc.mapper;

import java.util.Map;

/**
 *
 * @author zmoore
 */
public interface TimeSeriesTranslationMapper {
	Map<Number, Number> getAqGdawsApprovalMap(Map<String, Object> parms);
	Map<String, Number> getAqGdawsQualifierMap(Map<String, Object> parms);
}
