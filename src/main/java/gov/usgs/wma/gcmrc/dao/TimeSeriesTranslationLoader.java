package gov.usgs.wma.gcmrc.dao;

import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesTranslationMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class TimeSeriesTranslationLoader {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesTranslationLoader.class);
			
	private SqlSessionFactory sessionFactory;

	public TimeSeriesTranslationLoader(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	public Map<Integer, Integer> getAqGdawsApprovalMap(){
		LOG.debug("Loading AQ -> GDAWS Approval Mapping");
		List<Map<String, Integer>> approvalMap = null;
		Map<Integer, Integer> returnMap = null;
		
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesTranslationMapper mapper = session.getMapper(TimeSeriesTranslationMapper.class);
			approvalMap = mapper.getAqGdawsApprovalMap(new HashMap<>());
			returnMap = new HashMap<>();
			for(Map<String, Integer> entry : approvalMap) {
				
				returnMap.put(((Number)entry.get("aq_approval_level")).intValue(), ((Number)entry.get("data_approval_id")).intValue());
			}
			
			LOG.trace("Loaded " + returnMap.entrySet().size() + " approval mappings.");
		}
		
		return returnMap;
	}
	
	public Map<String, Integer> getAqGdawsQualifierMap(){
		LOG.debug("Loading AQ -> GDAWS Qualifier Mapping");
		List<Map<String, Object>> qualifierMapList = null;
		Map<String, Integer> returnMap;
		
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesTranslationMapper mapper = session.getMapper(TimeSeriesTranslationMapper.class);
			qualifierMapList = mapper.getAqGdawsQualifierMap(new HashMap<>());
			returnMap = new HashMap<>();
			
			for(Map<String, Object> entry : qualifierMapList) {
				returnMap.put((String)entry.get("aq_qualifier_identifier"), ((Number)entry.get("measurement_qualifier_id")).intValue());
			}
			
			LOG.trace("Loaded " + returnMap.entrySet().size() + " qualifier mappings.");
		}
		
		return returnMap;
	}
}
