package gov.usgs.wma.gcmrc;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import gov.usgs.aqcu.data.service.DataService;
import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.wma.gcmrc.service.AquariusRetrievalService;
import gov.usgs.wma.gcmrc.service.TimeSeriesDataCorrectedService;
import gov.usgs.wma.gcmrc.util.ConfigLoader;
import gov.usgs.wma.gcmrc.util.UnmodifiableProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author eeverman
 */
public class RunConfiguration {
	
	private static final Logger LOG = LoggerFactory.getLogger(RunConfiguration.class);
	
	private static RunConfiguration singleton;
	private static final Object syncLock = new Object();
	
	private Properties props = null;
	private DataService aquariusDataService;
	private TimeSeriesDataCorrectedService timeSeriesDataService; 
	
	private RunConfiguration() {
		//Singleton pattern
	}
	
	public static RunConfiguration instance() {
		if (singleton != null) {
			return singleton;
		} else {
			synchronized (syncLock) {
				if (singleton == null) {
					singleton = new RunConfiguration();
				}
				return singleton;
			}
		}
	}
	
//	public DataService getAquariusDataService() {
//		if (aquariusDataService != null) {
//			return aquariusDataService;
//		} else {
//			return buildAquariusDataService();
//		}
//	}
//	
	public TimeSeriesDataCorrectedService getTimeSeriesDataService(){
		if (timeSeriesDataService != null) {
			return timeSeriesDataService;
		} else {
			return buildTimeSeriesDataService();
		}
	}
	
	private TimeSeriesDataCorrectedService buildTimeSeriesDataService() {
		synchronized (syncLock) {
			timeSeriesDataService = new TimeSeriesDataCorrectedService(
				new AquariusRetrievalService(getProperty("aquarius.service.endpoint",""),
					getProperty("aquarius.service.user", ""),
					getProperty("aquarius.service.password", ""),
					3));
			return timeSeriesDataService;
		}
	}
	
//	private DataService buildAquariusDataService() {
//		synchronized (syncLock) {
//			if (aquariusDataService == null) {
//				aquariusDataService = new DataService();
//			}
//			return aquariusDataService;
//		}
//	}
	
	public Properties getProperties() {
		if (props != null) {
			return props;
		} else {
			return buildProperties();
		}
	}
	
	/**
	 * Fetches a property or the defaultValue if the property is null.
	 * @param prop The property name
	 * @param defaultValue
	 * @return 
	 */
	public String getProperty(String prop, String defaultValue) {
		return getProperties().getProperty(prop, defaultValue);
	}
	
	/**
	 * Returns the integer value of a property if it is non-null and can be parsed as int.
	 * 
	 * Otherwise it returns the defaultValue.
	 * @param prop The property name
	 * @param defaultValue
	 * @return 
	 */
	public Integer getIntProperty(String prop, Integer defaultValue) {
		String s = getProperties().getProperty(prop);
		
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (Exception e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Returns the LocalDateTime value of a property if it is non-null and can be parsed as LocalDateTime.
	 * 
	 * Otherwise it returns the defaultValue.
	 * @param prop The property name
	 * @param defaultValue
	 * @return 
	 */
	public LocalDateTime getDateTimeProperty(String prop, LocalDateTime defaultValue) {
		String s = getProperties().getProperty(prop);
		
		if (s != null) {
			try {
				return LocalDateTime.parse(s.trim(), DateTimeFormatter.ISO_DATE_TIME);
			} catch (Exception e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}
	
	/**
	 * Returns an array from a comma-separated list property if all of the 
	 * values are non-null and can be parsed as the provided Type
	 * 
	 * Otherwise it returns an empty array of the provided type.
	 * @param prop The property name
	 * @param type The class to convert the property elements to
	 * @return 
	 */
	public <T> ArrayList<T> getArrayProperty(String prop, Class<T> type){
		String s = getProperties().getProperty(prop);
		ArrayList<T> toReturn = new ArrayList<>();
				
		if (s != null) {
			for(String st : s.split(",")){
				try {
					toReturn.add(type.getConstructor(String.class).newInstance(st.trim()));
				} catch(Exception e) {
					return new ArrayList<>();
				}
			}
			
			return toReturn;
		} else {
			return new ArrayList<>();
		}
	}
	
	/**
	 * Build the Properties list
	 * @return 
	 */
	private Properties buildProperties() {
		synchronized (syncLock) {
			if (null == props) {
				String propFileLocation = new DynamicReadOnlyProperties().get(ConfigLoader.CONFIG_FILE_PROP_NAME);
				
				if(!StringUtils.isBlank(propFileLocation)) {
					loadFilePropsIntoSystemProps(propFileLocation);
				}
				
				props = new UnmodifiableProperties(System.getProperties());

			}
			return props;
		}
	}
	
	private void loadFilePropsIntoSystemProps(String file) {
		Properties fromPropsFile = ConfigLoader.getConfigFromPropertiesFile(file);
		
		for (String key : fromPropsFile.stringPropertyNames()) {
			System.setProperty(key, fromPropsFile.getProperty(key));
		}
	}
	
}
