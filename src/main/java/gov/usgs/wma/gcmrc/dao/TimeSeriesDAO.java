package gov.usgs.wma.gcmrc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesMapper;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class TimeSeriesDAO {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDAO.class);
	
	private SqlSessionFactory sessionFactory;

	public TimeSeriesDAO(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	public List<TimeSeriesRecord> getTimeSeries(Integer siteId, String groupName) {
		List<TimeSeriesRecord> timeSeries = null;

		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put("siteId", siteId);
		parms.put("groupName", groupName);
		
		LOG.debug("Loading time series for site {} and group {}", siteId, groupName);
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesMapper mapper = session.getMapper(TimeSeriesMapper.class);
			timeSeries = mapper.getTimeSeries(parms);
		}
		
		return timeSeries;
	}
}
