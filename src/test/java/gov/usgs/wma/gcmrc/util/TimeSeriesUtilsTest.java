package gov.usgs.wma.gcmrc.util;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class TimeSeriesUtilsTest {
	
	private LocalDateTime now = LocalDateTime.now();
	private List<TimeSeriesRecord> testList = Arrays.asList(new TimeSeriesRecord[]{
			new TimeSeriesRecord(now.minus(7, ChronoUnit.HOURS), 1d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(6, ChronoUnit.HOURS), 2d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(5, ChronoUnit.HOURS), 3d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(4, ChronoUnit.HOURS), 4d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(3, ChronoUnit.HOURS), 5d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(2, ChronoUnit.HOURS), 6d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(1, ChronoUnit.HOURS), 7d, 0, 0, 0)
	});
	
	private List<TimeSeriesRecord> outOfOrder = Arrays.asList(new TimeSeriesRecord[]{
			new TimeSeriesRecord(now.minus(1, ChronoUnit.HOURS), 1d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(2, ChronoUnit.HOURS), 2d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(3, ChronoUnit.HOURS), 3d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(4, ChronoUnit.HOURS), 4d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(5, ChronoUnit.HOURS), 5d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(6, ChronoUnit.HOURS), 6d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(7, ChronoUnit.HOURS), 7d, 0, 0, 0)
	});
	

	@Test
	public void asMillisIndexMapTest() {
		Map<LocalDateTime, Integer> timeIndexMap = TimeSeriesUtils.asMillisIndexMap(testList);
		
		assertEquals(7, timeIndexMap.size());
		assertEquals(new Integer(0), timeIndexMap.get(now.minus(7, ChronoUnit.HOURS)));
		assertEquals(new Integer(1), timeIndexMap.get(now.minus(6, ChronoUnit.HOURS)));
		assertEquals(new Integer(2), timeIndexMap.get(now.minus(5, ChronoUnit.HOURS)));
		assertEquals(new Integer(3), timeIndexMap.get(now.minus(4, ChronoUnit.HOURS)));
		assertEquals(new Integer(4), timeIndexMap.get(now.minus(3, ChronoUnit.HOURS)));
		assertEquals(new Integer(5), timeIndexMap.get(now.minus(2, ChronoUnit.HOURS)));
		assertEquals(new Integer(6), timeIndexMap.get(now.minus(1, ChronoUnit.HOURS)));
	}
	
	@Test(expected = RuntimeException.class)
	public void getBracketingPoints_test_unordered_list() {
		TimeSeriesUtils.getBracketingPoints(outOfOrder,  
				now.minus(200, ChronoUnit.MINUTES), new Integer[] { 0, outOfOrder.size() - 1});
	}
	
	@Test
	public void getBracketingIndexes() {
		Integer[] leftRight = TimeSeriesUtils.getBracketingPoints(testList,  
				now.minus(200, ChronoUnit.MINUTES), new Integer[] { 0, testList.size() - 1 });
		
		assertEquals(leftRight[0], Integer.valueOf(3));
		assertEquals(leftRight[1], Integer.valueOf(4));
	}
	

	@Test
	public void getInterpolatedDischargeTest() {
		TimeSeriesRecord test = TimeSeriesUtils.getInterpolatedDischarge(testList, now.minus(210, ChronoUnit.MINUTES), 0, 0, 0);
		assertEquals(test.getMeasurementDate(), now.minus(210, ChronoUnit.MINUTES));
		assertEquals(test.getFinalValue(), Double.valueOf(4.5));
		
		test = TimeSeriesUtils.getInterpolatedDischarge(testList, now.minus(225, ChronoUnit.MINUTES), 0, 0, 0);
		assertEquals(test.getMeasurementDate(), now.minus(225, ChronoUnit.MINUTES));
		assertEquals(test.getFinalValue(), Double.valueOf(4.25));
		
		test = TimeSeriesUtils.getInterpolatedDischarge(testList, now.minus(195, ChronoUnit.MINUTES), 0, 0, 0);
		assertEquals(test.getMeasurementDate(), now.minus(195, ChronoUnit.MINUTES));
		assertEquals(test.getFinalValue(), Double.valueOf(4.75));
	}
}
