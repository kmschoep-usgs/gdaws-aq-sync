package gov.usgs.wma.gcmrc.model;

/**
 * Specifies how a site should be/loaded and stored from Aquarius and in GDAWS
 * @author thongsav
 *
 */
public class SiteConfiguration {
	private String parameter;
	private String siteNumber;
	private String siteId;
	private Integer groupId;
	private Integer sourceId;
//	private Temporal highWaterMark; TODO, is this needed? What is it?
	
	public SiteConfiguration(String parameter, String siteNumber, String siteId, Integer groupId, Integer sourceId) {
		super();
		this.parameter = parameter;
		this.siteNumber = siteNumber;
		this.siteId = siteId;
		this.groupId = groupId;
		this.sourceId = sourceId;
	}
	
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	public String getSiteNumber() {
		return siteNumber;
	}
	public void setSiteNumber(String siteNumber) {
		this.siteNumber = siteNumber;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
}
