package gov.usgs.wma.gcmrc.model;

import gov.usgs.aqcu.data.service.DataService;
import gov.usgs.wma.gcmrc.util.ConfigLoader;
import gov.usgs.wma.gcmrc.util.UnmodifiableProperties;
import java.util.Properties;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 *
 * @author eeverman
 */
public class RunConfiguration {
	
	private static RunConfiguration singleton;
	private static final Object syncLock = new Object();
	
	private Properties props = null;
	private DataService aquariusDataService;
	private SqlSessionFactory sqlSessionFactory;
	
	
	
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
	
	
	public SqlSessionFactory getSqlSessionFactory() {
		if (sqlSessionFactory != null) {
			return sqlSessionFactory;
		} else {
			return buildSqlSessionFactory();
		}
	}
	
	private SqlSessionFactory buildSqlSessionFactory() {
		synchronized (syncLock) {
			if (sqlSessionFactory == null) {
				sqlSessionFactory = ConfigLoader.buildSqlSessionFactory(getProperties());
			}
			return sqlSessionFactory;
		}
	}
	
	public DataService getAquariusDataService() {
		if (aquariusDataService != null) {
			return aquariusDataService;
		} else {
			return buildAquariusDataService();
		}
	}
	
	private DataService buildAquariusDataService() {
		synchronized (syncLock) {
			if (aquariusDataService == null) {
				aquariusDataService = new DataService();
			}
			return aquariusDataService;
		}
	}
	
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
	 * Build the Properties list
	 * @return 
	 */
	private Properties buildProperties() {
		synchronized (syncLock) {
			if (null == props) {
				Properties systemProps = System.getProperties();
				
				if(systemProps.contains(ConfigLoader.PROP_FILE_NAME)) {
					loadFilePropsIntoSystemProps(systemProps.getProperty(ConfigLoader.PROP_FILE_NAME));
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
