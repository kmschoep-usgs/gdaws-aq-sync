package gov.usgs.wma.gcmrc.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProcConfigurationLoader.class);
	
	private AutoProcConfigurationLoader autoProcConfLoader;
	private TimeSeriesDAO timeSeriesDAO;
	
	public AutoProc(GdawsDaoFactory gdawsDaoFactory) {
		this.autoProcConfLoader = new AutoProcConfigurationLoader(gdawsDaoFactory);
		this.timeSeriesDAO = new TimeSeriesDAO(gdawsDaoFactory);
	}
	
	public void processBedloadCalculations() {
		Map<Integer, Map<String, Double>> bedLoadParams = 
				autoProcConfLoader.asParamMap(autoProcConfLoader.loadBedLoadCalculationConfiguration());
		
		for(Integer siteId : bedLoadParams.keySet()) {
			//TODO this is prohibitively slow, commenting out for now while we consider moving this calculation to the database via SQL
//			List<TimeSeriesRecord> discharge = timeSeriesDAO.getTimeSeries(siteId, "Discharge"); //TODO parameterize this magic string?
//			List<TimeSeriesRecord> suspendedSand = timeSeriesDAO.getTimeSeries(siteId, "S Sand Inst Load"); //TODO parameterize this magic string?
			LOG.debug("HUZZAH SITE {} C1 {} C2 {}", siteId, bedLoadParams.get(siteId).get("c1"), bedLoadParams.get(siteId).get("c2"));
		}
	}
}
