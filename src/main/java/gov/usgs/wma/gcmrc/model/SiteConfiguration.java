package gov.usgs.wma.gcmrc.model;

import java.time.ZonedDateTime;

/**
 * Specifies how a site should be/loaded and stored from Aquarius and in GDAWS
 * 
 * Unique instances are ID'ed in the database by the combo of localSiteId and
 * localParamId.
 * 
 * @author thongsav
 */
public class SiteConfiguration {
	private Integer localSiteId;		//One part (of 2) of the db identifier
	private Integer localParamId;	//One part (of 2) of the db identifier
	private String remoteSiteId;
	private String remoteParamId;
	private String pCode;
	private ZonedDateTime neverPullBefore;
	private ZonedDateTime neverPullAfter;
	private ZonedDateTime lastNewPullStart;
	private ZonedDateTime lastNewPullEnd;
	
	public SiteConfiguration() {
		//empty for MyBatis
	}
	
	/**
	 * Full constructor.
	 * @param localSiteId
	 * @param localParamId
	 * @param remoteSiteId
	 * @param pCode
	 * @param lastNewPullStart
	 * @param lastNewPullEnd
	 * @param timeshiftMinutes
	 * @param aqParam 
	 */
	public SiteConfiguration(Integer localSiteId, Integer localParamId, String remoteSiteId, String remoteParamId, String pCode, ZonedDateTime neverPullBefore, ZonedDateTime neverPullAfter, ZonedDateTime lastNewPullStart, ZonedDateTime lastNewPullEnd, Integer proxySiteId, Integer timeshiftMinutes) {
		this.pCode = pCode;
		this.remoteSiteId = remoteSiteId;
		this.localSiteId = localSiteId;
		this.localParamId = localParamId;
		this.remoteParamId = remoteParamId;
		this.neverPullBefore = neverPullBefore;
		this.neverPullAfter = neverPullAfter;
		this.lastNewPullStart = lastNewPullStart;
		this.lastNewPullEnd = lastNewPullEnd;
	}
	
	public Integer getLocalSiteId() {
		return localSiteId;
	}
	public void setLocalSiteId(Integer siteId) {
		this.localSiteId = siteId;
	}
	
	public Integer getLocalParamId() {
		return localParamId;
	}
	public void setLocalParamId(Integer groupId) {
		this.localParamId = groupId;
	}
	
	public String getPCode() {
		return pCode;
	}
	public void setPCode(String parameter) {
		this.pCode = parameter;
	}
	public String getRemoteSiteId() {
		return remoteSiteId;
	}
	public void setRemoteSiteId(String siteNumber) {
		this.remoteSiteId = siteNumber;
	}

	public String getRemoteParamId() {
		return remoteParamId;
	}

	public void setRemoteParamId(String aqParam) {
		this.remoteParamId = aqParam;
	}
	
	public String getpCode() {
		return pCode;
	}

	public void setpCode(String pCode) {
		this.pCode = pCode;
	}

	public ZonedDateTime getNeverPullBefore() {
		return neverPullBefore;
	}

	public void setNeverPullBefore(ZonedDateTime neverPullBefore) {
		this.neverPullBefore = neverPullBefore;
	}

	public ZonedDateTime getNeverPullAfter() {
		return neverPullAfter;
	}

	public void setNeverPullAfter(ZonedDateTime neverPullAfter) {
		this.neverPullAfter = neverPullAfter;
	}

	public ZonedDateTime getLastNewPullStart() {
		return lastNewPullStart;
	}

	public void setLastNewPullStart(ZonedDateTime lastNewPullStart) {
		this.lastNewPullStart = lastNewPullStart;
	}

	public ZonedDateTime getLastNewPullEnd() {
		return lastNewPullEnd;
	}

	public void setLastNewPullEnd(ZonedDateTime lastNewPullEnd) {
		this.lastNewPullEnd = lastNewPullEnd;
	}
}
