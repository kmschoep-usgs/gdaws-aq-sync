package gov.usgs.wma.gcmrc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesMapper;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class TimeSeriesDAO {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDAO.class);
	
	private SqlSessionFactory sessionFactory;

	public TimeSeriesDAO(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	
	//TODO Mybatis is mishandling the timestamps (which have no timezone) and shifting all datetimes to local timezone (EG: by -5 if running code in central timezone)
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
	
	public void insertTimeseriesData(GdawsTimeSeries series){
		LOG.debug("Starting insert of timeseries data from AQ");
		
		Map<String, Object> parms = new HashMap<String, Object>();
			parms.put("records", series.getRecords());
			parms.put("sourceId", series.getSourceId());
			parms.put("groupId", series.getGroupId());
			parms.put("siteId", series.getSiteId());
			parms.put("startTime", series.getStartTime());
			parms.put("endTime", series.getEndTime());
		
		if(series.getRecords().size() > 0){
			try (SqlSession session = sessionFactory.openSession()) {
				TimeSeriesMapper mapper = session.getMapper(TimeSeriesMapper.class);	
				mapper.emptyStageTable(parms);	
				mapper.insertTimeseriesDataToStageTable(parms);
				session.commit();
				mapper.deleteOverlappingDataInStarTable(parms);
				//Is this still necessary?
				//mapper.analyzeTable(parms);
				mapper.copyStageTableToStarTable(parms);
				mapper.emptyStageTable(parms);
				session.commit();
			}
		}
	}
}
