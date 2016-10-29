package gov.usgs.wma.gcmrc.service;

import java.util.Map;

import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProcConfigurationLoader.class);
	
	private SqlSessionFactory sessionFactory;
	private AutoProcConfigurationLoader autoProcConfLoader;
	
	public AutoProc(SqlSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		this.autoProcConfLoader = new AutoProcConfigurationLoader(sessionFactory);
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
