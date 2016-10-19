package gov.usgs.wma.gcmrc.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 *
 * @author eeverman
 */
public class DataPullRunState {
	
	private static DataPullRunState singleton;
	private static final Object syncLock = new Object();
	
	private SqlSessionFactory sqlSessionFactory;
	
	
	private DataPullRunState() {
		//Singleton pattern
	}
	
	public static DataPullRunState instance() {
		if (singleton != null) {
			return singleton;
		} else {
			synchronized (syncLock) {
				if (singleton != null) {
					return singleton;
				} else {
					singleton = new DataPullRunState();
					return singleton;
				}
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
				return GdawsConfigLoader.buildSqlSessionFactory();
			} else {
				return sqlSessionFactory;
			}
		}
	}
}
