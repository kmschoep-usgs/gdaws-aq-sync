package gov.usgs.wma.gcmrc.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProcConfigurationLoader.class);
	
	private GdawsDaoFactory gdawsDaoFactory;
	private AutoProcConfigurationLoader autoProcConfLoader;
	
	public AutoProc(GdawsDaoFactory gdawsDaoFactory) {
		this.gdawsDaoFactory = gdawsDaoFactory;
		this.autoProcConfLoader = new AutoProcConfigurationLoader(gdawsDaoFactory);
	}
	
	public void processBedloadCalculations() {
		Map<Long, Map<String, Double>> bedLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadBedLoadCalculationConfiguration());
		
		for(Long siteId : bedLoadParams.keySet()) {
			//TODO load all data for this site, then use c1 and c2 to perform bedload calculations and insert into database
			LOG.debug("HUZZAH SITE {} C1 {} C2 {}", siteId, bedLoadParams.get(siteId).get("c1"), bedLoadParams.get(siteId).get("c2"));
		}
	}
}
