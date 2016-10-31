package gov.usgs.wma.gcmrc.dao;

import gov.usgs.wma.gcmrc.mapper.AqToGdawsMapper;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author zmoore
 */
public class AqToGdawsDAO {
	private static final Logger LOG = LoggerFactory.getLogger(SiteConfigurationLoader.class);
			
	private SqlSessionFactory sessionFactory;

	public AqToGdawsDAO(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	public void insertTimeseriesData(GdawsTimeSeries series){
		LOG.debug("Starting insert of timeseries data from AQ");
		
		Map<String, Object> parms = new HashMap<String, Object>();
			parms.put("records", series.getRecords());
			parms.put("sourceId", series.getSourceId());
			parms.put("groupId", series.getGroupId());
			parms.put("siteId", series.getSiteId());
			
		try (SqlSession session = sessionFactory.openSession()) {
			AqToGdawsMapper mapper = session.getMapper(AqToGdawsMapper.class);			
			mapper.insertTimeseriesData(parms);
		}
	}
}
