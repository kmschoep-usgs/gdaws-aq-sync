package gov.usgs.wma.gcmrc.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.SiteConfigurationMapper;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;

public class SiteConfigurationLoader {
	private static final Logger LOG = LoggerFactory.getLogger(SiteConfigurationLoader.class);
			
	private SqlSessionFactory sessionFactory;

	public SiteConfigurationLoader(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	public List<SiteConfiguration> loadSiteConfiguration() {
		LOG.debug("Loading site configuration");
		List<SiteConfiguration> sitesToLoad = null;
		
		try (SqlSession session = sessionFactory.openSession()) {
			SiteConfigurationMapper mapper = session.getMapper(SiteConfigurationMapper.class);
			sitesToLoad = mapper.getAll();
		}
		
		return sitesToLoad;
	}
}
