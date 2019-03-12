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
	
	public List<TimeSeriesRecord> getTimeSeries(Integer siteId, String groupName) {
		return getTimeSeries(siteId, groupName, true);
	}
	
	public List<TimeSeriesRecord> getTimeSeries(Integer siteId, String groupName, Boolean excludeDummyValues) {
		List<TimeSeriesRecord> timeSeries = null;

		Map<String, Object> parms = new HashMap<String, Object>();
		parms.put("siteId", siteId);
		parms.put("groupName", groupName);
		
		if(excludeDummyValues) {
			parms.put("excludeDummyValues", true);
		}
		
		LOG.debug("Loading time series for site {} and group {}", siteId, groupName);
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesMapper mapper = session.getMapper(TimeSeriesMapper.class);
			timeSeries = mapper.getTimeSeries(parms);
		}
		
		return timeSeries;
	}
	
	/**
	 * Multi-step update from TIME_SERIES_AP_STAGE TO TIME_SERIES_STAR.
	 * 
	 * Please review the code carefully before using this method - it has lots of
	 * side effects.
	 * 
	 * @param series The new series of data to insert
	 * @param oldSourceId A legacy source id for which it is OK to replace data
	 *		for if it overlaps the new data.  OK to leave as null if there is no
	 *		legacy data for this series.
	 */
	public void insertTimeseriesData(GdawsTimeSeries series, Integer oldSourceId) {
		if(series.getRecords().size() > 0) {
			Map<String, Object> parms = new HashMap<String, Object>();
				parms.put("records", series.getRecords());
				parms.put("sourceId", series.getSourceId());
				parms.put("oldSourceId", oldSourceId);
				parms.put("groupId", series.getGroupId());
				parms.put("siteId", series.getSiteId());
				parms.put("startTime", series.getStartTime());
				parms.put("endTime", series.getEndTime());

			LOG.trace("Starting GDAWS insert of " + series.getRecords().size() + " points from AQ for (Site)" + series.getSiteId() + " (Group)" + series.getGroupId() + " (Source)" + series.getSourceId());
			long startTime = System.nanoTime();

			try (SqlSession session = sessionFactory.openSession()) {
				TimeSeriesMapper mapper = session.getMapper(TimeSeriesMapper.class);	
				
				LOG.trace("Emptying the staging table...");
				mapper.emptyStageTable();	
				
				LOG.trace("Inserting timeseries points...");
				for(TimeSeriesRecord r : series.getRecords()) {
					mapper.insertTimeseriesDataToStageTable(r);
				}
				session.flushStatements();
				
				LOG.trace("Deleting overlapping data...");
				mapper.deleteOverlappingDataInStarTable(parms);
				session.flushStatements();
				
				//Is this still necessary?
				LOG.trace("Copying data from staging table to star table...");
				mapper.analyzeStageTable();
				mapper.copyStageTableToStarTable(parms);
				mapper.emptyStageTable();
				
				LOG.trace("Commiting timeseries...");
				session.commit();
			} catch (Exception e) {
				LOG.error("Error during insert process for (Site)" + series.getSiteId() + " (Group)" + series.getGroupId() + " (Source)" + series.getSourceId() + ": ", e);
			}
			long durationMs = (System.nanoTime() - startTime)/1000000;
			LOG.trace("Finished GDAWS insert of " + series.getRecords().size() + " points for (Site)" + series.getSiteId() + " (Group)" + series.getGroupId() + " (Source)" + series.getSourceId() + " in " + durationMs + "ms");
		} else {
			LOG.trace("Retrieved no points for (Site)" + series.getSiteId() + " (Group)" + series.getGroupId() + " (Source)" + series.getSourceId() + " so skipping insert to GDAWS.");
		}
	}
}
