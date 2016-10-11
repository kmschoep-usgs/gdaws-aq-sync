package gov.usgs.wma.gcmrc.service;

import java.util.ArrayList;
import java.util.List;

import gov.usgs.wma.gcmrc.model.SiteConfiguration;

public class GdawsConfigLoader {
	
	public List<SiteConfiguration> loadSiteConfiguration() {
		List<SiteConfiguration> configs = new ArrayList<>();
		
		//TODO load site config from file or database
		configs.add(new SiteConfiguration("Discharge", "01010000", "01010000", 99, 99));
		
		return configs;
	}
}
