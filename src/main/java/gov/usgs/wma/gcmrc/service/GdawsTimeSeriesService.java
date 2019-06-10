package gov.usgs.wma.gcmrc.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class GdawsTimeSeriesService {
	private static final Logger LOG = LoggerFactory.getLogger(GdawsTimeSeriesService.class);

	public GdawsTimeSeries toGdawsTimeSeries(List<TimeSeriesRecord> points, Integer siteId, Integer paramId, Integer sourceId){
		GdawsTimeSeries newSeries = new GdawsTimeSeries();
		
		newSeries.setSiteId(siteId);
		newSeries.setGroupId(paramId);
		newSeries.setSourceId(sourceId);
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