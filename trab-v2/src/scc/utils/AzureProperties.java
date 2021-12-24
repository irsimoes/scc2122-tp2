package scc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AzureProperties
{
	public static final String BLOB_KEY = "BlobStoreConnection";
	public static final String COSMOSDB_KEY = "COSMOSDB_KEY";
	public static final String COSMOSDB_URL = "COSMOSDB_URL";
	public static final String COSMOSDB_DATABASE = "COSMOSDB_DATABASE";

	public static final String PROPS_FILE = "azurekeys.props";
	private static Properties props;
	
	public static synchronized Properties getProperties() {
		if( props == null) {
			props = new Properties();
			try {
				props.load( new FileInputStream(PROPS_FILE));
			} catch (IOException e) {
				// do nothing
			}
		}
		return props;
	}

}
