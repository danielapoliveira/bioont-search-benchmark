package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Configuration interface for this API.
 * <p>
 * Usage Example: Configuration.getProperty(Configuration.STORE_PATH)
 * <p>
 * See {@link Configuration#getProperty(String)}
 *
 * @author anila butt
 * @author Daniela Oliveira
 */
public class Configuration {

	/** Property key for ontology path */
	public static final String STORE_METADATA = "store.metadata";

	/** Property key for virtuoso instance */
	public static final String VIRTUOSO_INSTANCE = "virtuoso.instance";

	/** Property key for virtuoso port */
	public static final String VIRTUOSO_PORT = "virtuoso.port";

	/** Property key for virtuoso username */
	public static final String VIRTUOSO_USERNAME = "virtuoso.username";

	/** Property key for virtuoso password */
	public static final String VIRTUOSO_PASSWORD = "virtuoso.password";

	/**Property key for the core of the solr server instance */
	public static final String SOLR_INSTANCE = "solr.instance";

	/**Property key to specify the BioPortal API key of the user */
    public static final String BIOPORTAL_APIKEY = "bioportal.apikey";

    public static final String URIS_FILENAME = "uris.file";

    public static final String QUERY_FILENAME = "query.file";
	
	public static final String SAVE_PATH = "save.path";

	public static final String GROUND_TRUTH_PATH = "gt.path";

	/** Default instance of this class */
	private static Configuration instance;

	/** Default logger */
	private Logger logger;

	/** Default properties */
	private Properties properties = new Properties();

	/**
	 * External classes should invoke static methods
	 * instead of instantiating this class.
	 *
	 * @see #getProperty(String)
	 */
	private Configuration() {
		logger = Logger.getLogger(getClass().getName());
		try {
            final BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\danoli\\Desktop\\CBRBench\\bioont-search-benchmark\\userinput\\config.properties"));
                properties.load(reader);
			// Try loading configuration file otherwise fall back to a default path
			//properties.load(getClass().getResourceAsStream("resources/config.properties"));
		} catch (IOException iox) {

			logger.severe("Error in reading store configuration "+ iox);

		}
	}

	/**
	 * Returns default configuration object.
	 */
	private static Configuration getDefaults() {
		if(instance==null) {
			instance = new Configuration();
		}
		return instance;
	}

	/**
	 * Gets value of the specified configuration property.
	 *
	 * @param key Configuration key
	 */
	public static String getProperty(String key) {
		Configuration config = Configuration.getDefaults();
		return config.properties.getProperty(key);
	}

	/**
	 * Loads program properties from a specified file name.
	 *
	 * <p>If there is any error, default values will be used. This method is expected to never fail.</p>
	 *
	 * @param fileName file to read program properties from
	 */
	public void loadFromFile(final String fileName) {
		try (final BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			properties.load(reader);
		} catch (FileNotFoundException e) {
			logger.warning("Properties file "+ fileName + " not found, using defaults.");
		} catch (IOException e) {
			logger.warning("I/O exception reading properties file "+ fileName +" (now using defaults): " + e.getMessage());
		}
	}

}
