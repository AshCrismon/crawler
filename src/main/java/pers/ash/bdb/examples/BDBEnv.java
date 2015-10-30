package pers.ash.bdb.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.SecondaryConfig;

import com.sleepycat.persist.EntityStore;

public class BDBEnv {

    private static Environment environment;
    private static EntityStore entityStore;
    private static DatabaseConfig defaultDbConfig = DatabaseConfigurator.getDefaultDatabaseConfig();
    private static DatabaseConfig classDbConfig = DatabaseConfigurator.getClassDatabaseConfig();
    private static ClassCatalog classCatalog;
    private static List<Database> openedDatabases = new ArrayList<Database>();
    
    /**
     * 设置数据库路径和只读属性
     * @param envHome
     * @param readOnly
     * @throws DatabaseException
     */
    public static void setup(File envHome) 
        throws DatabaseException {

        EnvironmentConfig environmentConfig = EnvironmentConfigurator.getInstance();
        environment = new Environment(envHome, environmentConfig);
    }
    
    public static Database openDatabase(String dbName, DatabaseConfig dbConfig){
    	Database database = null;
    	if(dbConfig == null){
    		database = openDatabase(dbName);
    	}else{
    		database = environment.openDatabase(null, dbName, dbConfig);
    	}
    	openedDatabases.add(database);
    	return database;
    }
    
    public static Database openDatabase(String dbName){
    	return environment.openDatabase(null, dbName, defaultDbConfig);
    }
    
    public static Database openClassDatabase(String dbName){
    	Database classDatabase = environment.openDatabase(null, dbName, classDbConfig);
    	classCatalog = new StoredClassCatalog(classDatabase);
    	return classDatabase;
    }
 
    public static Database openSecondaryDatabase(String databaseName, Database primaryDatabase, SecondaryConfig secondaryDbConfig){
    	return environment.openSecondaryDatabase(null, databaseName, primaryDatabase, secondaryDbConfig);
    }
    
    public static EntityStore getEntityentityStore() {
        return entityStore;
    }

    public static Environment getEnv() {
        return environment;
    }
    
    public static ClassCatalog getClassCatalog() {
		return classCatalog;
	}

	public static void setClassCatalog(ClassCatalog classCatalog) {
		BDBEnv.classCatalog = classCatalog;
	}

/**
    * 关闭指定数据库
    * @param database
    */
    public static void closeDatabase(Database database){
    	try {
			database.close();
			openedDatabases.remove(database);
		} catch (DatabaseException dbe) {

			System.err.println("Error closing database: " + dbe.toString());
			System.exit(-1);
		}
    }
    
    /**
     * 关闭数据库及数据库环境
     */
	public static void close() {
		try {
			for (int i = 0; i < openedDatabases.size(); i++) {
				openedDatabases.get(i).close();
			}
			if (environment != null) {
				environment.close();
			}
		} catch (DatabaseException dbe) {

			System.err.println("Error closing database environment: " + dbe.toString());
			System.exit(-1);
		}
	}
}
