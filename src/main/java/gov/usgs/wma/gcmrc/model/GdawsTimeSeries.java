/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.wma.gcmrc.model;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * A simple wrapper class for holding GdawsTimeSeriesPoint instances
 * 
 * @author zmoore
 */
public class GdawsTimeSeries {
	private Number siteId;	
	private Number groupId;
	private Number sourceId;
	private Instant startTime;
	private Instant endTime;
	
	private List<TimeSeriesRecord> records;

	/**
	 * @return the siteId
	 */
	public Number getSiteId() {
		return siteId;
	}

	/**
	 * @param siteId the siteId to set
	 */
	public void setSiteId(Number siteId) {
		this.siteId = siteId;
	}

	/**
	 * @return the groupId
	 */
	public Number getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(Number groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the sourceId
	 */
	public Number getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(Number sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the records
	 */
	public List<TimeSeriesRecord> getRecords() {
		return records;
	}

	/**
	 * @param records the records to set
	 */
	public void setRecords(List<TimeSeriesRecord> records) {
		this.records = records;
	}

	/**
	 * @return the startTime
	 */
	public Instant getStartTime() {
		return startTime;
	}
	
	/**
	 * @return the formatted startTime string
	 */
	public String getStartTimeString() {
		return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(startTime);
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Instant getEndTime() {
		return endTime;
	}
	
	/**
	 * @return the formatted endTime string
	 */
	public String getEndTimeString() {
		return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endTime);
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}
}
