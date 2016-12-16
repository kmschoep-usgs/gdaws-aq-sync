package gov.usgs.wma.gcmrc.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.wma.gcmrc.dao.AutoProcConfigurationLoader;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;
import gov.usgs.wma.gcmrc.service.Interpolation;

public class TimeSeriesUtils {
	private static final Logger LOG = LoggerFactory.getLogger(AutoProcConfigurationLoader.class);
	
	public static TimeSeriesRecord getInterpolatedDischarge(List<TimeSeriesRecord> discharge, LocalDateTime targetDateTime, Integer sourceId, Integer groupId, Integer siteId) {
		Integer[] leftRightIndex = getBracketingPoints(discharge, targetDateTime, new Integer[] { 0, discharge.size()-1}); //start recursive function at the far left and right
		
		TimeSeriesRecord leftPoint = discharge.get(leftRightIndex[0]);
		TimeSeriesRecord rightPoint = discharge.get(leftRightIndex[1]);
		
		Double interpolatedValue = Interpolation.calculateLinear(
				leftPoint.getMeasurementDate(), rightPoint.getMeasurementDate(), 
				leftPoint.getFinalValue(), rightPoint.getFinalValue(), targetDateTime);
		
		TimeSeriesRecord interpolatedPoint = new TimeSeriesRecord(targetDateTime, interpolatedValue, sourceId, groupId, siteId);
		
		return interpolatedPoint;
	}
	
	/**
	 * Recursive function to find the two points bracketing the inner point to be interpolated;
	 *
	 */
	public static Integer[] getBracketingPoints(List<TimeSeriesRecord> timeseries, LocalDateTime targetDateTime, Integer[] leftRightIndexes) {
		int left = leftRightIndexes[0];
		int right = leftRightIndexes[1];
		
		if(right - left == 1) { //terminal case
			return leftRightIndexes;
		}
		
		int midPoint = (right + left) / 2;
		
		LocalDateTime leftTime = timeseries.get(left).getMeasurementDate();
		LocalDateTime rightTime = timeseries.get(right).getMeasurementDate();
		LocalDateTime midTime = timeseries.get(midPoint).getMeasurementDate();
		
		//check to make sure the 2 points are chronologicalling ordered
		if(leftTime.isAfter(midTime) || midTime.isAfter(rightTime)) {
			throw new RuntimeException("Timeseries is not an ordered list");
		}
		
		if(targetDateTime.isAfter(midTime)) {
			leftRightIndexes[0] = midPoint;
			leftRightIndexes[1] = right;
		} else {
			leftRightIndexes[0] = left;
			leftRightIndexes[1] = midPoint;
		}
		
		return getBracketingPoints(timeseries, targetDateTime, leftRightIndexes);
	}
	
	
	/**
	 * For the purpose of quickly finding time-value pairs for interpolating random points
	 */
	public static  Map<LocalDateTime, Integer> asMillisIndexMap(List<TimeSeriesRecord> list) {
		HashMap<LocalDateTime, Integer> newMap = new HashMap<>();
		for(int i = 0; i < list.size(); i++) {
			TimeSeriesRecord r = list.get(i);
			if(newMap.putIfAbsent(r.getMeasurementDate(), i) != null) {
				LOG.error("DUPE {} {} versus {} {}", r.getMeasurementDate(), r.getFinalValue(), 
						list.get(newMap.get(r.getMeasurementDate())).getMeasurementDate(), list.get(newMap.get(r.getMeasurementDate())).getFinalValue());
				throw new RuntimeException("Multiple points found at the same time in time series");
			}
		}
		
		return newMap;
	}
	
	public static LocalDateTime getMstDateTime(Temporal aqDateTime) {
		LocalDateTime mstDateTime;
		ZonedDateTime newZonedDateTime;
		ZoneOffset newZoneOffset;
		ZoneOffset oldZoneOffset = ZonedDateTime.from(aqDateTime).getOffset();
		
		if (!oldZoneOffset.equals(ZoneOffset.of("-07:00"))){
			newZoneOffset = ZoneOffset.of("-07:00");
			newZonedDateTime = ZonedDateTime.from(aqDateTime).withZoneSameInstant(newZoneOffset);
			mstDateTime = LocalDateTime.from(newZonedDateTime);
		} else {
			mstDateTime = LocalDateTime.from(aqDateTime);
		}
		return mstDateTime;
	}
}
