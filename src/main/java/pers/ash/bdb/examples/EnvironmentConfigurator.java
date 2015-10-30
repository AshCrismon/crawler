package pers.ash.bdb.examples;

import com.sleepycat.je.EnvironmentConfig;

public class EnvironmentConfigurator {

	private static EnvironmentConfig environmentConfig = null;
	
	private EnvironmentConfigurator() {
	}

	public static EnvironmentConfig getInstance(){
		if(environmentConfig == null){
			synchronized(EnvironmentConfigurator.class){
				if(environmentConfig == null){
					environmentConfig = new EnvironmentConfig();
					initializeConfig(environmentConfig);
				}
			}
		}
        return environmentConfig;
	}

	private static void initializeConfig(EnvironmentConfig environmentConfig2) {
		environmentConfig.setReadOnly(false);
        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(false);
	}
}
