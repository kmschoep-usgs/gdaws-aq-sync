package gov.usgs.wma.gcmrc.util;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.wma.gcmrc.model.SiteConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
	
	private static final String PROP_FILE_NAME = "gcmrc-sync-config.properties";
	private static final String DEV_CONF_DIR = "/datausgs/projects/gdaws-aq-sync/config";
	private static final String EXECUTABLE_CONF_DIR = "";
	private static final String USER_CONF_DIR = "~/gdaws-aq-sync/config";
	
//	public static List<SiteConfiguration> loadSiteConfiguration() {
//		List<SiteConfiguration> configs = new ArrayList<>();
//		
//		//TODO load site config from file or database
//		configs.add(new SiteConfiguration(1234321L, "01010000", "Discharge", 99));
//		
//		return configs;
//	}
	
	public static SqlSessionFactory buildSqlSessionFactory(Properties properties) throws RuntimeException {
		
		try {
			String resource = "mybatis/mybatis.conf.xml";
			InputStream inputStream = Resources.getResourceAsStream(resource);

			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);
			return sqlSessionFactory;
			
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	public static Properties getConfigFromPropertiesFile() {
		
		ArrayList<String> dirs = new ArrayList(3);
		dirs.add(DEV_CONF_DIR);
		dirs.add(EXECUTABLE_CONF_DIR);
		dirs.add(USER_CONF_DIR);
		
		
		File propFile = null;
		
		
		for (String path : dirs) {
			
			String actualPath = path;
			
			if (path.startsWith("~")) {
				actualPath = path.replaceFirst("~", System.getProperty("user.home"));
			} else if (path.equals("")) {
				actualPath = findExecutableDirectory();
			}
			
			propFile = findConfigFromPath(actualPath, PROP_FILE_NAME);
			
			if (propFile != null) {
				LOG.info("Found config file {} at logical path '{}' (expands to '{}')", PROP_FILE_NAME, path, actualPath);
				break;
			}
			
		}
		
		if (propFile != null) {
			
			Properties props = new Properties();

			try (FileInputStream in = new FileInputStream(propFile)) {
				props.load(in);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			return props;
			
		} else {
			LOG.error("Unable to find a path containing the {} conig file.)", PROP_FILE_NAME);
			throw new RuntimeException("Unable to find config file");
		}
		
	}
	
	private static File findConfigFromPath(String path, String fileName) {
		
		if (path == null) return null;
		
		if (path.startsWith("~")) {
			path = path.replaceFirst("~", System.getProperty("user.home"));
		}
		
		File dir = new File(path);
		File file = new File(dir, fileName);
		
		if (file.exists()) {
			return file;
		} else {
			LOG.debug("Unable to find a config file at '{}')", file.getPath());
			return null;
		}
	}
	
	private static String findExecutableDirectory() {
		try {
			String path = ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File jarFile = new File(path);
			File jarDir = jarFile.getParentFile();

			if (jarDir.exists()) {
				return jarDir.getCanonicalPath();
			} else {
				LOG.debug("Unable to find a directory containing the running jar file (maybe this is not running from a jar??)");
				return null;
			}
		} catch (Exception e) {
			LOG.error("Attempting to find the executable directory containing the running jar file caused an exception", e);
			return null;
		}
	}
}
