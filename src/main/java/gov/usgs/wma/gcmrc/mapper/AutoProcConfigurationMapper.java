package gov.usgs.wma.gcmrc.mapper;

import java.util.List;
import java.util.Map;

import gov.usgs.wma.gcmrc.model.AutoProcConfiguration;

/**
 *
 * @author thongsav
 */
public interface AutoProcConfigurationMapper {
	List<AutoProcConfiguration> getByLoadCalculationName(Map<String, Object> parms);
}
