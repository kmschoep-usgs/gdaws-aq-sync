package gov.usgs.wma.gcmrc.model;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;

/**
 * Utility class that represents a date range between a given start date & time
 * and a given end date & time.
 * 
 * Range is represented with OffsetDateTime objects.
 * 
 * @author thongsav
 */
public class DateRange implements Comparable<DateRange>{
	private OffsetDateTime startDate;
	private OffsetDateTime endDate;

	/**
	 * Constructor that builds a date range from 2 LocalDateTime instances.
	 * 
	 * @param startDate
	 * @param endDate
	 */
	public DateRange(LocalDateTime startDate, LocalDateTime endDate) {
		super();
		this.startDate = OffsetDateTime.of(startDate, ZoneOffset.UTC);
		this.endDate = OffsetDateTime.of(endDate, ZoneOffset.UTC);
	}

	/**
	 * Constructor that builds a date range from 2 Temporal instances.
	 * 
	 * @param startDate
	 * @param endDate
	 */
	public DateRange(Temporal startDate, Temporal endDate) {
		super();
		this.startDate = OffsetDateTime.from(startDate);
		this.endDate = OffsetDateTime.from(endDate);
	}

	/**
	 *
	 * @return The date range start date & time
	 */
	public OffsetDateTime getStartDate() {
		return startDate;
	}

	/**
	 *
	 * @return The date range end date & time
	 */
	public OffsetDateTime getEndDate() {
		return endDate;
	}

	/**
	 *
	 * @param startDate The start date & time to set
	 */
	public void setStartDate(OffsetDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 *
	 * @param endDate The end date & time to set
	 */
	public void setEndDate(OffsetDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Utility function that checks whether or not the provided date range overlaps
	 * the date range that this function is being called from.
	 * 
	 * @param dateRange The date range that should be compared with the date range calling this function.
	 * @return TRUE - The date ranges overlap. | FALSE - The date ranges do not overlap.
	 */
	public boolean overlaps(DateRange dateRange) {
		OffsetDateTime inStart = dateRange.getStartDate();
		OffsetDateTime inEnd = dateRange.getEndDate();
		
		return this.startDate.isBefore(inEnd) && this.endDate.isAfter(dateRange.getStartDate()) || 
				this.endDate.isAfter(inStart) && this.startDate.isBefore(inEnd);
	}

	/**
	 * Utility function that returns whether or not the provided time is contained
	 * within the time range. Note that containment is not considered true of the
	 * edges of the comparison range are equal to the provided time. The provided
	 * time must be AFTER the start date of the range and BEFORE the end date of
	 * the range for this to return true.
	 * 
	 * @param inTime The time that should be compared with the date range calling this function.
	 * @return TRUE - The provided time is contained in the date range. | FALSE - The provided time is not contained in the date range.
	 */
	public boolean contains(Temporal inTime) {
		return this.startDate.isBefore(OffsetDateTime.from(inTime)) && this.endDate.isAfter(OffsetDateTime.from(inTime));
	}
	

	@Override
	public int compareTo(DateRange o) {
		if(!(o instanceof Temporal)){
			throw new ClassCastException();
		} 
		DateRange otherRange = (DateRange)o;

		LocalDateTime thisStart = LocalDateTime.from(this.getStartDate());
		LocalDateTime otherStart = LocalDateTime.from(otherRange.getStartDate());
		
		return (thisStart.compareTo(otherStart));
	}
}
