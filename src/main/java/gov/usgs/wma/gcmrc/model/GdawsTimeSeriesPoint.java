package gov.usgs.wma.gcmrc.model;

import java.time.temporal.Temporal;

/**
 * Represents a single row in the TIME_SERIES_STAR table
 * 
 * @author zmoore
 */
public class GdawsTimeSeriesPoint {
	private Number siteId;
	private Number subsiteId;
	private Number groupId;
	private Temporal measurementDate;
	private Number finalValue;
	private Number rawValue;
	private Number meausrementGradeId;
	private Number deploymentId;
	private Number turbidityPegged;
	private Number probeTypeId;
	private Number instrumentId;
	private Number dataLeadId;
	private Number rawFlagId;
	private Number dataQualificationId;
	private Number accuracyRatingId;
	private Number sourceId;
	private String notes;
	private Number erValue;
	private Number dataApprovalId;
	private Number mainQualifierId;
	private Number iceAffectedId;	

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
	 * @return the subsiteId
	 */
	public Number getSubsiteId() {
		return subsiteId;
	}

	/**
	 * @param subsiteId the subsiteId to set
	 */
	public void setSubsiteId(Number subsiteId) {
		this.subsiteId = subsiteId;
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
	 * @return the measurementDate
	 */
	public Temporal getMeasurementDate() {
		return measurementDate;
	}

	/**
	 * @param measurementDate the measurementDate to set
	 */
	public void setMeasurementDate(Temporal measurementDate) {
		this.measurementDate = measurementDate;
	}

	/**
	 * @return the finalValue
	 */
	public Number getFinalValue() {
		return finalValue;
	}

	/**
	 * @param finalValue the finalValue to set
	 */
	public void setFinalValue(Number finalValue) {
		this.finalValue = finalValue;
	}

	/**
	 * @return the rawValue
	 */
	public Number getRawValue() {
		return rawValue;
	}

	/**
	 * @param rawValue the rawValue to set
	 */
	public void setRawValue(Number rawValue) {
		this.rawValue = rawValue;
	}

	/**
	 * @return the meausrementGradeId
	 */
	public Number getMeausrementGradeId() {
		return meausrementGradeId;
	}

	/**
	 * @param meausrementGradeId the meausrementGradeId to set
	 */
	public void setMeausrementGradeId(Number meausrementGradeId) {
		this.meausrementGradeId = meausrementGradeId;
	}

	/**
	 * @return the deploymentId
	 */
	public Number getDeploymentId() {
		return deploymentId;
	}

	/**
	 * @param deploymentId the deploymentId to set
	 */
	public void setDeploymentId(Number deploymentId) {
		this.deploymentId = deploymentId;
	}

	/**
	 * @return the turbidityPegged
	 */
	public Number getTurbidityPegged() {
		return turbidityPegged;
	}

	/**
	 * @param turbidityPegged the turbidityPegged to set
	 */
	public void setTurbidityPegged(Number turbidityPegged) {
		this.turbidityPegged = turbidityPegged;
	}

	/**
	 * @return the probeTypeId
	 */
	public Number getProbeTypeId() {
		return probeTypeId;
	}

	/**
	 * @param probeTypeId the probeTypeId to set
	 */
	public void setProbeTypeId(Number probeTypeId) {
		this.probeTypeId = probeTypeId;
	}

	/**
	 * @return the instrumentId
	 */
	public Number getInstrumentId() {
		return instrumentId;
	}

	/**
	 * @param instrumentId the instrumentId to set
	 */
	public void setInstrumentId(Number instrumentId) {
		this.instrumentId = instrumentId;
	}

	/**
	 * @return the dataLeadId
	 */
	public Number getDataLeadId() {
		return dataLeadId;
	}

	/**
	 * @param dataLeadId the dataLeadId to set
	 */
	public void setDataLeadId(Number dataLeadId) {
		this.dataLeadId = dataLeadId;
	}

	/**
	 * @return the rawFlagId
	 */
	public Number getRawFlagId() {
		return rawFlagId;
	}

	/**
	 * @param rawFlagId the rawFlagId to set
	 */
	public void setRawFlagId(Number rawFlagId) {
		this.rawFlagId = rawFlagId;
	}

	/**
	 * @return the dataQualificationId
	 */
	public Number getDataQualificationId() {
		return dataQualificationId;
	}

	/**
	 * @param dataQualificationId the dataQualificationId to set
	 */
	public void setDataQualificationId(Number dataQualificationId) {
		this.dataQualificationId = dataQualificationId;
	}

	/**
	 * @return the accuracyRatingId
	 */
	public Number getAccuracyRatingId() {
		return accuracyRatingId;
	}

	/**
	 * @param accuracyRatingId the accuracyRatingId to set
	 */
	public void setAccuracyRatingId(Number accuracyRatingId) {
		this.accuracyRatingId = accuracyRatingId;
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
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * @return the erValue
	 */
	public Number getErValue() {
		return erValue;
	}

	/**
	 * @param erValue the erValue to set
	 */
	public void setErValue(Number erValue) {
		this.erValue = erValue;
	}

	/**
	 * @return the dataApprovalId
	 */
	public Number getDataApprovalId() {
		return dataApprovalId;
	}

	/**
	 * @param dataApprovalId the dataApprovalId to set
	 */
	public void setDataApprovalId(Number dataApprovalId) {
		this.dataApprovalId = dataApprovalId;
	}

	/**
	 * @return the mainQualifierId
	 */
	public Number getMainQualifierId() {
		return mainQualifierId;
	}

	/**
	 * @param mainQualifierId the mainQualifierId to set
	 */
	public void setMainQualifierId(Number mainQualifierId) {
		this.mainQualifierId = mainQualifierId;
	}

	/**
	 * @return the iceAffectedId
	 */
	public Number getIceAffectedId() {
		return iceAffectedId;
	}

	/**
	 * @param iceAffectedId the iceAffectedId to set
	 */
	public void setIceAffectedId(Number iceAffectedId) {
		this.iceAffectedId = iceAffectedId;
	}
	
	
}
