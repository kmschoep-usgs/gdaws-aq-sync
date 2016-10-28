package gov.usgs.wma.gcmrc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
	private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);
	
	public static final String CONFIG_FILE_PROP_NAME = "gcmrc.config.file";
	
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
	
	public static Properties getConfigFromPropertiesFile(String fileName) {
		
		File propFile = null;
			
		String actualPathFileName = fileName;
		
		if (fileName.startsWith("~")) {
			actualPathFileName = fileName.replaceFirst("~", System.getProperty("user.home"));
		} else if (!fileName.contains(File.separator)) { //if no path in filename, use executable
			actualPathFileName = findExecutableDirectory() + File.separator + fileName;
		}
		
		propFile = loadFile(actualPathFileName);
		
		if (propFile != null) {
			LOG.info("Found config file {} at logical path '{}' (expands to '{}')", CONFIG_FILE_PROP_NAME, fileName, actualPathFileName);
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
			throw new RuntimeException("Unable to find config file " + fileName);
		}
		
	}
	
	private static File loadFile(String fileName) {
		File file = new File(fileName);
		
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
