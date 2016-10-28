package gov.usgs.wma.gcmrc.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.aqcu.data.service.DataService;
import gov.usgs.aqcu.gson.ISO8601TemporalSerializer;
import gov.usgs.aqcu.model.TimeSeries;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;

public class AqToGdaws {
	private static final Logger LOG = LoggerFactory.getLogger(AqToGdaws.class);
	
	private static final Integer DEFAULT_DAYS_TO_FETCH = 30;
		
	private List<SiteConfiguration> sitesToLoad;
	private Integer daysToFetch;
	
	DataService dataService;

	public AqToGdaws(DataService dataService, SqlSessionFactory sqlSesionFactory, Integer defaultDaysToFetch) {
		SiteConfigurationLoader siteConfiguationLoader = new SiteConfigurationLoader(sqlSesionFactory);
		this.sitesToLoad = siteConfiguationLoader.loadSiteConfiguration();
		this.dataService = dataService;
		
		defaultDaysToFetch = defaultDaysToFetch != null ? defaultDaysToFetch : DEFAULT_DAYS_TO_FETCH;
	}
	
	public void migrateAqData() {
		fillInAquariusParamNames(sitesToLoad);
		
		for(SiteConfiguration site : sitesToLoad) {
			
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
					
			//load the data from the source. TODO, determine if we only use primary/published/UV series 
			List<String> tsUids = dataService.getTimeSeriesUniqueIdsAtSite(remoteSiteId, null, null, site.getAqParam(), null, null);
			
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
			
			//TODO: Update last pulled information
		}
	}
	
	public void fillInAquariusParamNames(List<SiteConfiguration> sitesToLoad) {
		
		long time = System.currentTimeMillis();
		
		LOG.trace("Starting request for PCode to AqParam mappings. . . .");
		Map<String, String> pCodeMap = dataService.getPcodeToAquariusMap();
		LOG.trace("And the PCode-AqCode request is done.  That was {} minutes", ((System.currentTimeMillis() - time) / 60000));
		
		LOG.debug("Found {}  PCode to Aquarius Name mappings", pCodeMap.size());
		
		
		//Fist line does the mapping
		//Second line finds ones where the AqCode is still null and logs them as errors
		sitesToLoad.stream().peek(s -> s.setAqParam(pCodeMap.get(StringUtils.trimToEmpty(s.getPCode())))).
				filter(s -> s.getAqParam() == null).forEach(s -> LOG.error("Unable to map the pCode '{}' to an Aquarius Param Name (PCode not found)", s.getPCode()));
	}
}
