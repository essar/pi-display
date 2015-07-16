package it.essar.hop.broker;

import it.essar.hop.broker.api.BrokerInfoType;
import it.essar.hop.broker.api.ConnectionSet;
import it.essar.hop.broker.api.ConnectionStatisticsType;
import it.essar.hop.broker.api.ConnectionType;
import it.essar.hop.broker.api.DestinationSet;
import it.essar.hop.broker.api.DestinationType;
import it.essar.hop.broker.api.ObjectFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides the embedded JMS Broker used within the HOP server process.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
@Deprecated
public class HOPBroker
{

	private static final Logger log = LogManager.getLogger(HOPBroker.class);
	private static final BrokerMonitor monitor = new BrokerMonitor();
	private static final Properties brokerProps = new Properties();
	private static final String PROPERTY_FILE = "/broker.properties";
	private static HOPBroker broker;
	
	static String DEF_BROKER_NAME = "hop-broker";
	static String DEF_TCP_ADDRESS = "tcp://localhost:61616";
	
	public static final String CF_JNDI_NAME = "HOPBrokerCF";
	
	private final BrokerService svc;
	
	/**
	 * Load the properties file.
	 */
	static {
		
		loadProperties();
		
	}

	/**
	 * Instantiates a new HOPBroker instance.
	 */
	private HOPBroker() {
		
		svc = new BrokerService();
		svc.setBrokerName(brokerProps.getProperty("hop.broker.brokerName", DEF_BROKER_NAME));
		svc.setPersistent(false);
			
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
	 * Checks if the broker service has been started.
	 * @return true if the broker is running, false if it has not been started or has been stopped.
	 */
	static boolean isBrokerStarted() {
		
		return (broker != null && broker.svc.waitUntilStarted(500L));
		
	}
	
	/**
	 * Blocks until the broker has been started.
	 * @return true if the broker is started, false otherwise.
	 */
	static boolean waitUntilStarted() {
		
		try {
			
			while(isBrokerStarted()) {
				
				log.debug("Broker not started");
				Thread.sleep(100);
				
			}
			
			return broker.svc.waitUntilStarted();

		} catch(InterruptedException ie) {
			
			return false;
			
		}
	}
	
	/**
	 * Gets the current broker object.
	 * @return the current broker.
	 */
	public static HOPBroker getBroker() {
		
		return broker;
		
	}
	
	/**
	 * Starts the default broker. Blocks until startup is complete.
	 */
	public static HOPBroker startBroker() { 
		
		if(broker == null) {
			
			broker = new HOPBroker();
			
		}
		
		monitor.startMonitor();
		waitUntilStarted();
		log.info("Broker service started");

		return getBroker();

	}
	
	/**
	 * Stops the default broker. Blocks until the shutdown is complete.
	 */
	public static void stopBroker() {
		
		if(broker != null) {
			
			monitor.stopMonitor();
			broker.svc.waitUntilStopped();
			log.info("Broker service stopped");
			
		}
		
		// Release resources
		broker = null;
		
	}

	
	/**
	 * Initialise the BrokerService.
	 * @return if the broker is successfully created and initialised.
	 */
	private boolean initBroker() {
		
		try {

			log.info("Initiating broker");
			svc.addConnector(brokerProps.getProperty("hop.broker.tcpAddress", DEF_TCP_ADDRESS));
			
		} catch(Exception e) {
			
			log.fatal("Unable to bind broker to TCP address: {}", e.getMessage());
			return false;
			
		}
		
		return true;
		
	}
	
	/**
	 * Checks if the current broker is alive. Will attempt to make a connection to the internal URI of the broker.
	 * @return true if the broker is alive, false otherwise.
	 */
	private boolean isAlive() {
	
		if(isStarted()) {
		
			try {
			
				getLocalConnectionFactory().createConnection().close();
				return true;
				
			} catch(JMSException jmse) {
			
				log.warn("Broker connection test failure: {}", jmse.getMessage());
				
			}
		}
		
		log.debug("Broker not alive");
		return false;
		
	}
	
	/**
	 * Starts the broker
	 */
	private synchronized void start() {
		
		if(!initBroker()) {
			
			throw new IllegalStateException("Broker did not initialise correctly");
			
		}
		
		try {

			log.info("Starting broker {}", getName());
			svc.start();
			log.info("Broker {} {}", getName(), svc.isStarted() ? "started" : "not started");
			
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to start broker", e);
			
		}
	}
	
	/**
	 * Stops the broker.
	 */
	private synchronized void stop() {
		
		try {
			
			log.info("Stopping broker {}", getName());
			svc.stop();
			log.info("Broker {} {}", getName(), svc.isStopped() ? "stopped" : "not stopped");
				
		} catch(Exception e) {
			
			log.error("Unable to stop broker", e);
			
		}
	}
	
	void abort() {
		
		stop();
		
	}
	
	/**
	 * Gets the name of the broker.
	 * @return the name of this broker.
	 */
	String getName() {
		
		return svc.getBrokerName();
		
	}

	/**
	 * Checks if the broker is currently running.
	 * @return 
	 */
	synchronized boolean isStarted() {
		
		return svc.isStarted();
		
	}
	
	/**
	 * Returns information about the broker.
	 * @return current broker runtime and administration information.
	 */
	public BrokerInfoType getBrokerInfo() {
		
		ObjectFactory of = new ObjectFactory();
		
		BrokerInfoType bit = of.createBrokerInfoType();

		try {
			
			bit.setBrokerID(svc.getBroker().getBrokerId().getValue());
			bit.setBrokerName(svc.getBrokerName());
			
			ConnectionSet cs = of.createConnectionSet();
			for(Connection c : svc.getBroker().getClients()) {
				
				ConnectionType ct = of.createConnectionType();
				ct.setConnectionID(c.getConnectionId());
				ct.setRemoteAddress(c.getRemoteAddress());
				ct.setNetworkConnection(c.isNetworkConnection());
				
				ConnectionStatisticsType cst = of.createConnectionStatisticsType();
				cst.setDequeues(BigInteger.valueOf(c.getStatistics().getDequeues().getCount()));
				cst.setEnqueues(BigInteger.valueOf(c.getStatistics().getEnqueues().getCount()));
				ct.setStatistics(cst);
				
				cs.getConnection().add(ct);
				
			}
			bit.setClients(cs);

			DestinationSet ds = of.createDestinationSet();
			for(ActiveMQDestination d : svc.getBroker().getDestinations()) {
				
				DestinationType dt = of.createDestinationType();
				dt.setPhysicalName(d.getPhysicalName());
				dt.setQualifiedName(d.getQualifiedName());
				dt.setQueue(d.isQueue());
				dt.setTemporary(d.isTemporary());
				dt.setTopic(d.isTopic());
				
				ds.getDestination().add(dt);
				
			}
			bit.setDestinations(ds);
			
		} catch(Exception e) {
			
			log.error(e);
	
		}
		return bit;
		
	}
	
	/**
	 * Gets a {@see ConnectionFactory} object that refers to the embedded broker service.
	 * @return the ConnectionFactory.
	 */
	public ActiveMQConnectionFactory getLocalConnectionFactory() {
		
		if(!isStarted()) {
			
			throw new IllegalStateException("Broker not running");
			
		}

		return new ActiveMQConnectionFactory(svc.getVmConnectorURI());
		
	}
	
	/**
	 * Monitor class for the broker. Ensures that the broker is automatically restarted should it stop for any reason.
	 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
	 * @version 1.0
	 */
	static class BrokerMonitor implements Runnable
	{
		
		private boolean running;
		private Thread monitorThread;
		
		/**
		 * Checks if the broker is alive.
		 * @return true if the broker is alive, false if it is <tt>null</tt> or not alive.
		 * @see HOPBroker#isAlive()
		 */
		private boolean isBrokerAlive() {
			
			if(broker == null) {
				
				return false;
				
			}
			
			return broker.isAlive();
			
		}
		
		/**
		 * Restarts the broker if it becomes unresponsive.
		 */
		private void restartBroker() {
			
			if(broker != null && broker.isStarted()) {
			
				broker.stop();
				
			}
			
			broker = new HOPBroker();
			broker.start();
			
		}

		/**
		 * Starts the monitor.
		 */
		void startMonitor() {

			log.info("Starting monitor for {}", broker.getName());
			monitorThread = new Thread(this, broker.getName() + "-Monitor");
			monitorThread.setDaemon(true);
			monitorThread.start();
			
		}
		
		/**
		 * Stops the monitor.
		 */
		void stopMonitor() {
			
			log.info("Stopping monitor for {}", broker.getName());
			running = false;
			monitorThread.interrupt();
			
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			
			running = true;
			
			while(running) {
			
				try {
					
					if(isBrokerAlive()) {
						
						log.debug("Broker is alive");
						
					} else {
						
						restartBroker();
						
					}
					
					// Wait before checking again
					try { Thread.sleep(5000L); } catch(InterruptedException ie) { }
				
				} catch(RuntimeException re) {
					
					log.error(re);
					
				}
			}
			
			// Stop the service
			broker.stop();
			
			log.info("Monitor stopped");
			
		}
	}
}
