package gov.usgs.wma.gcmrc.model;

import com.google.common.base.MoreObjects;
import java.math.BigDecimal;

import java.util.Objects;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

/**
 * The AQCU representation of a gap tolerance period in a Time Series
 * 
 * @author zmoore
 */
public class TimeSeriesGapTolerance {
	private Temporal startTime;
	private Temporal endTime;
	private BigDecimal toleranceInMinutes;
	
	/**
	 * Constructor that creates an AQCU TimeSeriesGapTolerance with all of the necessary and
	 * relevant  parameters.
	 * 
	 * @param startTime The start date & time of the gap tolerance
	 * @param endTime The end date & time of the gap tolerance
	 * @param toleranceInMinutes The tolerance value (in minutes) of what constitutes a gap or not
	 */
	public TimeSeriesGapTolerance(Temporal startTime, Temporal endTime, BigDecimal toleranceInMinutes) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.toleranceInMinutes = toleranceInMinutes;
	}

	/**
	 *
	 * @return The start date & time of the tolerance period
	 */
	public Temporal getStartTime() {
		return startTime;
	}
	
	/**
	 *
	 * @return The end date & time of the tolerance period
	 */
	public Temporal getEndTime() {
		return endTime;
	}
	
	/**
	 *
	 * @return The tolerance value in minutes of what constitutes a gap during this period
	 */
	public BigDecimal getToleranceInMinutes() {
		return toleranceInMinutes;
	}

	/**
	 *
	 * @param startTime The start date & time to set
	 * @return The TimeSeriesGapTolerance object
	 */
	public TimeSeriesGapTolerance setStartTime(Temporal startTime) {
		this.startTime = startTime;
		return this;
	}
	
	/**
	 *
	 * @param endTime The end date & time to set
	 * @return The TimeSeriesGapTolerance object
	 */
	public TimeSeriesGapTolerance setEndTime(Temporal endTime) {
		this.endTime = endTime;
		return this;
	}
	
	/**
	 *
	 * @param toleranceInMinutes The tolerance value in minutes to set
	 * @return The TimeSeriesGapTolerance object
	 */
	public TimeSeriesGapTolerance setToleranceInMinutes(BigDecimal toleranceInMinutes) {
		this.toleranceInMinutes = toleranceInMinutes;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if ((getClass() != obj.getClass())
				&& (!TimeSeriesGapTolerance.class.isAssignableFrom(obj.getClass()))) {
			return false;
		}
		final TimeSeriesGapTolerance rhs = (TimeSeriesGapTolerance) obj;
		
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
					|| (compareEndTime == 0))
				&& Objects.equals(this.getToleranceInMinutes(), rhs.getToleranceInMinutes());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getStartTime(), this.getEndTime(), this.getToleranceInMinutes());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("startTime", this.getStartTime())
				.add("endTime", this.getEndTime())
				.add("toleranceInMinutes", this.getToleranceInMinutes())
				.toString();
	}
}
