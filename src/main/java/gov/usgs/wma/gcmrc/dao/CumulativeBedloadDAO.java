package gov.usgs.wma.gcmrc.dao;

import gov.usgs.wma.gcmrc.mapper.CumulativeBedloadMapper;
import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesMapper;

public class CumulativeBedloadDAO {
	private static final Logger LOG = LoggerFactory.getLogger(CumulativeBedloadDAO.class);
	
	private SqlSessionFactory sessionFactory;

	public CumulativeBedloadDAO(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	

	/**
	 * 
	 * @param siteId The site that the calc is being done for
	 * @param sourceId The source id, which identifies this calculation process as
	 *		the originator of the calculated data.
	 * @param sourceGroupId The source data group_id, which should be the instantaneous load data series
	 * @param destinationGroupId The destination group_id, which is the data series we are calculating
	 */
	public void calcCumulativeBedloadToStageTable(Integer targetSiteId, Integer sourceSiteId,
			Integer sourceId, Integer sourceGroupId, Integer destinationGroupId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("siteId", targetSiteId);
		params.put("sourceSiteId", sourceSiteId);
		params.put("sourceId", sourceId);
		params.put("sourceGroupId", sourceGroupId);
		params.put("groupId", destinationGroupId);	//for consistency, use 'groupId' as SQL param
		
		
		
		LOG.debug("Calculating cumulative bedload for site {} using instaneous bedload from site {}", targetSiteId, sourceSiteId);
		
		//This does not need to happen in the same transaction
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesMapper timeSeriesMapper = session.getMapper(TimeSeriesMapper.class);	
			timeSeriesMapper.emptyStageTable();	
			session.commit();
		}

		try (SqlSession session = sessionFactory.openSession()) {
			CumulativeBedloadMapper cbmMapper = session.getMapper(CumulativeBedloadMapper.class);
			TimeSeriesMapper timeSeriesMapper = session.getMapper(TimeSeriesMapper.class);	
				
			LOG.trace("Will calculate cumulative bedload");
			
			long time = System.currentTimeMillis();
			cbmMapper.calcCumulativeBedloadToStageTable(params);
			session.flushStatements();
			LOG.trace("Cumulative Calcs took {} seconds for site id {}",
					(System.currentTimeMillis() - time) / 1000, targetSiteId);
			
			
			
			if (LOG.isTraceEnabled()) {
				int count = timeSeriesMapper.getStageCount();
				LOG.trace("{} cumulative bedload records inserted into stage.  Will delete matching from Star.", count);
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
