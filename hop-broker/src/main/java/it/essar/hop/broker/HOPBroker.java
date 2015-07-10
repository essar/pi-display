package it.essar.hop.broker;

import java.io.IOException;
import java.util.Properties;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides the embedded JMS Broker used within the HOP server process.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
public class HOPBroker
{

	private static final Logger log = LogManager.getLogger(HOPBroker.class);
	private static final Properties brokerProps = new Properties();
	private static final String PROPERTY_FILE = "/broker.properties";
	private static BrokerService broker;
	
	static String DEF_BROKER_NAME = "hop-broker";
	static String DEF_TCP_ADDRESS = "tcp://localhost:61616";
	
	/**
	 * Load the properties file.
	 */
	static {
		
		loadProperties();
		
	}
	
	/**
	 * Initialise a BrokerService.
	 * @return if the broker is successfully created and initialised.
	 */
	private static boolean initBroker() {
		
		if(broker == null) {
			
			broker = new BrokerService();
			
		}
		
		try {

			broker.addConnector(brokerProps.getProperty("hop.broker.tcpAddress", DEF_TCP_ADDRESS));
			
		} catch(Exception e) {
			
			log.fatal("Unable to bind broker to TCP address", e);
			return false;
			
		}
		
		broker.setBrokerName(brokerProps.getProperty("hop.broker.brokerName", DEF_BROKER_NAME));
		broker.setPersistent(false);
		
		return true;
		
	}
	
	/**
	 * Loads properties from the file specified by {@see PROPERTY_FILE}.
	 */
	private static void loadProperties() {
		
		try {
		
			brokerProps.load(HOPBroker.class.getResourceAsStream(PROPERTY_FILE));

		} catch(IOException ioe) {
			
			log.error("Unable to load properties from {}: {}", PROPERTY_FILE, ioe.getMessage());
			
		}
	}
	
	/**
	 * Gets a {@see ConnectionFactory} object that refers to the embedded broker service.
	 * @return the ConnectionFactory.
	 */
	static ActiveMQConnectionFactory getLocalConnectionFactory() {
		
		if(!isStarted()) {
			
			throw new IllegalStateException("Broker not running");
			
		}

		return new ActiveMQConnectionFactory(broker.getVmConnectorURI());
		
	}
	

	/**
	 * Checks if the broker is currently started.
	 * @return 
	 */
	static boolean isStarted() {
		
		return broker != null && broker.isStarted();
		
	}

	/**
	 * Starts the broker. Blocks until startup is complete.
	 */
	public static void startBroker() {
		
		if(! initBroker()) {
			
			throw new IllegalStateException("Broker did not initialise correctly");
			
		}
		
		try {

			broker.start();
			broker.waitUntilStarted();
			
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to start broker", e);
			
		}
	}
	
	/**
	 * Stops the broker. Blocks until the shutdown is complete.
	 */
	public static void stopBroker() {
		
		try {
			
			if(broker != null) {
				
				broker.stop();
				broker.waitUntilStopped();
				
			}
			
		} catch(Exception e) {
			
			log.error("Unable to stop broker", e);
			
		}
		
		// Release resources
		broker = null;
		
	}
}
