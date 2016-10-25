package gov.usgs.wma.gcmrc.model;

/**
 * Specifies how a site should be/loaded and stored from Aquarius and in GDAWS
 * @author thongsav
 *
 */
public class SiteConfiguration {
	private Long localSiteId;
	private String remoteSiteId;
	private String parameter;
	private Integer groupId;
//	private Temporal highWaterMark; TODO, is this needed? What is it?
	
	public SiteConfiguration() {
		//empty for MyBatis
	}
	
	public SiteConfiguration(Long localSiteId, String remoteSiteId, String parameter, Integer groupId) {
		this.parameter = parameter;
		this.remoteSiteId = remoteSiteId;
		this.localSiteId = localSiteId;
		this.groupId = groupId;
	}
	
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	public String getRemoteSiteId() {
		return remoteSiteId;
	}
	public void setRemoteSiteId(String siteNumber) {
		this.remoteSiteId = siteNumber;
	}
	public Long getLocalSiteId() {
		return localSiteId;
	}
	public void setLocalSiteId(Long siteId) {
		this.localSiteId = siteId;
	}
	public Integer getGroupId() {
		return groupId;
	}
	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}
}
