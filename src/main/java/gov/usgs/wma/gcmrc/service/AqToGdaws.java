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
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.SiteConfigurationLoader;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import java.util.HashMap;

public class AqToGdaws {
	private static final Logger LOG = LoggerFactory.getLogger(AqToGdaws.class);
	
	private static final Integer DEFAULT_DAYS_TO_FETCH = 30;
		
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
		
		this.daysToFetch = defaultDaysToFetch != null ? defaultDaysToFetch : DEFAULT_DAYS_TO_FETCH;
		this.sourceId = sourceId;
	}
	
	public void migrateAqData() {
		fillInAquariusParamNames(sitesToLoad);
		
		for(SiteConfiguration site : sitesToLoad) {

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
}
