package gov.usgs.wma.gcmrc.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.mapper.TimeSeriesMapper;
import gov.usgs.wma.gcmrc.mapper.TotalSuspendedSedimentCalcMapper;

public class TotalSuspendedSedimentDAO {
	private static final Logger LOG = LoggerFactory.getLogger(TotalSuspendedSedimentDAO.class);
	
	private SqlSessionFactory sessionFactory;

	public TotalSuspendedSedimentDAO(GdawsDaoFactory gdawsDaoFactory) {
		this.sessionFactory = gdawsDaoFactory.getSqlSessionFactory();
	}
	

	/**
	 * 
	 * @param targetSiteIds The sites that the calc is being done for
	 * @param finesGroupId The group id which is the acoustic suspended silt and clay data series
	 * @param sandGroupId The group id which is the acoustic suspended sand data series
	 * @param totalSuspSedGroupId The group id to assign the new total suspended sediment calculation to
	 * @param sourceId The source id, which identifies this calculation process as
	 *		the originator of the calculated data.
	 */
	public void calcTotalSuspendedSedimentToStageTable(List<Integer> targetSiteIds, Integer finesGroupId,
			Integer sandGroupId, Integer totalSuspSedGroupId, Integer sourceId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("siteIds", targetSiteIds);
		params.put("finesGroupId", finesGroupId);
		params.put("sandGroupId", sandGroupId);
		params.put("groupId", totalSuspSedGroupId);
		params.put("sourceId", sourceId);
		
		LOG.debug("Calculating total suspended sediment for sites {}", targetSiteIds);
		
		//This does not need to happen in the same transaction
		try (SqlSession session = sessionFactory.openSession()) {
			TimeSeriesMapper timeSeriesMapper = session.getMapper(TimeSeriesMapper.class);	
			timeSeriesMapper.emptyStageTable();	
			session.commit();
		}

		try (SqlSession session = sessionFactory.openSession()) {
			TotalSuspendedSedimentCalcMapper tsscMapper = session.getMapper(TotalSuspendedSedimentCalcMapper.class);
			TimeSeriesMapper timeSeriesMapper = session.getMapper(TimeSeriesMapper.class);	
				
			LOG.trace("Will calculate total suspended sediment");
			
			long time = System.currentTimeMillis();
			tsscMapper.calcTotalSuspendedSedimentToStageTable(params);
			session.flushStatements();
			LOG.trace("Total suspended sediment calcs took {} seconds for site ids {}",
					(System.currentTimeMillis() - time) / 1000, targetSiteIds);
			
			
			
			if (LOG.isTraceEnabled()) {
				int count = timeSeriesMapper.getStageCount();
				LOG.trace("{} total suspended sediment records inserted into stage.  Will delete matching from Star.", count);
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
