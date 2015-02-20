package fr.ifremer.sensornanny.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

	public static Configuration getInstance() {
		return instance;
	}
	
	private static final String CONFIGURATION_COUCHBASE_FILENAME = "couchbase.properties";
	private static final String CONFIGURATION_GRABBER_FILENAME = "grabber.properties";
	
	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	private static Configuration instance = new Configuration();
	private Properties properties;
	
	private Configuration() {
		load(CONFIGURATION_COUCHBASE_FILENAME);
		load(CONFIGURATION_GRABBER_FILENAME);
	}
	
	public String[] cluster() {
		return get("cluster").split(",");
	}
	
	public String systemsBucket() {
		return get("systemsBucket");
	}

	public String systemsBucketPassword() {
		return get("systemsBucketPassword");
	}
	
	public String observationsBucket() {
		return get("observationsBucket");
	}
	
	public String observationsBucketPassword() {
		return get("observationsBucketPassword");
	}

	public String liveObservationsBucket() {
		return get("liveObservationsBucket");
	}
	
	public String liveObservationsBucketPassword() {
		return get("liveObservationsBucketPassword");
	}

	public String observationsViewDesign() {
		return get("observationsViewDesign");
	}
	
	public String observationsViewName() {
		return get("observationsViewName");
	}

	public String liveObservationsViewDesign() {
		return get("liveObservationsViewDesign");
	}

	public String liveObservationsViewName() {
		return get("liveObservationsViewName");
	}
	
	public String systemsViewDesign() {
		return get("systemsViewDesign");
	}
	
	public String systemsViewName() {
		return get("systemsViewName");
	}
	
	public String targetResultPathPattern() {
		return get("targetResultPathPattern");
	}

	public String restServiceQueryTimeFormat() {
		return get("restServiceQueryTimeFormat");
	}
	
	public String restServiceTimeFormat() {
		return get("restServiceTimeFormat");
	}

	private void load(final String propertyFile) {
		InputStream inputStream = Configuration.class.getClassLoader().getResourceAsStream(propertyFile);
		
		if (inputStream != null) {
			try {
				if (properties == null) {
					properties = new Properties();
				}
				properties.load(inputStream);
			} catch (IOException e) {
				properties = null;
				logger.log(Level.SEVERE, "Error while reading '" + propertyFile + "'", e);
			}
		} else {
			logger.log(Level.SEVERE, "Property file '" + propertyFile + "' not found in the classpath");
			throw new RuntimeException("Property file '" + propertyFile + "' not found in the classpath");
		}
	}
	
	private void checkProperties() {
		if (properties == null) {
			logger.log(Level.SEVERE, "Property file '" + CONFIGURATION_COUCHBASE_FILENAME + "' and '" + CONFIGURATION_GRABBER_FILENAME + "' not initialized");
			throw new RuntimeException("Property file '" + CONFIGURATION_COUCHBASE_FILENAME + "' and '" + CONFIGURATION_GRABBER_FILENAME + "' not initialized");
		}
	}
	
	private String get(String property) {
		checkProperties();
		String value = properties.getProperty(property);
		if (value == null) {
			logger.log(Level.SEVERE, "Property named " + property + " not found in '" + CONFIGURATION_COUCHBASE_FILENAME + "' nor '" + CONFIGURATION_GRABBER_FILENAME + "'");
			throw new RuntimeException("Property named " + property + " not found in '" + CONFIGURATION_COUCHBASE_FILENAME + "' nor '" + CONFIGURATION_GRABBER_FILENAME + "'");
		}
		return value;
	}
	
}
