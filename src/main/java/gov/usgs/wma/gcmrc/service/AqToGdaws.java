package gov.usgs.wma.gcmrc.service;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Approval;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.aqcu.gson.ISO8601TemporalSerializer;

import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.SiteConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.dao.TimeSeriesTranslationLoader;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import gov.usgs.wma.gcmrc.util.TimeSeriesUtils;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class AqToGdaws {
	private static final Logger LOG = LoggerFactory.getLogger(AqToGdaws.class);
	
	private static final Integer DEFAULT_DAYS_TO_FETCH_FOR_NEW_TIMESERIES = 30;
	//From ICE_AFFECTED_STAR: 1 - TRUE, 2 - FALSE
	private static final Integer ICE_AFFECTED_ID = 1;
	private final TimeSeriesDAO timeSeriesDao;
	private final TimeSeriesTranslationLoader timeSeriesTranslationLoader;
		
	private List<SiteConfiguration> sitesToLoad;
	private Map<Integer, Integer> aqGdawsApprovalMap;
	private Map<String, Integer> aqGdawsQualifierMap;
	private Integer sourceId;
	
	private TimeSeriesDataCorrectedService timeSeriesDataCorrectedService;
	private SiteConfigurationLoader siteConfiguationLoader;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;
	private final Integer oldSourceId;
	private final ArrayList<String> tsToPullList;
	private Boolean doUpdateLastPullTime;

	/**
	 * Constructor that loads its own site configuration and automatically loads data since
	 * the last timestamp.
	 * 
	 * Records that are within the data range and have either sourceID or OldSourceId
	 * as their source can be overwritten by the process.
	 * 
	 * @param dataService
	 * @param gdawsDaoFactory
	 * @param daysToFetchForNewTimeseries
	 * @param sourceId Source ID for new records that are written
	 * @param oldSourceId Source ID for legacy records that may be overwritten
	 * @param startTime Optional time to start data pull at
	 * @param endTime Optional time to end data pull at
	 * @param tsToPullList Optional list of timeseries GUIDs to limit the pull to 
	 */
	public AqToGdaws(GdawsDaoFactory gdawsDaoFactory, 
			Integer daysToFetchForNewTimeseries, Integer sourceId, Integer oldSourceId, 
			LocalDateTime startTime, LocalDateTime endTime, ArrayList<String> tsToPullList, TimeSeriesDataCorrectedService timeSeriesDataCorrectedService) {
		siteConfiguationLoader = new SiteConfigurationLoader(gdawsDaoFactory);
		this.sitesToLoad = siteConfiguationLoader.getAllSites();
		this.timeSeriesTranslationLoader = new TimeSeriesTranslationLoader(gdawsDaoFactory);
		this.aqGdawsApprovalMap = this.timeSeriesTranslationLoader.getAqGdawsApprovalMap();
		this.aqGdawsQualifierMap = this.timeSeriesTranslationLoader.getAqGdawsQualifierMap();
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.timeSeriesDao = new TimeSeriesDAO(gdawsDaoFactory);
		this.sourceId = sourceId;
		this.oldSourceId = oldSourceId;
		this.startTime = TimeSeriesUtils.getAsMstDateTime(startTime);
		this.endTime = TimeSeriesUtils.getAsMstDateTime(endTime);
		this.tsToPullList = tsToPullList;
		
		//If the user is providing any part of a date range to pull then we should NOT update the last pull dates
		this.doUpdateLastPullTime = startTime == null && endTime == null;
	}
	
	public void migrateAqData() {
		ZonedDateTime siteStartTime;
		ZonedDateTime siteEndTime;
		for(SiteConfiguration site : sitesToLoad) {
			siteStartTime = null;
			siteEndTime = null;
			if (site.getRemoteParamId() != null && (tsToPullList.isEmpty() || tsToPullList.contains(site.getRemoteParamId()))) {	
				if(endTime == null){
					siteEndTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
				}
								
				//Further constrain the pull times by the 'never before' and 'never after' bounds
				if (site.getNeverPullBefore() != null) {
					siteStartTime = site.getNeverPullBefore();
				}
				
				if (site.getNeverPullAfter() != null && siteEndTime.isAfter(site.getNeverPullAfter())) {
					siteEndTime = site.getNeverPullAfter();
				}
				
				if (siteStartTime.isBefore(siteEndTime)) {
				

					LOG.debug("Pulling data for site {}, parameter {} for the date range starting {} to {}", 
							site.getLocalSiteId(), site.getPCode(), 
							DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(siteStartTime), 
							DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(siteEndTime));
					
					TimeSeriesDataServiceResponse response = null;
					try {
					response = timeSeriesDataCorrectedService.getRawResponse(
									site.getRemoteParamId(), 
									siteStartTime.toInstant(), 
									siteEndTime.toInstant());
					} catch(Exception e){
						LOG.error("Error parsing data from Aquarius: " + e);
					}
					
					Integer numOfPoints = Integer.valueOf(response.getNumPoints().toString());
					LOG.trace("Retrieved " + site.getRemoteParamId() + " From Aquarius" + 
							", which contains " + numOfPoints + " points");
					
					if(numOfPoints > 0) {
						LOG.trace("First point: " +
								ISO8601TemporalSerializer.print(response.getPoints().get(0).getTimestamp().getDateTimeOffset().atOffset(siteStartTime.getOffset())) + 
								" " + response.getPoints().get(0).getValue().getDisplay());
						

						GdawsTimeSeries toInsert = aqToGdawsTimeSeries(response, site);
	
						LOG.debug("Created Time Series: (Site)" + toInsert.getSiteId() + " (Group)" + toInsert.getGroupId() + " (Source)" + toInsert.getSourceId() + " with " + numOfPoints + " records.");

						timeSeriesDao.insertTimeseriesData(toInsert, oldSourceId);
					}


					//Update the site w/ a new timestamp of the last pull
					if(doUpdateLastPullTime){
						site.setLastNewPullStart(siteStartTime);
						site.setLastNewPullEnd(siteEndTime);
						siteConfiguationLoader.updateNewDataPullTimestamps(site);
					}
				} else {
					LOG.info("Skipping pull for site {}, parameter {} because the current pull dates are outside the neverbefore/after range: {} to {}", 
							site.getLocalSiteId(), site.getPCode(), 
							(site.getNeverPullBefore()!=null)?DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(site.getNeverPullBefore()):"[unspecified]", 
							(site.getNeverPullAfter()!=null)?DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(site.getNeverPullAfter()):"[unspecified]");
				}
			}
		}
	}
	
	public GdawsTimeSeries aqToGdawsTimeSeries(TimeSeriesDataServiceResponse source, SiteConfiguration site){
		GdawsTimeSeries newSeries = new GdawsTimeSeries();
		
		newSeries.setSiteId(site.getLocalSiteId());
		newSeries.setGroupId(site.getLocalParamId());
		newSeries.setSourceId(this.sourceId);
		
		//Build Points
		List<TimeSeriesRecord> newRecords = new ArrayList<>();
		
		LocalDateTime startTime = null, endTime = null;
		
		for(TimeSeriesPoint point : source.getPoints()){
			newRecords.add(aqToGdawsTimeSeriesPoint(point, site, source));
			
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
	
	public TimeSeriesRecord aqToGdawsTimeSeriesPoint(TimeSeriesPoint source, SiteConfiguration site, TimeSeriesDataServiceResponse sourceSeries){
		TimeSeriesRecord newPoint = new TimeSeriesRecord();
		
		newPoint.setSiteId(site.getLocalSiteId());
		newPoint.setGroupId(site.getLocalParamId());
		
		//Fix for points with no time
		if(source.getTimestamp().getDateTimeOffset().isSupported(ChronoUnit.HOURS)){
			newPoint.setMeasurementDate(TimeSeriesUtils.getMstDateTime(source.getTimestamp().getDateTimeOffset().atOffset(ZoneOffset.UTC)));
		} else {
			LOG.debug("Found point without associated time: " + source.getTimestamp().getDateTimeOffset());
			
			//TODO BEFORE MERGING: Make this be at START OF DAY.
			newPoint.setMeasurementDate((LocalDateTime.ofInstant(source.getTimestamp().getDateTimeOffset(), site.getNeverPullBefore().getOffset())));
		}
		newPoint.setFinalValue(Double.parseDouble(source.getValue().getDisplay()));
		newPoint.setSourceId(this.sourceId);
		
		//Apply Qualifiers
		for(Qualifier aqQualifier : sourceSeries.getQualifiers()){
			Integer gdawsQualifier = this.aqGdawsQualifierMap.get(aqQualifier.getIdentifier());
			
			//Apply ICE AFFECTED
			if(aqQualifier.getIdentifier().equalsIgnoreCase("ICE")){
				//NOTE: Temporarily disabled until we confirm that this is desired behavior.
				//newPoint.setIceAffectedId(ICE_AFFECTED_ID);
			}
			//If we have a valid mapping for this qualifier
			else if(gdawsQualifier != null){
				//If the qualifier time period includes the current point apply the qualifier
				if((LocalDateTime.from(aqQualifier.getStartTime().atOffset(ZoneOffset.UTC)).isBefore(newPoint.getMeasurementDate()) || LocalDateTime.from(aqQualifier.getStartTime().atOffset(ZoneOffset.UTC)).isEqual(newPoint.getMeasurementDate()))
						&& (LocalDateTime.from(aqQualifier.getEndTime().atOffset(ZoneOffset.UTC)).isAfter(newPoint.getMeasurementDate()) || LocalDateTime.from(aqQualifier.getEndTime().atOffset(ZoneOffset.UTC)).isEqual(newPoint.getMeasurementDate()))){
					newPoint.setMainQualifierId(gdawsQualifier);
					break;
				}
			}
		}
		
		//Apply Approvals
		for(Approval aqApproval : sourceSeries.getApprovals()){
			Integer gdawsApproval = this.aqGdawsApprovalMap.get(aqApproval.getApprovalLevel());
			
			//If we have a valid mapping for this qualifier
			if(gdawsApproval != null){
				//If the approval time period includes the current point apply the approval
				if((LocalDateTime.from(aqApproval.getStartTime().atOffset(ZoneOffset.UTC)).isBefore(newPoint.getMeasurementDate()) || LocalDateTime.from(aqApproval.getStartTime().atOffset(ZoneOffset.UTC)).isEqual(newPoint.getMeasurementDate()))
						&& (LocalDateTime.from(aqApproval.getEndTime().atOffset(ZoneOffset.UTC)).isAfter(newPoint.getMeasurementDate()) || LocalDateTime.from(aqApproval.getEndTime().atOffset(ZoneOffset.UTC)).isEqual(newPoint.getMeasurementDate()))){
					newPoint.setDataApprovalId(gdawsApproval);
					break;
				}
			}
		}
		
		//TODO: SubsiteId? Other?
		
		return newPoint;
	}
}
