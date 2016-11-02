package gov.usgs.wma.gcmrc.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.aqcu.data.service.DataService;
import gov.usgs.aqcu.gson.ISO8601TemporalSerializer;
import gov.usgs.aqcu.model.TimeSeries;
import gov.usgs.aqcu.model.TimeSeriesPoint;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.SiteConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.AqToGdawsDAO;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

public class AqToGdaws {
	private static final Logger LOG = LoggerFactory.getLogger(AqToGdaws.class);
	
	private static final Integer DEFAULT_DAYS_TO_FETCH = 30;
	private final AqToGdawsDAO aqToGdawsDao;
		
	private List<SiteConfiguration> sitesToLoad;
	private Integer daysToFetch;
	
	private DataService dataService;
	private SiteConfigurationLoader siteConfiguationLoader;

	
	/**
	 * Constructor that loads its own site configuration and automatically loads data since
	 * the last timestamp.
	 * @param dataService
	 * @param gdawsDaoFactory
	 * @param defaultDaysToFetch 
	 */
	public AqToGdaws(DataService dataService, GdawsDaoFactory gdawsDaoFactory, Integer defaultDaysToFetch) {
<<<<<<< HEAD
		SiteConfigurationLoader siteConfiguationLoader = new SiteConfigurationLoader(gdawsDaoFactory);
		this.aqToGdawsDao = new AqToGdawsDAO(gdawsDaoFactory);
		this.sitesToLoad = siteConfiguationLoader.loadSiteConfiguration();
=======
		siteConfiguationLoader = new SiteConfigurationLoader(gdawsDaoFactory);
		this.sitesToLoad = siteConfiguationLoader.getAllSites();
>>>>>>> e37ab49ceef5620d2b9b5a96ab3397bc6394826a
		this.dataService = dataService;
		
		this.daysToFetch = defaultDaysToFetch != null ? defaultDaysToFetch : DEFAULT_DAYS_TO_FETCH;
	}
	
	public void migrateAqData() {
		fillInAquariusParamNames(sitesToLoad);
				
		for(SiteConfiguration site : sitesToLoad) {
<<<<<<< HEAD
			if(site.getLocalSiteId() != 9402000) {
				continue;
			}
			ZonedDateTime startTime = null;
			ZonedDateTime endTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
							
			if (site.getLastNewPullStart() != null && site.getLastNewPullEnd() != null) {
				//Pull data from since the last pull until now.
				//Move the start time back a second, since we round to the nearest second.
				startTime = site.getLastNewPullEnd().truncatedTo(ChronoUnit.SECONDS).minusSeconds(1);
			} else {
				startTime = endTime.minusDays(daysToFetch);
			}
			
			LOG.debug("Pulling data for site {}, parameter {} for the date range starting {} to {}", 
					site.getLocalSiteId(), site.getPCode(), 
					DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime), 
					DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime));
			
			
			//Long siteId = c.getSiteId();
			String remoteSiteId = site.getRemoteSiteId();

			//Only pull published timeseries
			List<String> tsUids = dataService.getTimeSeriesUniqueIdsAtSite(remoteSiteId, true, null, site.getAqParam(), null, null);
			
