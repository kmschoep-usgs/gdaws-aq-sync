package gov.usgs.wma.gcmrc.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.CumulativeBedloadDAO;
import gov.usgs.wma.gcmrc.dao.MergeCumulativeLoadCalcDAO;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import gov.usgs.wma.gcmrc.util.TimeSeriesUtils;

public class BedloadCalculation {
	private static final Logger LOG = LoggerFactory.getLogger(BedloadCalculation.class);
	
	private AutoProcConfigurationLoader autoProcConfLoader;
	private TimeSeriesDAO timeSeriesDAO;
	private CumulativeBedloadDAO cumulativeBedloadDAO;
	private MergeCumulativeLoadCalcDAO mergeCumulativeLoadCalcDAO;
	private GdawsTimeSeriesService gdawsTimeSeriesService;
	private Integer sourceId;
	
	public static final String DISCHARGE_PARAMETER_NAME = "Discharge"; //TODO give user way to override this;
	public static final String INST_SUSP_SAND_PARAMETER_NAME = "S Sand Inst Load"; //TODO give user way to override this;
	
	public BedloadCalculation(GdawsDaoFactory gdawsDaoFactory, Integer sourceId) {
		this.autoProcConfLoader = new AutoProcConfigurationLoader(gdawsDaoFactory);
		this.timeSeriesDAO = new TimeSeriesDAO(gdawsDaoFactory);
		this.cumulativeBedloadDAO = new CumulativeBedloadDAO(gdawsDaoFactory);
		this.mergeCumulativeLoadCalcDAO = new MergeCumulativeLoadCalcDAO(gdawsDaoFactory);
		this.gdawsTimeSeriesService = new GdawsTimeSeriesService();
		this.sourceId = sourceId;
	}

	public void processBedloadCalculations(Integer instantaneousBedloadGroupId, Integer cumulativeBedloadGroupId) {
		Map<Integer, Map<String, String>> bedLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadBedLoadCalculationConfiguration());
		
