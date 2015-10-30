package pers.ash.bdb.examples;

import com.sleepycat.je.DatabaseConfig;

public class DatabaseConfigurator {

	private static DatabaseConfig defaultDatabaseConfig = null;
	private static DatabaseConfig classDatabaseConfig = null;

	private DatabaseConfigurator() {
	}

	public static DatabaseConfig getDefaultDatabaseConfig() {
		if(defaultDatabaseConfig == null){
			synchronized(DatabaseConfigurator.class){
				if(defaultDatabaseConfig == null){
					defaultDatabaseConfig = new DatabaseConfig();
					initializeDefaultDatabaseConfig(defaultDatabaseConfig);
				}
			}
		}
		return defaultDatabaseConfig;
	}
	
	public static DatabaseConfig getClassDatabaseConfig(){
		if(classDatabaseConfig == null){
			synchronized(DatabaseConfigurator.class){
				if(classDatabaseConfig == null){
					classDatabaseConfig = new DatabaseConfig();
					initializeClassDatabaseConfig(classDatabaseConfig);
				}
			}
		}
		return classDatabaseConfig;
	}

	private static void initializeDefaultDatabaseConfig(DatabaseConfig databaseConfig) {
		databaseConfig.setAllowCreate(true);		//没有数据库就创建
		databaseConfig.setDeferredWrite(false);		//true设置延迟写,sync或者close时提交操作到磁盘，false关闭
		databaseConfig.setTransactional(false);		//不开启事务
	}
	
	private static void initializeClassDatabaseConfig(DatabaseConfig databaseConfig) {
		databaseConfig.setAllowCreate(true);		//没有数据库就创建
		databaseConfig.setDeferredWrite(false);		//true设置延迟写,sync或者close时提交操作到磁盘，false关闭
		databaseConfig.setTransactional(false);		//不开启事务
		databaseConfig.setSortedDuplicates(false);	//不允许重复记录
	}
	
}
