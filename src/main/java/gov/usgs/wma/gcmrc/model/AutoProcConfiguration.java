package gov.usgs.wma.gcmrc.model;

/**
 * 
 * 
 * @author thongsav
 */
public class AutoProcConfiguration {
	private Integer siteId;
	private String loadCalculationName;
	private String configParamaterName;
	private Double configParamaterValue;
	
	public AutoProcConfiguration() {
		//empty for MyBatis
	}

	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer siteId) {
		this.siteId = siteId;
	}

	public String getLoadCalculationName() {
		return loadCalculationName;
	}

	public void setLoadCalculationName(String loadCalculationName) {
		this.loadCalculationName = loadCalculationName;
	}

	public String getConfigParamaterName() {
		return configParamaterName;
	}

	public void setConfigParamaterName(String configParamaterName) {
		this.configParamaterName = configParamaterName;
	}

	public Double getConfigParamaterValue() {
		return configParamaterValue;
	}

	public void setConfigParamaterValue(Double configParamaterValue) {
		this.configParamaterValue = configParamaterValue;
	}
	
	
}
