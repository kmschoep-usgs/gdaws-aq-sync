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
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class AqToGdaws {
	private static final Logger LOG = LoggerFactory.getLogger(AqToGdaws.class);
	
	private static final Integer DEFAULT_DAYS_TO_FETCH = 30;
	private final TimeSeriesDAO timeSeriesDao;
		
	private List<SiteConfiguration> sitesToLoad;
	private Integer daysToFetch;
	private Integer sourceId;
	
	private DataService dataService;
	private SiteConfigurationLoader siteConfiguationLoader;

	/**
	 * Constructor that loads its own site configuration and automatically loads data since
	 * the last timestamp.
	 * @param dataService
	 * @param gdawsDaoFactory
	 * @param defaultDaysToFetch 
	 */
	public AqToGdaws(DataService dataService, GdawsDaoFactory gdawsDaoFactory, Integer defaultDaysToFetch, Integer sourceId) {
		siteConfiguationLoader = new SiteConfigurationLoader(gdawsDaoFactory);
		this.sitesToLoad = siteConfiguationLoader.getAllSites();
		this.dataService = dataService;
		this.timeSeriesDao = new TimeSeriesDAO(gdawsDaoFactory);
		this.daysToFetch = defaultDaysToFetch != null ? defaultDaysToFetch : DEFAULT_DAYS_TO_FETCH;
		this.sourceId = sourceId;
	}
	
	public void migrateAqData() {
		fillInAquariusParamNames(sitesToLoad);
		
		for(SiteConfiguration site : sitesToLoad) {
			
			//Temporary hack to only test on one specific site
			
			if(site.getLocalSiteId() != 9402000){
				continue;
			}
			

			if (site.getAqParam() != null) {
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
						
						GdawsTimeSeries toInsert = aqToGdawsTimeSeries(retrieved, site);

						LOG.debug("Created Time Series: (Site)" + toInsert.getSiteId() + " (Group)" + toInsert.getGroupId() + " (Source)" + toInsert.getSourceId() + " with " + numOfPoints + " records.");

						//NOTE: Temporarily disabled until site configuration loading is completed
						timeSeriesDao.insertTimeseriesData(toInsert);
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
		
		long time = System.currentTimeMillis();
		LOG.trace("Starting request for PCode to AqParam mappings. . . .");
		Map<String, String> pCodeMap = dataService.getPcodeToAquariusMap();
		LOG.trace("And the PCode-AqCode request is done.  That was {} minutes", ((System.currentTimeMillis() - time) / 60000));
		
		LOG.debug("Found {}  PCode to Aquarius Name mappings", pCodeMap.size());
		
		pCodeMap.entrySet().stream().filter(p -> p.getKey().equals("00060")).forEach(m -> System.out.println("Found PCode '00060' mapped to AQ '" + m.getValue() + "'"));
		
		
		//Temporary hack to disable loading of pcodes from the service
		//Map<String, String> pCodeMap = new HashMap();
		//pCodeMap.put("00060", "Discharge");
		
		//Fist line does the mapping
		//Second line finds ones where the AqCode is still null and logs them as errors
		sitesToLoad.stream().peek(s -> s.setAqParam(pCodeMap.get(StringUtils.trimToEmpty(s.getPCode())))).
				filter(s -> s.getAqParam() == null).forEach(s -> LOG.error("Unable to map the pCode '{}' to an Aquarius Param Name (PCode not found)", s.getPCode()));
	}
	
	public GdawsTimeSeries aqToGdawsTimeSeries(TimeSeries source, SiteConfiguration site){
		GdawsTimeSeries newSeries = new GdawsTimeSeries();
		
		newSeries.setSiteId(site.getLocalSiteId());
		newSeries.setGroupId(site.getLocalParamId());
		newSeries.setSourceId(this.sourceId);
		
		//Build Points
		List<TimeSeriesRecord> newRecords = new ArrayList<>();
		
		LocalDateTime startTime = null, endTime = null;
		
		for(TimeSeriesPoint point : source.getPoints()){
			newRecords.add(aqToGdawsTimeSeriesPoint(point, site));
			
			if(startTime == null || newRecords.get(newRecords.size()-1).getMeasurementDate().isBefore(startTime)){
				startTime = newRecords.get(newRecords.size()-1).getMeasurementDate();
			}
			
			if(endTime == null || newRecords.get(newRecords.size()-1).getMeasurementDate().isAfter(endTime)){
				endTime = newRecords.get(newRecords.size()-1).getMeasurementDate();
			}
		}
		newSeries.setRecords(newRecords);
		
		newSeries.setStartTime(startTime);
		newSeries.setEndTime(endTime);
		
		return newSeries;
	}
	
	public TimeSeriesRecord aqToGdawsTimeSeriesPoint(TimeSeriesPoint source, SiteConfiguration site){
		TimeSeriesRecord newPoint = new TimeSeriesRecord();
		
		newPoint.setSiteId(site.getLocalSiteId());
		newPoint.setGroupId(site.getLocalParamId());
		//Fix for points with no time
		if(source.getTime().isSupported(ChronoUnit.HOURS)){
			newPoint.setMeasurementDate(LocalDateTime.from(source.getTime()));
		} else {
			LOG.debug("Found point without associated time: " + source.getTime());
			newPoint.setMeasurementDate(((LocalDate)source.getTime()).atStartOfDay());
		}
		newPoint.setFinalValue(source.getValue().doubleValue());
		newPoint.setSourceId(this.sourceId);
		
		//TODO: Apply Qualifiers?
		newPoint.setDataApprovalId(1);
		
		//TODO: Apply Approvals?
		newPoint.setMainQualifierId(1);
		
		//TODO: Other info needed?
		
		return newPoint;
	}
}
