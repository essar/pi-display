package it.essar.hop.broker;

import it.essar.hop.broker.api.BrokerInfoType;
import it.essar.hop.broker.api.BrokerRuntimeType;
import it.essar.hop.broker.api.ConnectionSet;
import it.essar.hop.broker.api.ConnectionStatisticsType;
import it.essar.hop.broker.api.ConnectionType;
import it.essar.hop.broker.api.DestinationSet;
import it.essar.hop.broker.api.DestinationType;
import it.essar.hop.broker.api.ObjectFactory;
import it.essar.hop.broker.api.ObjectNameSet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;

import javax.jms.JMSException;
import javax.management.ObjectName;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.Connection;
import org.apache.activemq.broker.jmx.BrokerView;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides the embedded JMS Broker used within the HOP server process.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
class HOPBrokerService
{

	private static final Logger log = LogManager.getLogger(HOPBrokerService.class);
	private static final Properties brokerProps = new Properties();
	private static final String PROPERTY_FILE = "/broker.properties";
	
	static String DEF_BROKER_NAME = "hop-broker";
	static String DEF_TCP_ADDRESS = "tcp://localhost:61616";

	private final BrokerService svc;
	private boolean init;
	
	/**
	 * Load the properties file.
	 */
	static {
		
		loadProperties();
		
	}
	
	/**
	 * Loads properties from the file specified by {@see PROPERTY_FILE}.
	 */
	private static void loadProperties() {
		
		try {
		
			brokerProps.load(HOPBrokerService.class.getResourceAsStream(PROPERTY_FILE));

		} catch(IOException ioe) {
			
			log.error("Unable to load properties from {}: {}", PROPERTY_FILE, ioe.getMessage());
			
		}
	}

	/**
	 * Instantiates a new HOPBroker instance.
	 */
	HOPBrokerService() {
		
		svc = new BrokerService();
		svc.setBrokerName(getPropertyBrokerName());
		svc.setPersistent(false);
			
	}
	
	/**
	 * Reads the <tt>Broker Name</tt> property.
	 * @return <tt>hop.broker.brokerName</tt> or {@see #DEF_BROKER_NAME}.
	 */
	private static String getPropertyBrokerName() {
		
		return brokerProps.getProperty("hop.broker.brokerName", DEF_BROKER_NAME);
		
	}
	
	/**
	 * Reads the <tt>TCP bind address</tt> property.
	 * @return <tt>hop.broker.tcpAddress</tt> or {@see #DEF_TCP_ADDRESS}.
	 */
	private static String getPropertyTcpAddress() {
		
		return brokerProps.getProperty("hop.broker.tcpAddress", DEF_TCP_ADDRESS);
		
	}
	
	/**
	 * Gets a default in-VM connection factory.
	 * @return
	 */
	static ActiveMQConnectionFactory getDefaultLocalConnectionFactory() {
		
		return new ActiveMQConnectionFactory("vm://" + getPropertyBrokerName());
		
	}
	
	/**
	 * Gets a default TCP connection factory.
	 * @return
	 */
	static ActiveMQConnectionFactory getDefaultTCPConnectionFactory() {
		
		return new ActiveMQConnectionFactory(getPropertyTcpAddress());
	
	}
	
	/**
	 * Initialise the BrokerService.
	 * @return if the broker is successfully created and initialised.
	 */
	private boolean initBroker() {
		
		try {

			log.info("Initiating broker");
			svc.addConnector(getPropertyTcpAddress());
			
		} catch(Exception e) {
			
			log.fatal("Unable to bind broker to TCP address: {}", e.getMessage());
			return false;
			
		}
		
		init = true;
		return true;
		
	}
	
	/**
	 * Gets a JMS ConnectionFactory for connecting to this broker.
	 * @return
	 */
	ActiveMQConnectionFactory getLocalConnectionFactory() {
		
		return new ActiveMQConnectionFactory(svc.getVmConnectorURI());
		
	}
	
	/**
	 * Gets the name of the broker.
	 * @return the name of this broker.
	 */
	String getName() {
		
		return svc.getBrokerName();
		
	}

