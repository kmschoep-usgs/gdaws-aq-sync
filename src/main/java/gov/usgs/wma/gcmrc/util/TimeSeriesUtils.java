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
	
	/** The TimeZone all data in the project is assumed to be in (Mountain Standard Time, offset of -7 hours.) */
	public static final ZoneOffset MST_ZONE_OFFSET = ZoneOffset.of("-07:00");
	
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
		
		return LocalDateTime.from(ZonedDateTime.from(aqDateTime).withZoneSameInstant(MST_ZONE_OFFSET));
	}
	
	/**
	 * Tacks on the MST Timezone to a LocalDateTime.
	 * 
	 * This method doesn't do much, but it is tricky to understand if adding the
	 * zone adjusts the time - here it codifies how to do it for the project.
	 * 
	 * Returns null for null.
	 * 
	 * @param localDateTime
	 * @return 
	 */
	public static ZonedDateTime getAsMstDateTime(LocalDateTime localDateTime) {
		if (localDateTime != null) {
			return localDateTime.atZone(MST_ZONE_OFFSET);
		} else {
			return null;
		}
	}
	
	/**
	 * The instantaneous bedload calculation
	 * 
	 * @param suspSed
	 * @param c1
	 * @param c2
	 * @param corrDis
	 * @return
	 */
	
	public static Double getInstBedload(Double suspSed, Double c1, Double c2, Double corrDis) {
		//Bedload calc Y=X(10.^(c1+c2logQ))
		return suspSed * getBoundingValue(c1, c2, corrDis);
	}
	
	public static Double getBoundingValue(Double c1, Double c2, Double corrDis) {
	//(10.^(c1+c2logQ))
		return (Math.pow(10, (c1 + c2 * Math.log10(corrDis))));
}
}
