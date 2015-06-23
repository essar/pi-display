package it.essar.pidisplay.display;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayProperties
{
	private static Logger log = LogManager.getLogger(DisplayProperties.class);
	
	private static final Properties props = new Properties();
	
	static {
		
		// Load all system properties
		props.putAll(System.getProperties());
		
		try {
			
			props.load(DisplayProperties.class.getResourceAsStream("display.properties"));
			
		} catch(IOException ioe) {
			
			log.error("Unable to load properties from file", ioe);
			
		}
		
	}

	public static URL getDataConnectionURL() {
		
		String dataConnectionURIStr = props.getProperty("pidisplay.net.data.url", "http://localhost:80");
		try {
			
			return new URL(dataConnectionURIStr);
			
		} catch(MalformedURLException mue) {
			
			log.warn("Invalid URI: {}", dataConnectionURIStr);
		}
		
		return null;
		
	}
}
