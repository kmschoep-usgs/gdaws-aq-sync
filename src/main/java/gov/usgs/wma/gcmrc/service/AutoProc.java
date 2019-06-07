package gov.usgs.wma.gcmrc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProc.class);
	private BedloadCalculation bedloadCalculation;
	private TotalSuspendedSedimentCalculation totalSuspendedSedimentCalculation;
	
	public AutoProc(GdawsDaoFactory gdawsDaoFactory, Integer sourceId) {
		this.bedloadCalculation = new BedloadCalculation(gdawsDaoFactory, sourceId);
		this.totalSuspendedSedimentCalculation = new TotalSuspendedSedimentCalculation(gdawsDaoFactory, sourceId);
	}
	
	public void processBedloadCalculations(Integer instantaneousBedloadGroupId, Integer cumulativeBedloadGroupId) {
		bedloadCalculation.processBedloadCalculations(instantaneousBedloadGroupId, cumulativeBedloadGroupId);
	}
	
	public void processMergeCumulativeLoadCalculations(Integer ... cumulativeLoadGroupIds) {
		bedloadCalculation.processMergeCumulativeLoadCalculations(cumulativeLoadGroupIds);
	}

	public void processTotalSuspendedSedimentCalculation(Integer finesGroupId, Integer sandGroupId, Integer totalSuspSedGroupId) {
		totalSuspendedSedimentCalculation.processTotalSuspendedSedimentCalculations(finesGroupId, sandGroupId, totalSuspSedGroupId);	
	}
	
}