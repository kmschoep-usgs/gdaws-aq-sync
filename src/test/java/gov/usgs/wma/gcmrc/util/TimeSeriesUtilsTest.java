package gov.usgs.wma.gcmrc.util;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class TimeSeriesUtilsTest {
	
	private LocalDateTime now = LocalDateTime.now();
	
	private Temporal ASTDateTime = OffsetDateTime.of(LocalDateTime.of(2014, Month.JANUARY, 14, 11, 30), ZoneOffset.of("-04:00"));
	private Temporal CSTDateTime = OffsetDateTime.of(LocalDateTime.of(2014, Month.JANUARY, 14, 9, 30), ZoneOffset.of("-06:00"));
	private Temporal ESTDateTime = OffsetDateTime.of(LocalDateTime.of(2014, Month.JANUARY, 14, 10, 30), ZoneOffset.of("-05:00"));
	private Temporal MSTDateTime = OffsetDateTime.of(LocalDateTime.of(2014, Month.JANUARY, 14, 8, 30), ZoneOffset.of("-07:00"));
	private Temporal MDTDateTime = OffsetDateTime.of(LocalDateTime.of(2014, Month.JANUARY, 14, 9, 30), ZoneOffset.of("-06:00"));

	private List<TimeSeriesRecord> testList = Arrays.asList(new TimeSeriesRecord[]{
			new TimeSeriesRecord(now.minus(7, ChronoUnit.HOURS), 1d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(6, ChronoUnit.HOURS), 2d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(5, ChronoUnit.HOURS), 3d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(4, ChronoUnit.HOURS), 5d, 0, 0, 0),
			new TimeSeriesRecord(now.minus(3, ChronoUnit.HOURS), 4d, 0, 0, 0),
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
		assertEquals(test.getFinalValue(), Double.valueOf(4.75));
		
		test = TimeSeriesUtils.getInterpolatedDischarge(testList, now.minus(345, ChronoUnit.MINUTES), 0, 0, 0);
		assertEquals(test.getMeasurementDate(), now.minus(345, ChronoUnit.MINUTES));
		assertEquals(test.getFinalValue(), Double.valueOf(2.25));
	}
	
	@Test
	public void getMstDateTimeTest() {
		LocalDateTime testDateTime = TimeSeriesUtils.getMstDateTime(ASTDateTime);
		assertNotEquals(testDateTime, LocalDateTime.from(ASTDateTime));
		assertEquals(testDateTime, LocalDateTime.from(MSTDateTime));
		
		testDateTime = TimeSeriesUtils.getMstDateTime(CSTDateTime);
		assertNotEquals(testDateTime, LocalDateTime.from(CSTDateTime));
		assertEquals(testDateTime, LocalDateTime.from(MSTDateTime));
		
		testDateTime = TimeSeriesUtils.getMstDateTime(ESTDateTime);
		assertNotEquals(testDateTime, LocalDateTime.from(ESTDateTime));
		assertEquals(testDateTime, LocalDateTime.from(MSTDateTime));
		
		testDateTime = TimeSeriesUtils.getMstDateTime(MDTDateTime);
		assertNotEquals(testDateTime, LocalDateTime.from(MDTDateTime));
		assertEquals(testDateTime, LocalDateTime.from(MSTDateTime));
		
		testDateTime = TimeSeriesUtils.getMstDateTime(MSTDateTime);
		assertEquals(testDateTime, LocalDateTime.from(MSTDateTime));
	}
	
	@Test
	public void getAsMstDateTimeTest() {
		LocalDateTime dt = LocalDateTime.of(2000, 2, 2, 6, 35); //Feb 2, 2000 at 5:34 AM
		ZonedDateTime zdt = TimeSeriesUtils.getAsMstDateTime(dt);
		
		assertEquals(2000, zdt.getYear());
		assertEquals(2, zdt.getMonthValue());
		assertEquals(2, zdt.getDayOfMonth());
		assertEquals(6, zdt.getHour());
		assertEquals(35, zdt.getMinute());
		assertEquals(ZoneOffset.of("-07:00"), zdt.getOffset());
		
		assertNull(TimeSeriesUtils.getAsMstDateTime(null));
	}
	
	@Test
	public void getBoundingValueTest() {
		Double c1 = -6.434;
		Double c2 = 1.935;
		Double corrDis = 4780.0;
		Double boundingValue = null;
		
		boundingValue = TimeSeriesUtils.getBoundingValue(c1, c2, corrDis);
		
		assertTrue(boundingValue.equals(4.849455516416029));
	}
	
	@Test
	public void getInstBedloadTest() {
		Double suspSed = 5.3204;
		Double c1 = 4.3139;
		Double c2 = -1.3821;
		Double corrDis = 4780.0;
		Double instBedload = null;
		
		instBedload = TimeSeriesUtils.getInstBedload(suspSed, c1, c2, corrDis);
		
		assertTrue(instBedload.equals(0.9005482546993885));
	}
}
