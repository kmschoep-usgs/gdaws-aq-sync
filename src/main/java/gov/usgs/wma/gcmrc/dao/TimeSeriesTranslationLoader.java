package gov.usgs.wma.gcmrc.dao;

import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesTranslationMapper;
import java.util.HashMap;

public class TimeSeriesTranslationLoader {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesTranslationLoader.class);
			
	private SqlSessionFactory sessionFactory;

	public TimeSeriesTranslationLoader(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	public Map<Integer, Integer> getAqGdawsApprovalMap(){
		LOG.debug("Loading AQ -> GDAWS Approval Mapping");
		Map<Integer, Integer> approvalMap = null;
		
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesTranslationMapper mapper = session.getMapper(TimeSeriesTranslationMapper.class);
			approvalMap = mapper.getAqGdawsApprovalMap(new HashMap<>());
		}
		
		return approvalMap;
	}
	
	public Map<String, Integer> getAqGdawsQualifierMap(){
		LOG.debug("Loading AQ -> GDAWS Qualifier Mapping");
		Map<String, Integer> qualifierMap = null;
		
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesTranslationMapper mapper = session.getMapper(TimeSeriesTranslationMapper.class);
			qualifierMap = mapper.getAqGdawsQualifierMap(new HashMap<>());
		}
		
		return qualifierMap;
	}
}
