package gov.usgs.wma.gcmrc.dao;

import gov.usgs.wma.gcmrc.mapper.CumulativeSandLoadMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesMapper;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class CumulativeSandLoadDAO {
	private static final Logger LOG = LoggerFactory.getLogger(CumulativeSandLoadDAO.class);
	
	private SqlSessionFactory sessionFactory;

	public CumulativeSandLoadDAO(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	

	/**
	 * 
	 * @param siteId The old Dinosaur-Dinosaur site that will be unioned with cumulative sand load calcs from new site
	 * @param newSiteId The new Dinosaur-Dinosaur site that will be unioned with cumulative sand load calcs from old site
	 * @param sourceId The source id, which identifies this calculation process as
	 *		the originator of the calculated data.
	 * @param groupId The group_id, which should be the cumulative sand load data series
	 * @param lastTimeStamp the last timestamp of the old site that has the final cumulative sand load value when the data are switched to new site
	 * @param firstTimeStamp the first timestamp of the new site that cumulative sand loads are calculated
	 */
	public void calcCumulativeSandLoadToStageTable(Integer siteId, Integer newSiteId,
			Integer sourceId, Integer groupId, String lastTimestamp, String firstTimestamp) {
		List<TimeSeriesRecord> timeSeries = null;

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("siteId", siteId);
		params.put("newSiteId", newSiteId);
		params.put("sourceId", sourceId);
		params.put("groupId", groupId);	//for consistency, use 'groupId' as SQL param
		params.put("lastTimestamp", lastTimestamp);
		params.put("firstTimestamp", firstTimestamp);
		
		
		
		LOG.debug("Merging cumulative sand load for sites {} (old site) and {} (new site)", siteId, newSiteId);
		
		//This does not need to happen in the same transaction
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesMapper timeSeriesMapper = session.getMapper(TimeSeriesMapper.class);	
			timeSeriesMapper.emptyStageTable();	
		}

		try (SqlSession session = sessionFactory.openSession()) {
			CumulativeSandLoadMapper cbmMapper = session.getMapper(CumulativeSandLoadMapper.class);
			TimeSeriesMapper timeSeriesMapper = session.getMapper(TimeSeriesMapper.class);	
				
			LOG.trace("Will merge cumulative sand load");
			
			long time = System.currentTimeMillis();
			cbmMapper.calcCumulativeSandLoadToStageTable(params);
			session.flushStatements();
			LOG.trace("Merging Calcs took {} seconds",
					(System.currentTimeMillis() - time) / 1000);
			
			
			
			if (LOG.isTraceEnabled()) {
				int count = timeSeriesMapper.getStageCount();
				LOG.trace("{} newly-merged cumulative sand load records inserted into stage.  Will delete matching from Star.", count);
			}
			
			timeSeriesMapper.deleteOverlappingDataInStarTable(params);
			session.flushStatements();
			LOG.trace("Will analyze stage");
			timeSeriesMapper.analyzeStageTable();
			LOG.trace("Will copy from stage to star");
			timeSeriesMapper.copyStageTableToStarTable(params);
			LOG.trace("Will Commit");
			session.commit();
			LOG.trace("Commit Complete.");
		}
		

	}
	
}
