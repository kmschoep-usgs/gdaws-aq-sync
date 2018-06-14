package gov.usgs.wma.gcmrc.model;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Approval;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GapTolerance;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Note;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;

import java.util.Objects;

import java.time.temporal.Temporal;

/**
 * The AQCU representation of a Time Series
 * 
 * @author thongsav
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeSeries {
	
	private String name;
	
	private String type;
	
	private String units;
	
	private boolean isVolumetricFlow;
	
	private String description;

	private Temporal requestedStartTime;
	
	private Temporal startTime;
	
	private Temporal endTime;
	
	private Temporal requestedEndTime;
		
	private List<TimeSeriesPoint> points;
	
	private List<TimeSeriesGap> gaps;
	
	private List<GapTolerance> gapTolerances;
	
	private List<Approval> approvals;
	
	private List<DateRange> estimatedPeriods;
	
	private List<Qualifier> qualifiers;
	
	private List<Grade> grades;
	
	private List<Note> notes;

	/**
	 *
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @param name The name to set
	 * @return
	 */
	public TimeSeries setName(String name) {
		this.name = name;
		return this;
	}

	/**
	 *
	 * @return The type
	 */
	public String getType() {
		return type;
	}

	/**
	 *
	 * @param type The type to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setType(String type) {
		this.type = type;
		return this;
	}

	/**
	 *
	 * @return The units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 *
	 * @param units The units to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setUnits(String units) {
		this.units = units;
		return this;
	}

	/**
	 *
	 * @return The description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 *
	 * @param description The description to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 *
	 * @return The start date & time
	 */
	public Temporal getStartTime() {
		return startTime;
	}

	/**
	 *
	 * @param startTime The start date & time to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setStartTime(Temporal startTime) {
		this.startTime = startTime;
		return this;
	}

	/**
	 *
	 * @return The end date & time
	 */
	public Temporal getEndTime() {
		return endTime;
	}

	/**
	 *
	 * @param endTime The end date & time to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setEndTime(Temporal endTime) {
		this.endTime = endTime;
		return this;
	}

	/**
	 *
	 * @return The list of TimeSeresPoints that make up the TimeSeries
	 */
	public List<TimeSeriesPoint> getPoints() {
		return points;
	}
	
	/**
	 *
	 * @return The list of TimeSeriesGaps that are present in the TimeSeries
	 */
	public List<TimeSeriesGap> getGaps() {
		return this.gaps;
	}
	
	/**
	 *
	 * @return The list of TimeSeriesGapTolerances that are present in the TimeSeries
	 */
	public List<GapTolerance> getGapTolerances() {
		return this.gapTolerances;
	}

	/**
	 *
	 * @param points The list of TimeSeriesPoints to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setPoints(List<TimeSeriesPoint> points) {
		this.points = points;
		return this;
	}
	
	/**
	 *
	 * @param gaps The list of TimeSeriesGaps to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setGaps(List<TimeSeriesGap> gaps) {
		this.gaps = gaps;
		return this;
	}
	
	/**
	 *
	 * @param gapTolerances The list of TimeSeriesGapTolerances to set
	 * @return The TimeSeries object
	 */
	public TimeSeries setGapTolerances(List<GapTolerance> gapTolerances) {
		this.gapTolerances = gapTolerances;
		return this;
	}
		
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TimeSeries rhs = (TimeSeries) obj;
		return Objects.equals(this.getName(), rhs.getName())
				&& Objects.equals(this.getType(), rhs.getType())
				&& Objects.equals(this.getUnits(), rhs.getUnits())
				&& Objects.equals(this.getDescription(), rhs.getDescription())
				&& Objects.equals(this.getStartTime(), rhs.getStartTime())
				&& Objects.equals(this.getEndTime(), rhs.getEndTime())
				&& Objects.equals(this.getPoints(), rhs.getPoints())
				&& Objects.equals(this.getGaps(), rhs.getGaps())
				&& Objects.equals(this.getGapTolerances(), rhs.getGapTolerances());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getName(), this.getType(), this.getUnits(), this.getDescription(), this.getStartTime(), this.getEndTime(), this.getPoints(), this.getGaps());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("name", this.getName())
				.add("type", this.getType())
				.add("units", this.getUnits())
				.add("description", this.getDescription())
				.add("startTime", this.getStartTime())
				.add("endTime", this.getEndTime())
				.add("points", this.getPoints())
				.add("gaps", this.getGaps())
				.add("gapTolerances", this.getGapTolerances())
				.toString();
	}

	/**
	 *
	 * @return The list of Approvals
	 */
	public List<Approval> getApprovals() {
		return approvals;
	}

	/**
	 *
	 * @param approvals The list of Approvals to set
	 */
	public void setApprovals(List<Approval> approvals) {
		this.approvals = approvals;
	}

	/**
	 *
	 * @return The list of estimated DateRanges
	 */
	public List<DateRange> getEstimatedPeriods() {
		return estimatedPeriods;
	}

	/**
	 *
	 * @param estimatedPeriods The list of estimated DateRanges to set
	 */
	public void setEstimatedPeriods(List<DateRange> estimatedPeriods) {
		this.estimatedPeriods = estimatedPeriods;
	}
	
	/**
	 *
	 * @return The list of Qualifiers
	 */
	public List<Qualifier> getQualifiers() {
		return qualifiers;
	}

	/**
	 *
	 * @param qualifiers The list of Qualifiers to set
	 */
	public void setQualifiers(List<Qualifier> qualifiers) {
		this.qualifiers = qualifiers;
	}

	/**
	 *
	 * @return Whether or not this TimeSeries is representing volumetric flow
	 */
	public boolean isVolumetricFlow() {
		return isVolumetricFlow;
	}

	/**
	 *
	 * @param isVolumetricFlow Sets whether or not this TimeSeries is representing volumetric flow
	 */
	public void setVolumetricFlow(boolean isVolumetricFlow) {
		this.isVolumetricFlow = isVolumetricFlow;
	}

	/**
	 *
	 * @return The start date & time that this TimeSeries was requested from Aquarius
	 */
	public Temporal getRequestedStartTime() {
		return requestedStartTime;
	}

	/**
	 *
	 * @param requestedStartTime Sets the start date & time that this TimeSeries was requested from Aquarius
	 */
	public void setRequestedStartTime(Temporal requestedStartTime) {
		this.requestedStartTime = requestedStartTime;
	}

	/**
	 *
	 * @return The end date & time that this TimeSeries was requested from Aquarius
	 */
	public Temporal getRequestedEndTime() {
		return requestedEndTime;
	}

	/**
	 *
	 * @param requestedEndTime Sets the end date & time that this TimeSeries was requested from Aquarius
	 */
	public void setRequestedEndTime(Temporal requestedEndTime) {
		this.requestedEndTime = requestedEndTime;
	}

	/**
	 *
	 * @return The list of Grades
	 */
	public List<Grade> getGrades() {
		return grades;
	}

	/**
	 *
	 * @return The list of Notes
	 */
	public List<Note> getNotes() {
		return notes;
	}

	/**
	 *
	 * @param grades The list of Grades to set
	 */
	public void setGrades(List<Grade> grades) {
		this.grades = grades;
	}

	/**
	 *
	 * @param notes The list of Notes to set
	 */
	public void setNotes(List<Note> notes) {
		this.notes = notes;
	}
	
	
}
