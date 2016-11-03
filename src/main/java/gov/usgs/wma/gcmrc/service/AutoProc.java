package gov.usgs.wma.gcmrc.service;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import gov.usgs.wma.gcmrc.util.TimeSeriesUtils;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProcConfigurationLoader.class);
	
	private AutoProcConfigurationLoader autoProcConfLoader;
	private TimeSeriesDAO timeSeriesDAO;
	private Integer sourceId;
	
	public static final String DISCHARGE_PARAMETER_NAME = "Discharge"; //TODO give user way to override this;
	public static final String INST_SUSP_SAND_PARAMETER_NAME = "S Sand Inst Load"; //TODO give user way to override this;
	
	public AutoProc(GdawsDaoFactory gdawsDaoFactory, Integer sourceId) {
		this.autoProcConfLoader = new AutoProcConfigurationLoader(gdawsDaoFactory);
		this.timeSeriesDAO = new TimeSeriesDAO(gdawsDaoFactory);
		this.sourceId = sourceId;
	}
	
	//TODO refactor bedload stuff out into it's own class and leave AutoProc as the top level service class for all future calculation
	
	public void processBedloadCalculations() {
		Map<Integer, Map<String, Double>> bedLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadBedLoadCalculationConfiguration());
		
		for(Integer siteId : bedLoadParams.keySet()) {
			List<TimeSeriesRecord> discharge = timeSeriesDAO.getTimeSeries(siteId, DISCHARGE_PARAMETER_NAME); 
			Map<LocalDateTime, Integer> dischargeMillisIndex = TimeSeriesUtils.asMillisIndexMap(discharge);
			List<TimeSeriesRecord> suspendedSand = timeSeriesDAO.getTimeSeries(siteId, INST_SUSP_SAND_PARAMETER_NAME);
			
			Double c1 = bedLoadParams.get(siteId).get("c1");
			Double c2 = bedLoadParams.get(siteId).get("c2");
			
			LOG.info("Running bedload calculations for site {} with C1 {} C2 {}, {} discharge points, {} suspended sand load points", siteId, c1, c2,
					discharge.size(), suspendedSand.size()
					);
			
			List<TimeSeriesRecord> results = new LinkedList<>();
			
			for(TimeSeriesRecord susp : suspendedSand) {
				LocalDateTime time = susp.getMeasurementDate();
				TimeSeriesRecord correspondingDischarge = dischargeMillisIndex.get(time) != null ? discharge.get(dischargeMillisIndex.get(time)) : null;
				if(correspondingDischarge == null) {
					LOG.trace("Discharge interpolation needed for {}", susp.getMeasurementDate());
					correspondingDischarge = TimeSeriesUtils.getInterpolatedDischarge(discharge, dischargeMillisIndex, time);
				} 

				//Bedload calc Y=X(10.^(c1+c2logQ))
				Double instBedload = susp.getFinalValue() * (Math.pow(10, (c1 + c2 * Math.log10(correspondingDischarge.getFinalValue())))); 
				
				LOG.trace("Calculated bed load {} {} {}", siteId, time, instBedload);
				
				//add result
				results.add(new TimeSeriesRecord(time, instBedload, sourceId));
			}
			
			//TODO insert records into time_series_ap_stage
		}
		
		//TODO merge time_series_stage into time_series_star
	}
}
