package gov.usgs.wma.gcmrc.dao;

import java.io.InputStream;
import java.util.Properties;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class GdawsDaoFactory {
	private static final Object syncLock = new Object();
	private static final String GDAWS_ENVIRONMENT = "gdaws.db";
	
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
				sqlSessionFactory = buildSqlSessionFactory(properties);
			}
			return sqlSessionFactory;
		}
	}
	
	public static SqlSessionFactory buildSqlSessionFactory(Properties properties) throws RuntimeException {
		
		try {
			String resource = "mybatis/mybatis.conf.xml";
			InputStream inputStream = Resources.getResourceAsStream(resource);

			SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties.getProperty(GDAWS_ENVIRONMENT), properties);

			return sqlSessionFactory;
			
		} catch (Exception e) {
			throw new RuntimeException (e);
		}
	}
	
}
