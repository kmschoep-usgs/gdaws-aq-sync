package gov.usgs.wma.gcmrc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.AutoProcConfigurationMapper;
import gov.usgs.wma.gcmrc.model.AutoProcConfiguration;

public class AutoProcConfigurationLoader {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProcConfigurationLoader.class);
	
	private SqlSessionFactory sessionFactory;

	private static final String BEDLOAD_CALC_NAME = "bedLoadCalc";
	
	public AutoProcConfigurationLoader(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	public List<AutoProcConfiguration> loadBedLoadCalculationConfiguration() {
		List<AutoProcConfiguration> sitesToLoad = null;
		
		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put("loadCalculationName", BEDLOAD_CALC_NAME);
		
		LOG.debug("Loading calculation configuration for {}", BEDLOAD_CALC_NAME);
		try (SqlSession session = sessionFactory.openSession()) {
			AutoProcConfigurationMapper mapper = session.getMapper(AutoProcConfigurationMapper.class);
			sitesToLoad = mapper.getByLoadCalculationName(parms);
		}
		
		return sitesToLoad;
	}
	
	public Map<Long, Map<String, Double>> asParamMap(List<AutoProcConfiguration> list) {
		Map<Long, Map<String, Double>> paramMap = new HashMap<>();
		
		for(AutoProcConfiguration c : list) {
			Long siteId = c.getSiteId();
			Map<String, Double> mapForSite = paramMap.get(siteId);
			if(mapForSite == null) {
				mapForSite = new HashMap<>();
				paramMap.put(siteId, mapForSite);
			}
			LOG.trace("Found calc parameter {} {} {}", siteId, c.getConfigParamaterName(), c.getConfigParamaterValue());
			mapForSite.put(c.getConfigParamaterName(), c.getConfigParamaterValue());
		}
		
		return paramMap;
	}
}

