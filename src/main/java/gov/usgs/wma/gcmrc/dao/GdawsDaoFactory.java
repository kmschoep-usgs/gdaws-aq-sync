package gov.usgs.wma.gcmrc.dao;

import java.util.Properties;
import org.apache.ibatis.session.SqlSessionFactory;
import gov.usgs.wma.gcmrc.util.ConfigLoader;

public class GdawsDaoFactory {
	private static final Object syncLock = new Object();
	
	private SqlSessionFactory sqlSessionFactory;
	private Properties properties;
	
	public GdawsDaoFactory(Properties properties) {
		this.properties = properties;
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
				sqlSessionFactory = ConfigLoader.buildSqlSessionFactory(properties);
			}
			return sqlSessionFactory;
		}
	}
}