			for(String uid: tsUids) {
				TimeSeries retrieved = dataService.getTimeSeriesData(
						remoteSiteId, uid, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime),
						DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime), false, false);
								
				Integer numOfPoints = retrieved.getPoints().size();
				LOG.debug("Retrieved " + retrieved.getName() + " " + retrieved.getDescription() + 
						", which contains " + numOfPoints + " points");
				if(numOfPoints > 0) {
					LOG.trace("First point: " + 
							ISO8601TemporalSerializer.print(retrieved.getPoints().get(0).getTime()) + 
							" " + retrieved.getPoints().get(0).getValue());
					
					GdawsTimeSeries toInsert = aqToGdawsTimeSeries(retrieved, site, startTime, endTime);

					LOG.debug("Created Time Series: (Site)" + toInsert.getSiteId() + " (Group)" + toInsert.getGroupId() + " (Source)" + toInsert.getSourceId() + " with " + numOfPoints + " records.");

					//NOTE: Temporarily disabled until site configuration loading is completed
					//aqToGdawsDao.insertTimeseriesData(toInsert);
=======

			if (site.getAqParam() != null) {
				ZonedDateTime startTime = null;
				ZonedDateTime endTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);

				if (site.getLastNewPullStart() != null && site.getLastNewPullEnd() != null) {
					//Pull data from since the last pull until now.
					//Move the start time back a second, since we round to the nearest second.
					startTime = site.getLastNewPullEnd().truncatedTo(ChronoUnit.SECONDS).minusSeconds(1);
				} else {
					startTime = endTime.minusDays(daysToFetch);
>>>>>>> e37ab49ceef5620d2b9b5a96ab3397bc6394826a
				}

				LOG.debug("Pulling data for site {}, parameter {} for the date range starting {} to {}", 
						site.getLocalSiteId(), site.getPCode(), 
						DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime), 
						DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime));


				//Long siteId = c.getSiteId();
				String remoteSiteId = site.getRemoteSiteId();

				//Only pull published timeseries
				List<String> tsUids = dataService.getTimeSeriesUniqueIdsAtSite(remoteSiteId, true, null, site.getAqParam(), null, null);

				//Hack to just load one site (if you want that)
				/*
				if (tsUids.size() > 1) {
					String oneId = tsUids.get(0);
					tsUids.clear();
					tsUids.add(oneId);
				}
				*/

				for(String uid: tsUids) {
					TimeSeries retrieved = dataService.getTimeSeriesData(
							remoteSiteId, uid, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime),
							DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime), false, false);

					//TODO transform and load into GDAWS
					Integer numOfPoints = retrieved.getPoints().size();
					LOG.trace("Retrieved " + retrieved.getName() + " " + retrieved.getDescription() + 
							", which contains " + numOfPoints + " points");
					if(numOfPoints > 0) {
						LOG.trace("First point: " + 
								ISO8601TemporalSerializer.print(retrieved.getPoints().get(0).getTime()) + 
								" " + retrieved.getPoints().get(0).getValue());
					}
				}

				//Update the site w/ a new timestamp of the last pull
				site.setLastNewPullStart(startTime);
				site.setLastNewPullEnd(endTime);
				siteConfiguationLoader.updateNewDataPullTimestamps(site);
			}
		}
	}
	
	/**
	 * This is badly hacked to deal w/ the fact that the test services is very
	 * slow and e are not going to continue using the PCode mapping.
	 * @param sitesToLoad 
	 */
	public void fillInAquariusParamNames(List<SiteConfiguration> sitesToLoad) {
		
//		long time = System.currentTimeMillis();
//		LOG.trace("Starting request for PCode to AqParam mappings. . . .");
//		Map<String, String> pCodeMap = dataService.getPcodeToAquariusMap();
//		LOG.trace("And the PCode-AqCode request is done.  That was {} minutes", ((System.currentTimeMillis() - time) / 60000));
//		
//		LOG.debug("Found {}  PCode to Aquarius Name mappings", pCodeMap.size());
//		
//		pCodeMap.entrySet().stream().filter(p -> p.getKey().equals("00060")).forEach(m -> System.out.println("Found PCode '00060' mapped to AQ '" + m.getValue() + "'"));
//		

		Map<String, String> pCodeMap = new HashMap();
		pCodeMap.put("00060", "Discharge");
		
		//Fist line does the mapping
		//Second line finds ones where the AqCode is still null and logs them as errors
		sitesToLoad.stream().peek(s -> s.setAqParam(pCodeMap.get(StringUtils.trimToEmpty(s.getPCode())))).
				filter(s -> s.getAqParam() == null).forEach(s -> LOG.error("Unable to map the pCode '{}' to an Aquarius Param Name (PCode not found)", s.getPCode()));
	}
	
	public GdawsTimeSeries aqToGdawsTimeSeries(TimeSeries source, SiteConfiguration site, ZonedDateTime startTime, ZonedDateTime endTime){
		GdawsTimeSeries newSeries = new GdawsTimeSeries();
		
		newSeries.setSiteId(site.getLocalSiteId());
		newSeries.setGroupId(site.getLocalParamId());
		newSeries.setStartTime(Instant.from(startTime));
		newSeries.setEndTime(Instant.from(endTime));
		
		//TODO: SourceId?
		newSeries.setSourceId(67);
		
		//Build Points
		List<TimeSeriesRecord> newRecords = new ArrayList<>();
		for(TimeSeriesPoint point : source.getPoints()){
			newRecords.add(aqToGdawsTimeSeriesPoint(point, site));
		}
		newSeries.setRecords(newRecords);
		
		return newSeries;
	}
	
	public TimeSeriesRecord aqToGdawsTimeSeriesPoint(TimeSeriesPoint source, SiteConfiguration site){
		TimeSeriesRecord newPoint = new TimeSeriesRecord();
		
		newPoint.setSiteId(site.getLocalSiteId());
		newPoint.setGroupId(site.getLocalParamId());
		//Fix for points with no time
		if(source.getTime().isSupported(ChronoUnit.HOURS)){
			newPoint.setMeasurementDate(Instant.from(source.getTime()));
		} else {
			LOG.debug("Found point without associated time: " + source.getTime());
			newPoint.setMeasurementDate(((LocalDate)source.getTime()).atStartOfDay().toInstant(ZoneOffset.UTC));
		}
		newPoint.setFinalValue(source.getValue());
		
		//TODO: SourceId?
		newPoint.setSourceId(67);
		
		//TODO: Apply Qualifiers?
		newPoint.setDataApprovalId(1);
		
		//TODO: Apply Approvals?
		newPoint.setMainQualifierId(1);
		
		//TODO: Other info needed?
		
		return newPoint;
	}
}
