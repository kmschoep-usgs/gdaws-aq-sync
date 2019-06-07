package gov.usgs.wma.gcmrc.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.dao.CumulativeBedloadDAO;
import gov.usgs.wma.gcmrc.dao.MergeCumulativeLoadCalcDAO;
import gov.usgs.wma.gcmrc.dao.GdawsDaoFactory;
import gov.usgs.wma.gcmrc.dao.TimeSeriesDAO;
import gov.usgs.wma.gcmrc.dao.TotalSuspendedSedimentDAO;
import gov.usgs.wma.gcmrc.model.AutoProcConfiguration;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import gov.usgs.wma.gcmrc.util.TimeSeriesUtils;

public class TotalSuspendedSedimentCalculation {
	private static final Logger LOG = LoggerFactory.getLogger(TotalSuspendedSedimentCalculation.class);
	
	private AutoProcConfigurationLoader autoProcConfLoader;
	private TimeSeriesDAO timeSeriesDAO;
	private CumulativeBedloadDAO cumulativeBedloadDAO;
	private MergeCumulativeLoadCalcDAO mergeCumulativeLoadCalcDAO;
	private GdawsTimeSeriesService gdawsTimeSeriesService;
	private TotalSuspendedSedimentDAO totalSuspendedSedimentDAO;
	private Integer sourceId;
	
	public TotalSuspendedSedimentCalculation(GdawsDaoFactory gdawsDaoFactory, Integer sourceId) {
		this.autoProcConfLoader = new AutoProcConfigurationLoader(gdawsDaoFactory);
		this.timeSeriesDAO = new TimeSeriesDAO(gdawsDaoFactory);
		this.cumulativeBedloadDAO = new CumulativeBedloadDAO(gdawsDaoFactory);
		this.mergeCumulativeLoadCalcDAO = new MergeCumulativeLoadCalcDAO(gdawsDaoFactory);
		this.gdawsTimeSeriesService = new GdawsTimeSeriesService();
		this.totalSuspendedSedimentDAO = new TotalSuspendedSedimentDAO(gdawsDaoFactory);
		this.sourceId = sourceId;
	}
	
	public void processTotalSuspendedSedimentCalculations(Integer finesGroupId, Integer sandGroupId, Integer totalSuspSedGroupId) {
		List<AutoProcConfiguration> totalSuspSedParams = autoProcConfLoader.loadTotalSuspSedCalculationConfiguration();
		List<Integer> siteIds = new ArrayList<>();
		siteIds = totalSuspSedParams.stream()
				.map(AutoProcConfiguration::getSiteId)
				.collect(Collectors.toList());

		totalSuspendedSedimentDAO.calcTotalSuspendedSedimentToStageTable(siteIds, finesGroupId, sandGroupId, totalSuspSedGroupId, sourceId);

	}

}