package gov.usgs.wma.gcmrc.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.CumulativeBedloadDAO;
import gov.usgs.wma.gcmrc.dao.MergeCumulativeLoadCalcsDAO;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import gov.usgs.wma.gcmrc.util.TimeSeriesUtils;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProc.class);
	
	private AutoProcConfigurationLoader autoProcConfLoader;
	private TimeSeriesDAO timeSeriesDAO;
	private CumulativeBedloadDAO cumulativeBedloadDAO;
	private MergeCumulativeLoadCalcsDAO mergeCumulativeLoadCalcsDAO;
	private Integer sourceId;
	
	public static final String DISCHARGE_PARAMETER_NAME = "Discharge"; //TODO give user way to override this;
	public static final String INST_SUSP_SAND_PARAMETER_NAME = "S Sand Inst Load"; //TODO give user way to override this;
	
	public AutoProc(GdawsDaoFactory gdawsDaoFactory, Integer sourceId) {
		this.autoProcConfLoader = new AutoProcConfigurationLoader(gdawsDaoFactory);
		this.timeSeriesDAO = new TimeSeriesDAO(gdawsDaoFactory);
		this.cumulativeBedloadDAO = new CumulativeBedloadDAO(gdawsDaoFactory);
		this.mergeCumulativeLoadCalcsDAO = new MergeCumulativeLoadCalcsDAO(gdawsDaoFactory);
		this.sourceId = sourceId;
		
	}
	
	//TODO refactor bedload stuff out into it's own class and leave AutoProc as the top level service class for all future calculation
	
	public void processBedloadCalculations(Integer instantaneousBedloadGroupId, Integer cumulativeBedloadGroupId) {
		Map<Integer, Map<String, String>> bedLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadBedLoadCalculationConfiguration());
		
		for(Integer siteId : bedLoadParams.keySet()) {
			
			Double c1 = Double.parseDouble(bedLoadParams.get(siteId).get("c1"));
			Double c2 = Double.parseDouble(bedLoadParams.get(siteId).get("c2"));
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
			
			
			LOG.info("Running instantaneous bedload calculations for site {} with C1 {} C2 {}, using discharge from site {}, {} discharge points, {} suspended sand load points", siteId, c1, c2,
					dischargeSiteId, discharge.size(), suspendedSand.size()
					);
			
			List<TimeSeriesRecord> results = new LinkedList<>();
			
			for(TimeSeriesRecord susp : suspendedSand) {
				LocalDateTime time = susp.getMeasurementDate().plusMinutes(timeShiftMinutes);
				
				Double instBedload;

				TimeSeriesRecord correspondingDischarge = dischargeMillisIndex.get(time) != null ? discharge.get(dischargeMillisIndex.get(time)) : null;
				if(correspondingDischarge == null) {
					LOG.trace("Discharge interpolation needed for {}", susp.getMeasurementDate());
					correspondingDischarge = TimeSeriesUtils.getInterpolatedDischarge(discharge, time, sourceId, instantaneousBedloadGroupId, siteId);
				} 
				
				if(susp.getFinalValue() == 0d || correspondingDischarge.getFinalValue() == 0d) {
					instBedload = 0d;
				} else {
					//Bedload calc Y=X(10.^(c1+c2logQ))
					instBedload = susp.getFinalValue() * (Math.pow(10, (c1 + c2 * Math.log10(correspondingDischarge.getFinalValue()))));
				}

				LOG.trace("Calculated instantaneous bed load {} {} {}", siteId, time, instBedload);
				
				if(instBedload.isNaN()) {
					LOG.warn("Calculated instantaneous bed load isNaN");
				}
				
				//add result
				results.add(new TimeSeriesRecord(time, instBedload, sourceId, instantaneousBedloadGroupId, siteId));
			}

			if(results.size() > 0) {
				timeSeriesDAO.insertTimeseriesData(toGdawsTimeSeries(results, siteId, instantaneousBedloadGroupId), null);
			}
			
			cumulativeBedloadDAO.calcCumulativeBedloadToStageTable(siteId, sourceId, instantaneousBedloadGroupId, cumulativeBedloadGroupId);
		}
	}
	
	public void processMergeCumulativeLoadCalculations(Integer cumulativeLoadGroupId) {
		Map<Integer, Map<String, String>> mergeCumulativeLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadMergeCumulCalculationConfiguration());

		for(Integer siteId : mergeCumulativeLoadParams.keySet()) {	
			String lastTimestamp = mergeCumulativeLoadParams.get(siteId).get("lastTimestamp");
			String firstTimestamp = mergeCumulativeLoadParams.get(siteId).get("firstTimestamp");
			Integer newSiteId = Integer.parseInt(mergeCumulativeLoadParams.get(siteId).get("newSiteId"));

			mergeCumulativeLoadCalcsDAO.calcMergeCumulativeLoadCalcsToStageTable(siteId, newSiteId, sourceId, cumulativeLoadGroupId, lastTimestamp, firstTimestamp);
		}
	}
	
	private GdawsTimeSeries toGdawsTimeSeries(List<TimeSeriesRecord> points, Integer siteId, Integer paramId){
		GdawsTimeSeries newSeries = new GdawsTimeSeries();
		
		newSeries.setSiteId(siteId);
		newSeries.setGroupId(paramId);
		newSeries.setSourceId(this.sourceId);
		newSeries.setRecords(points);
		
		LocalDateTime startTime = null, endTime = null;
		
		for(TimeSeriesRecord p : points){
			if(startTime == null || p.getMeasurementDate().isBefore(startTime)){
				startTime = p.getMeasurementDate();
			}
			
			if(endTime == null || p.getMeasurementDate().isAfter(endTime)){
				endTime = p.getMeasurementDate();
			}
		}
		
		newSeries.setStartTime(startTime);
		newSeries.setEndTime(endTime);
		
		return newSeries;
	}

}