	/**
	 * Checks if the current broker is alive. Will attempt to make a connection to the internal URI of the broker.
	 * @return true if the broker is alive, false otherwise.
	 */
	boolean isAlive() {
	
		if(svc.isStarted()) {
		
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
	 * Checks if this broker has been initialised.
	 * @return true if the broker has been initialised, false otherwise.
	 */
	boolean isInit() {
		
		return init;
		
	}
	
	/**
	 * Starts the broker
	 */
	synchronized void start() {
		
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
	synchronized void stop() {
		
		try {
			
			log.info("Stopping broker {}", getName());
			svc.stop();
			log.info("Broker {} {}", getName(), svc.isStopped() ? "stopped" : "not stopped");
				
		} catch(Exception e) {
			
			log.error("Unable to stop broker", e);
			
		}
	}
	
	/**
	 * Returns information about the broker.
	 * @return current broker runtime and administration information.
	 */
	public BrokerInfoType getBrokerInfo() {
		
		ObjectFactory of = new ObjectFactory();
		
		BrokerInfoType bit = of.createBrokerInfoType();

		try {
			
			BrokerView bv = svc.getAdminView();
			
			
			bit.setBrokerID(bv.getBrokerId());
			bit.setBrokerName(bv.getBrokerName());
			bit.setBrokerVersion(bv.getBrokerVersion());
			
			// Runtime info
			{
			
				BrokerRuntimeType brt = of.createBrokerRuntimeType();
				brt.setMemoryLimit(bv.getMemoryLimit());
				brt.setMemoryPercentUsage(bv.getMemoryPercentUsage());
				brt.setStoreLimit(bv.getStoreLimit());
				brt.setMemoryPercentUsage(bv.getStorePercentUsage());
				brt.setTempLimit(bv.getTempLimit());
				brt.setTempPercentUsage(bv.getTempPercentUsage());
				brt.setUptimeMillis(bv.getUptimeMillis());
				bit.setRuntime(brt);
			
			}
			
			// Queue names
			{
				
				ObjectNameSet ons = of.createObjectNameSet();
				for(ObjectName n : bv.getQueues()) {
					
					ons.getCanonicalName().add(n.getCanonicalName());
				
				}
				bit.setQueues(ons);
		
			}
			
			// Temporary Queue names
			{
				
				ObjectNameSet ons = of.createObjectNameSet();
				for(ObjectName n : bv.getTemporaryQueues()) {
					
					ons.getCanonicalName().add(n.getCanonicalName());
				
				}
				bit.setTemporaryQueues(ons);
		
			}
			
			// Temporary Topic names
			{
				
				ObjectNameSet ons = of.createObjectNameSet();
				for(ObjectName n : bv.getTemporaryTopics()) {
					
					ons.getCanonicalName().add(n.getCanonicalName());
				
				}
				bit.setTemporaryTopics(ons);
		
			}

			// Topic names
			{
				
				ObjectNameSet ons = of.createObjectNameSet();
				for(ObjectName n : bv.getTopics()) {
					
					ons.getCanonicalName().add(n.getCanonicalName());
				
				}
				bit.setTopics(ons);
		
			}
			
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
	 * {@see BrokerService#isStarted()}
	 * @return a flag indicating if the broker is started or not.
	 */
	public boolean isStarted() {
		
		return svc.isStarted();
		
	}
	
	/**
	 * {@see BrokerService#isStopped()}
	 * @return a flag indicating if the broker is stopped or not.
	 */
	public boolean isStopped() {
		
		return svc.isStopped();
		
	}
	
	/**
	 * {@see BrokerService#isStopping()}
	 * @return a flag indicating if the broker is stopping or not.
	 */
	public boolean isStopping() {
		
		return svc.isStopping();
		
	}
	
	/**
	 * {@see BrokerService#waitUntilStarted()}
	 * @return a flag indicating if the broker is started or not.
	 */
	public boolean waitUntilStarted() {
		
		return svc.waitUntilStarted();
		
	}
	
	/**
	 * {@see BrokerService#waitUntilStopped()}
	 */
	public void waitUntilStopped() {
		
		svc.waitUntilStopped();
		
	}
}
