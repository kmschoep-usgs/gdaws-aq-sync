package gov.usgs.wma.gcmrc.model;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * The AQCU representation to a gap in a Time Series
 * 
 * @author zmoore
 */
public class TimeSeriesGap {
	private Temporal startTime;
	private Temporal endTime;

	/**
	 *
	 * @return The start date & time of the gap
	 */
	public Temporal getStartTime() {
		return startTime;
	}
	
	/**
	 *
	 * @return The end date & time of the gap
	 */
	public Temporal getEndTime() {
		return endTime;
	}

	/**
	 *
	 * @param startTime The start time to set
	 * @return The TimeSeriesGap object
	 */
	public TimeSeriesGap setStartTime(Temporal startTime) {
		this.startTime = startTime;
		return this;
	}
	
	/**
	 *
	 * @param endTime The end time to set
	 * @return The TimeSeriesGap object
	 */
	public TimeSeriesGap setEndTime(Temporal endTime) {
		this.endTime = endTime;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if ((getClass() != obj.getClass())
				&& (!TimeSeriesGap.class.isAssignableFrom(obj.getClass()))) {
			return false;
		}
		final TimeSeriesGap rhs = (TimeSeriesGap) obj;
		
		int compareStartTime = -1;
		if(this.getStartTime() instanceof LocalDate) {
			compareStartTime = ((LocalDate) this.getStartTime()).compareTo((LocalDate) rhs.getStartTime());
		} else if(this.getStartTime() instanceof OffsetDateTime) {
			compareStartTime = ((OffsetDateTime) this.getStartTime()).compareTo((OffsetDateTime) rhs.getStartTime());
		}
		
		int compareEndTime = -1;
		if(this.getEndTime() instanceof LocalDate) {
			compareEndTime = ((LocalDate) this.getEndTime()).compareTo((LocalDate) rhs.getEndTime());
		} else if(this.getEndTime() instanceof OffsetDateTime) {
			compareEndTime = ((OffsetDateTime) this.getEndTime()).compareTo((OffsetDateTime) rhs.getEndTime());
		}
			
		return (Objects.equals(this.getStartTime(), rhs.getStartTime())
					|| (compareStartTime == 0))
				&& (Objects.equals(this.getEndTime(), rhs.getEndTime())
					|| (compareEndTime == 0));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getStartTime(), this.getEndTime());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("startTime", this.getStartTime())
				.add("endTime", this.getEndTime())
				.toString();
	}
}
