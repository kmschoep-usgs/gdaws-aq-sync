package gov.usgs.wma.gcmrc.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

public class TimeSeriesRecord {
	private Integer siteId;
	private Integer subsiteId;             
	private Integer groupId;             
	private Instant measurementDate;       
	private BigDecimal finalValue;             
	private BigDecimal rawValue;             
	private Integer mainQualifierId;             
	private Integer dataApprovalId;             
	private Integer measurementGradeId;             
	private Integer deploymentId;             
	private Integer iceAffectedId;             
	private BigDecimal turbidityPegged;             
	private Integer probeTypeId;             
	private Integer instrumentId;             
	private Integer dataLeadId;             
	private Integer rawFlagId;             
	private Integer dataQualificationId;             
	private Integer AccuracyRatingId;             
	private Integer sourceId;    
	private String notes;    
	private BigDecimal erValue;
	
	public Integer getSiteId() {
		return siteId;
	}
	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}
	public Integer getSubsiteId() {
		return subsiteId;
	}
	public void setSubsiteId(Integer subsiteId) {
		this.subsiteId = subsiteId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Instant getMeasurementDate() {
		return measurementDate;
	}
	public void setMeasurementDate(Instant measurementDate) {
		this.measurementDate = measurementDate;
	}
	public BigDecimal getFinalValue() {
		return finalValue;
	}
	public void setFinalValue(BigDecimal finalValue) {
		this.finalValue = finalValue;
	}
	public BigDecimal getRawValue() {
		return rawValue;
	}
	public void setRawValue(BigDecimal rawValue) {
		this.rawValue = rawValue;
	}
	public Integer getMainQualifierId() {
		return mainQualifierId;
	}
	public void setMainQualifierId(Integer mainQualifierId) {
		this.mainQualifierId = mainQualifierId;
	}
	public Integer getDataApprovalId() {
		return dataApprovalId;
	}
	public void setDataApprovalId(Integer dataApprovalId) {
		this.dataApprovalId = dataApprovalId;
	}
	public Integer getMeasurementGradeId() {
		return measurementGradeId;
	}
	public void setMeasurementGradeId(Integer measurementGradeId) {
		this.measurementGradeId = measurementGradeId;
	}
	public Integer getDeploymentId() {
		return deploymentId;
	}
	public void setDeploymentId(Integer deploymentId) {
		this.deploymentId = deploymentId;
	}
	public Integer getIceAffectedId() {
		return iceAffectedId;
	}
	public void setIceAffectedId(Integer iceAffectedId) {
		this.iceAffectedId = iceAffectedId;
	}
	public BigDecimal getTurbidityPegged() {
		return turbidityPegged;
	}
	public void setTurbidityPegged(BigDecimal turbidityPegged) {
		this.turbidityPegged = turbidityPegged;
	}
	public Integer getProbeTypeId() {
		return probeTypeId;
	}
	public void setProbeTypeId(Integer probeTypeId) {
		this.probeTypeId = probeTypeId;
	}
	public Integer getInstrumentId() {
		return instrumentId;
	}
	public void setInstrumentId(Integer instrumentId) {
		this.instrumentId = instrumentId;
	}
	public Integer getDataLeadId() {
		return dataLeadId;
	}
	public void setDataLeadId(Integer dataLeadId) {
		this.dataLeadId = dataLeadId;
	}
	public Integer getRawFlagId() {
		return rawFlagId;
	}
	public void setRawFlagId(Integer rawFlagId) {
		this.rawFlagId = rawFlagId;
	}
	public Integer getDataQualificationId() {
		return dataQualificationId;
	}
	public void setDataQualificationId(Integer dataQualificationId) {
		this.dataQualificationId = dataQualificationId;
	}
	public Integer getAccuracyRatingId() {
		return AccuracyRatingId;
	}
	public void setAccuracyRatingId(Integer accuracyRatingId) {
		AccuracyRatingId = accuracyRatingId;
	}
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public BigDecimal getErValue() {
		return erValue;
	}
	public void setErValue(BigDecimal erValue) {
		this.erValue = erValue;
	}
	
	public Timestamp getMeasurementDateTimestamp() {
		return Timestamp.from(this.measurementDate);
	}
}