		Map<Integer, Map<String, String>> cumulativeBedLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadCumulativeBedLoadCalculationConfiguration());
		
		for(Integer siteId : bedLoadParams.keySet()) {
			
			Double c1 = Double.parseDouble(bedLoadParams.get(siteId).get("c1"));
			Double c2 = Double.parseDouble(bedLoadParams.get(siteId).get("c2"));
			Double uc1 = Double.parseDouble(bedLoadParams.get(siteId).get("uc1"));
			Double uc2 = Double.parseDouble(bedLoadParams.get(siteId).get("uc2"));
			Double lc1 = Double.parseDouble(bedLoadParams.get(siteId).get("lc1"));
			Double lc2 = Double.parseDouble(bedLoadParams.get(siteId).get("lc2"));
			String proxySiteId = bedLoadParams.get(siteId).get("dischargeProxySiteId");
			String timeShiftStr = bedLoadParams.get(siteId).get("timeShiftMinutes");
			Integer timeShiftMinutes = 0;
			Integer dischargeSiteId;
			
			if (timeShiftStr != null) {
				timeShiftMinutes = Integer.parseInt(timeShiftStr);
			}
			
			if (proxySiteId != null) {
				dischargeSiteId = Integer.parseInt(proxySiteId);
			} else
			{
				dischargeSiteId = siteId;
			}
			
			List<TimeSeriesRecord> discharge = timeSeriesDAO.getTimeSeries(dischargeSiteId, DISCHARGE_PARAMETER_NAME); 
			
			//we can't do anything if we don't have discharge
			if(discharge.size() == 0) {
				LOG.warn("No discharge data was found for {}, skipping bedload calculations", siteId);
				continue;
			}
			
			Map<LocalDateTime, Integer> dischargeMillisIndex = TimeSeriesUtils.asMillisIndexMap(discharge);
			List<TimeSeriesRecord> suspendedSand = timeSeriesDAO.getTimeSeries(siteId, INST_SUSP_SAND_PARAMETER_NAME);
			
			
			LOG.info("Running instantaneous bedload calculations for site {} with C1 {} C2 {} UC1 {} UC2 {} LC1 {} LC2 {}, using discharge from site {}, {} discharge points, {} suspended sand load points", siteId, c1, c2,
					uc1, uc2, lc1, lc2, dischargeSiteId, discharge.size(), suspendedSand.size()
					);
			
			List<TimeSeriesRecord> results = new LinkedList<>();
			
			for(TimeSeriesRecord susp : suspendedSand) {
				LocalDateTime suspSandTime = susp.getMeasurementDate();
				
				// Get the corresponding discharge, applying any time shift if necessary.
				LocalDateTime dischargeTime = suspSandTime.plusMinutes(timeShiftMinutes);
				
				Double instBedload = null;
				Double upperBound = null;
				Double lowerBound = null;
				Double retBedload = null;

				TimeSeriesRecord correspondingDischarge = dischargeMillisIndex.get(dischargeTime) != null ? discharge.get(dischargeMillisIndex.get(dischargeTime)) : null;
				if(correspondingDischarge == null) {
					LOG.trace("Discharge interpolation needed for {}", susp.getMeasurementDate());
					correspondingDischarge = TimeSeriesUtils.getInterpolatedDischarge(discharge, dischargeTime, sourceId, instantaneousBedloadGroupId, siteId);
				} 
				
				if(susp.getFinalValue() == 0d || correspondingDischarge.getFinalValue() == 0d) {
					retBedload = 0d;
				} else {
					//Bedload calc Y=X(10.^(c1+c2logQ))
					instBedload = TimeSeriesUtils.getInstBedload(susp.getFinalValue(), c1, c2, correspondingDischarge.getFinalValue());
					
					//upper bounding calc Y=(10.^(uc1+uc2logQ))
					upperBound = TimeSeriesUtils.getBoundingValue(uc1, uc2, correspondingDischarge.getFinalValue());
					
					//lower bounding calc Y=(10.^(lc1+lc2logQ))
					lowerBound = TimeSeriesUtils.getBoundingValue(lc1, lc2, correspondingDischarge.getFinalValue());
					
					if (instBedload > upperBound){
						retBedload = upperBound;
					} else if (instBedload < lowerBound) {
						retBedload = lowerBound;
					} else {
						retBedload = instBedload;
					}
				}

				LOG.trace("Calculated instantaneous bed load {} {} {}, upper bound {}, lower bound {}, using {} for bedload", 
						siteId, suspSandTime, instBedload, upperBound, lowerBound, retBedload);
				
				if(retBedload.isNaN()) {
					LOG.warn("Calculated instantaneous bed load isNaN");
				}
				
				//add result
				results.add(new TimeSeriesRecord(suspSandTime, retBedload, sourceId, instantaneousBedloadGroupId, siteId));
			}

			if(results.size() > 0) {
				timeSeriesDAO.insertTimeseriesData(gdawsTimeSeriesService.toGdawsTimeSeries(results, siteId, instantaneousBedloadGroupId, sourceId), null);
			}
		}
		
		for(Integer siteId : cumulativeBedLoadParams.keySet()) {
			
			String proxyBedloadSiteId = cumulativeBedLoadParams.get(siteId).get("bedloadProxySiteId");
			Integer sourceSiteId;
			
			if (proxyBedloadSiteId != null) {
				sourceSiteId = Integer.parseInt(proxyBedloadSiteId);
			} else
			{
				sourceSiteId = siteId;
			}
			cumulativeBedloadDAO.calcCumulativeBedloadToStageTable(siteId, sourceSiteId, sourceId, instantaneousBedloadGroupId, cumulativeBedloadGroupId);
		}
	}
	
	public void processMergeCumulativeLoadCalculations(Integer ... cumulativeLoadGroupIds) {
		
		for(Integer group: cumulativeLoadGroupIds){
			LOG.info("Starting Merge Cumulative Load Calculation for group ID {}", group);
			processMergeCumulativeLoadCalculation(group);
			LOG.info("Finished Merge Cumulative Load Calculation for group ID {}", group);
		}
	
	}
	private void processMergeCumulativeLoadCalculation(Integer cumulativeLoadGroupId) {
		Map<Integer, Map<String, String>> mergeCumulativeLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadMergeCumulCalculationConfiguration());

		for(Integer siteId : mergeCumulativeLoadParams.keySet()) {	
			String lastTimestamp = mergeCumulativeLoadParams.get(siteId).get("lastTimestamp");
			String firstTimestamp = mergeCumulativeLoadParams.get(siteId).get("firstTimestamp");
			Integer newSiteId = Integer.parseInt(mergeCumulativeLoadParams.get(siteId).get("newSiteId"));
			String timeShiftStr = mergeCumulativeLoadParams.get(siteId).get("timeShiftMinutes");
			String timeShiftMinutes = "0";
			
			if (timeShiftStr != null) {
				timeShiftMinutes = timeShiftStr;
			}

			mergeCumulativeLoadCalcDAO.calcMergeCumulativeLoadCalcToStageTable(siteId, newSiteId, sourceId, cumulativeLoadGroupId, lastTimestamp, firstTimestamp, timeShiftMinutes);
		}
	}

}