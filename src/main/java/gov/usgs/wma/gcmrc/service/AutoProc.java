package gov.usgs.wma.gcmrc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;

public class AutoProc {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProc.class);
	private BedloadCalculation bedloadCalculation;
	
	public AutoProc(GdawsDaoFactory gdawsDaoFactory, Integer sourceId) {
		this.bedloadCalculation = new BedloadCalculation(gdawsDaoFactory, sourceId);
	}
	
	public void processBedloadCalculations(Integer instantaneousBedloadGroupId, Integer cumulativeBedloadGroupId) {
		bedloadCalculation.processBedloadCalculations(instantaneousBedloadGroupId, cumulativeBedloadGroupId);
	}
	
	public void processMergeCumulativeLoadCalculations(Integer ... cumulativeLoadGroupIds) {
		bedloadCalculation.processMergeCumulativeLoadCalculations(cumulativeLoadGroupIds);
	
	}
}