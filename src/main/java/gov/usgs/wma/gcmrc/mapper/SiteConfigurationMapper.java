package gov.usgs.wma.gcmrc.mapper;

import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import java.util.List;

/**
 *
 * @author eeverman
 */
public interface SiteConfigurationMapper {
	List<SiteConfiguration> getAll();
}
