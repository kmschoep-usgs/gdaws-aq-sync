package gov.usgs.wma.gcmrc.service;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import gov.usgs.wma.gcmrc.model.GdawsTimeSeries;
import gov.usgs.wma.gcmrc.model.TimeSeriesRecord;

public class GdawsTimeseriesServiceTest {
	
	private LocalDateTime now = LocalDateTime.now();
	private LocalDateTime now7 = now.minus(7, ChronoUnit.HOURS);
	private LocalDateTime now6 = now.minus(6, ChronoUnit.HOURS);
	private LocalDateTime now5 = now.minus(5, ChronoUnit.HOURS);
	private LocalDateTime now4 = now.minus(4, ChronoUnit.HOURS);
	private LocalDateTime now3 = now.minus(3, ChronoUnit.HOURS);
	private LocalDateTime now2 = now.minus(2, ChronoUnit.HOURS);
	private LocalDateTime now1 = now.minus(1, ChronoUnit.HOURS);
	
	private GdawsTimeSeriesService gdawsTimeSeriesService = new GdawsTimeSeriesService();
	private GdawsTimeSeries gdawsTimeSeries;
	
	private List<TimeSeriesRecord> testList = Arrays.asList(new TimeSeriesRecord[]{
			new TimeSeriesRecord(now7, 1d, 0, 0, 0),
			new TimeSeriesRecord(now6, 2d, 0, 0, 0),
			new TimeSeriesRecord(now5, 3d, 0, 0, 0),
			new TimeSeriesRecord(now4, 5d, 0, 0, 0),
			new TimeSeriesRecord(now3, 4d, 0, 0, 0),
			new TimeSeriesRecord(now2, 6d, 0, 0, 0),
			new TimeSeriesRecord(now1, 7d, 0, 0, 0)
	});
	
	@Test
	public void toGdawsTimeSeriesTest() {
		gdawsTimeSeries = gdawsTimeSeriesService.toGdawsTimeSeries(testList, 1, 2, 3);
		
		assertEquals(now7, gdawsTimeSeries.getStartTime());
		assertEquals(now1, gdawsTimeSeries.getEndTime());
		assertEquals(1, gdawsTimeSeries.getSiteId());
		assertEquals(2, gdawsTimeSeries.getGroupId());
		assertEquals(3 ,gdawsTimeSeries.getSourceId());
		assertEquals(7 ,gdawsTimeSeries.getRecords().size());
		
	}
}
