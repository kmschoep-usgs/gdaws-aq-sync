package gov.usgs.wma.gcmrc.service;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import gov.usgs.wma.gcmrc.mapper.SiteConfigurationMapper;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;

public class SiteConfigurationLoader {
	private SqlSessionFactory sessionFactory;

	public SiteConfigurationLoader(SqlSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public List<SiteConfiguration> loadSiteConfiguration() {
		List<SiteConfiguration> sitesToLoad = null;
		
		try (SqlSession session = sessionFactory.openSession()) {
			SiteConfigurationMapper mapper = session.getMapper(SiteConfigurationMapper.class);
			sitesToLoad = mapper.getAll();
		}
		
		return sitesToLoad;
	}
}
