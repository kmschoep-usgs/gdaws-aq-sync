package gov.usgs.wma.gcmrc.service;

import java.util.ArrayList;
import java.util.List;

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

public class GdawsConfigLoader {
	private static final Logger log = LoggerFactory.getLogger(GdawsConfigLoader.class);
	
	private static final String PROP_FILE_NAME = "gcmrc-sync-config.properties";
	private static final String DEV_CONF_DIR = "/datausgs/projects/gdaws-aq-sync/config";
	private static final String EXECUTABLE_CONF_DIR = "";
	private static final String USER_CONF_DIR = "~/gdaws-aq-sync/config";
	
	public static List<SiteConfiguration> loadSiteConfiguration() {
		List<SiteConfiguration> configs = new ArrayList<>();
		
		//TODO load site config from file or database
		configs.add(new SiteConfiguration("Discharge", "01010000", "01010000", 99, 99));
		
		return configs;
	}
	
	public static SqlSessionFactory buildSqlSessionFactory() throws RuntimeException {
		
		try {
			String resource = "org/mybatis/example/mybatis-config.xml";
			InputStream inputStream = Resources.getResourceAsStream(resource);

			Properties props = getProperties();
			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, props);
			return sqlSessionFactory;
			
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
	public static Properties getProperties() throws Exception {
		
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
			
			if (propFile != null) break;
			
		}
		
		if (propFile != null) {
			
			Properties props = new Properties();

			try (FileInputStream in = new FileInputStream(propFile)) {
				props.load(in);
			}
			
			return props;
			
		} else {
			log.error("Unable to find a path containing the {} conig file.)", PROP_FILE_NAME);
			throw new Exception("Unable to find config file");
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
			log.debug("Unable to find a config file at '{}')", file.getPath());
			return null;
		}
	}
	
	private static String findExecutableDirectory() {
		try {
			String path = GdawsConfigLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File jarFile = new File(path);
			File jarDir = jarFile.getParentFile();

			if (jarDir.exists()) {
				return jarDir.getCanonicalPath();
			} else {
				log.debug("Unable to find a directory containing the running jar file (maybe this is not running from a jar??)");
				return null;
			}
		} catch (Exception e) {
			log.error("Attempting to find the executable directory containing the running jar file caused an exception", e);
			return null;
		}
	}
}
