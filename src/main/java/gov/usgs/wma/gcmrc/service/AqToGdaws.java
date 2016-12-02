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
import gov.usgs.aqcu.model.Approval;
import gov.usgs.aqcu.model.Qualifier;
import gov.usgs.aqcu.model.TimeSeries;
import gov.usgs.aqcu.model.TimeSeriesPoint;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.SiteConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.dao.TimeSeriesTranslationLoader;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
	private Integer daysToFetchForNewTimeseries;
	private Integer sourceId;
	
	private DataService dataService;
	private SiteConfigurationLoader siteConfiguationLoader;
	private ZonedDateTime startTime;
	private ZonedDateTime endTime;
	private final Integer oldSourceId;
	private final ArrayList<String> tsToPullList;

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
	 * @param startTime 
	 * @param endTime
	 * @param tsToPullList 
	 */
	public AqToGdaws(DataService dataService, GdawsDaoFactory gdawsDaoFactory, 
			Integer daysToFetchForNewTimeseries, Integer sourceId, Integer oldSourceId, LocalDateTime startTime, LocalDateTime endTime, ArrayList<String> tsToPullList) {
		siteConfiguationLoader = new SiteConfigurationLoader(gdawsDaoFactory);
		this.sitesToLoad = siteConfiguationLoader.getAllSites();
		this.timeSeriesTranslationLoader = new TimeSeriesTranslationLoader(gdawsDaoFactory);
		this.aqGdawsApprovalMap = this.timeSeriesTranslationLoader.getAqGdawsApprovalMap();
		this.aqGdawsQualifierMap = this.timeSeriesTranslationLoader.getAqGdawsQualifierMap();
		this.dataService = dataService;
		this.timeSeriesDao = new TimeSeriesDAO(gdawsDaoFactory);
		this.daysToFetchForNewTimeseries = daysToFetchForNewTimeseries != null ? daysToFetchForNewTimeseries : DEFAULT_DAYS_TO_FETCH_FOR_NEW_TIMESERIES;
		this.sourceId = sourceId;
		this.oldSourceId = oldSourceId;
		this.startTime = startTime != null ? startTime.atZone(ZonedDateTime.now().getZone()) : null;
		this.endTime = endTime != null ? endTime.atZone(ZonedDateTime.now().getZone()) : null;
		this.tsToPullList = tsToPullList;
	}
	
	public void migrateAqData() {
		
		for(SiteConfiguration site : sitesToLoad) {
			if (site.getRemoteParamId() != null && (tsToPullList.isEmpty() || tsToPullList.contains(site.getRemoteParamId()))) {				
				if(startTime == null){
					//Adjust start and end pull times based on the last data pull run
					//and constrain by the max number of days we are willing to go back.
					if (site.getLastNewPullStart() != null && site.getLastNewPullEnd() != null) {
						//Pull data from since the last pull until now.
						//Move the start time back a second, since we round to the nearest second.
						startTime = site.getLastNewPullEnd().truncatedTo(ChronoUnit.SECONDS).minusSeconds(1);
					} else {
						startTime = endTime.minusDays(daysToFetchForNewTimeseries);
					}
				}
				
				if(endTime == null){
					endTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
				}
				
				//Further constain the pull times by the 'never before' and 'never after' bounds
				if (site.getNeverPullBefore() != null && startTime.isBefore(site.getNeverPullBefore())) {
					startTime = site.getNeverPullBefore();
				}
				
				if (site.getNeverPullAfter() != null && endTime.isAfter(site.getNeverPullAfter())) {
					endTime = site.getNeverPullAfter();
				}
				
				if (startTime.isBefore(endTime)) {
				

					LOG.debug("Pulling data for site {}, parameter {} for the date range starting {} to {}", 
							site.getLocalSiteId(), site.getPCode(), 
							DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime), 
							DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime));


					TimeSeries retrieved = dataService.getTimeSeriesData(
							site.getRemoteSiteId(), site.getRemoteParamId(), DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime),
							DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime), false, false);

					Integer numOfPoints = retrieved.getPoints().size();
					LOG.trace("Retrieved " + retrieved.getName() + " " + retrieved.getDescription() + 
							", which contains " + numOfPoints + " points");
					if(numOfPoints > 0) {
						LOG.trace("First point: " + 
								ISO8601TemporalSerializer.print(retrieved.getPoints().get(0).getTime()) + 
								" " + retrieved.getPoints().get(0).getValue());

						GdawsTimeSeries toInsert = aqToGdawsTimeSeries(retrieved, site);
	
						LOG.debug("Created Time Series: (Site)" + toInsert.getSiteId() + " (Group)" + toInsert.getGroupId() + " (Source)" + toInsert.getSourceId() + " with " + numOfPoints + " records.");

						timeSeriesDao.insertTimeseriesData(toInsert, oldSourceId);
					}


					//Update the site w/ a new timestamp of the last pull
					site.setLastNewPullStart(startTime);
					site.setLastNewPullEnd(endTime);
					siteConfiguationLoader.updateNewDataPullTimestamps(site);
				} else {
					LOG.info("Skipping pull for site {}, parameter {} because the current pull dates are outside the neverbefore/after range: {} to {}", 
							site.getLocalSiteId(), site.getPCode(), 
							(site.getNeverPullBefore()!=null)?DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(site.getNeverPullBefore()):"[unspecified]", 
							(site.getNeverPullAfter()!=null)?DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(site.getNeverPullAfter()):"[unspecified]");
				}
			}
		}
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
	
	public TimeSeriesRecord aqToGdawsTimeSeriesPoint(TimeSeriesPoint source, SiteConfiguration site, TimeSeries sourceSeries){
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
				if((LocalDateTime.from(aqQualifier.getStartDate()).isBefore(newPoint.getMeasurementDate()) || LocalDateTime.from(aqQualifier.getStartDate()).isEqual(newPoint.getMeasurementDate()))
						&& (LocalDateTime.from(aqQualifier.getEndDate()).isAfter(newPoint.getMeasurementDate()) || LocalDateTime.from(aqQualifier.getEndDate()).isEqual(newPoint.getMeasurementDate()))){
					newPoint.setMainQualifierId(gdawsQualifier);
					break;
				}
			}
		}
		
		//Apply Approvals
		for(Approval aqApproval : sourceSeries.getApprovals()){
			Integer gdawsApproval = this.aqGdawsApprovalMap.get(aqApproval.getLevel());
			
			//If we have a valid mapping for this qualifier
			if(gdawsApproval != null){
				//If the approval time period includes the current point apply the approval
				if((LocalDateTime.from(aqApproval.getStartTime()).isBefore(newPoint.getMeasurementDate()) || LocalDateTime.from(aqApproval.getStartTime()).isEqual(newPoint.getMeasurementDate()))
						&& (LocalDateTime.from(aqApproval.getEndTime()).isAfter(newPoint.getMeasurementDate()) || LocalDateTime.from(aqApproval.getEndTime()).isEqual(newPoint.getMeasurementDate()))){
					newPoint.setDataApprovalId(gdawsApproval);
					break;
				}
			}
		}
		
		//TODO: SubsiteId? Other?
		
		return newPoint;
	}
}
