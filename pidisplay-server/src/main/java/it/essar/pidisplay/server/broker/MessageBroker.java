package it.essar.pidisplay.server.broker;

import java.net.URI;
import java.net.URISyntaxException;

import it.essar.pidisplay.common.proc.MonitorableProcess;
import it.essar.pidisplay.common.proc.ProcessMonitor;

import org.apache.activemq.broker.BrokerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageBroker implements MonitorableProcess
{
	private static final Logger log = LogManager.getLogger(MessageBroker.class);
	private static MessageBroker mb;
	
	
	public static final int DEFAULT_PORT = 61616;
	public static final String BROKER_NAME = "pidisplay";
	public static final String DEFAULT_HOST = "localhost";
	public static final String DEFAULT_SCHEME = "tcp";
	
	private final ProcessMonitor monitor;

	private BrokerService broker;
	private URI bindAddress;
	
	private MessageBroker(String bindHost, int bindPort) {

		try {

			bindAddress = new URI(DEFAULT_SCHEME, null, bindHost, bindPort, null, null, null);
		
		} catch(URISyntaxException use) {
			
			throw new IllegalArgumentException("Invalid bind address", use);
			
		}
		
		createAndConfigureService();
		
		// Create monitor
		monitor = new ProcessMonitor(this);
					
	}
	
	private void createAndConfigureService() {
		
		broker = new BrokerService();
		
		try {
			
			// Configure broker service
			log.debug("Configuring broker service");
			
			broker.addConnector(bindAddress);
			broker.setBrokerName(BROKER_NAME);
			broker.setPersistent(false);
			
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to configure broker service", e);
			
		}
	}
	
	public String getProcessName() {

		return "MessageBroker";
		
	}
	
	public boolean isProcessRunning() {
		
		if(broker == null) {
			
			log.debug("isBrokerRunning(): Broker is null");
			return false;
		
		}
		
		if(! broker.isStarted()) {
		
			log.debug("isBrokerRunning(): Broker is not started");
			return false;
		
		}
		
		return true;
		
	}
	
	public boolean startProcess() {
		
		try {
			
			if(! isProcessRunning()) {
				
				broker.start();
				
			}
			
			if(! isProcessRunning()) {
				
				// Service did not start correctly, recreate and try again
				createAndConfigureService();
				broker.start();
				
			}
			
			if(isProcessRunning()) {
				
				log.info("Broker {} started", broker.getBrokerName());
				return true;
				
			} else {
				
				log.warn("Broker failed to start");
				return false;
				
			}
			
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to start broker", e);
			
		}
	}
	
	public boolean stopProcess() {
		
		try {
			
			if(isProcessRunning()) {
				
				broker.stop();
				
			}
			
			if(! isProcessRunning()) {
				
				log.info("Broker {} stopped", broker.getBrokerName());
				return true;
				
			} else {
				
				log.warn("Broker failed to stop");
				return false;
				
			}
		
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to stop broker", e);
			
		}
	}
	
	public static void startBroker() {
		
		startBroker(DEFAULT_HOST, DEFAULT_PORT);
		
	}
	
	public static void startBroker(String bindHost, int bindPort) {
		
		if(mb == null) {
				
			mb = new MessageBroker(bindHost, bindPort);
			
		}
			
		// Start broker monitor - also starts broker
		mb.monitor.start();
		
	}
	
	public static void stopBroker() {
		
		if(mb != null) {
			
			// Stop the monitor - also stops broker
			mb.monitor.stop();
			
		}
		
		mb = null;
	}
	
	
	public static void main(String[] args) {
		
		// Start the broker
		startBroker();
		
		/*try {
			Thread.sleep(3000L);
		} catch(InterruptedException ie) {
			
		}*/
		
		try {
			ServerControlChannel svr = new ServerControlChannel(new URI("tcp://localhost:61616/" + MessageBroker.BROKER_NAME), "TestClient", "TestServer");
			svr.start();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		/*try {
			Thread.sleep(15000L);
		} catch(InterruptedException ie) {
			
		}*/
		
		// Abort the broker - monitor should restart it
		/*try {
			mb.broker.stop();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}*/
		
		try {
			Thread.sleep(3600000L);
		} catch(InterruptedException ie) {
			
		}
		
		// Stop the broker
		stopBroker();
		
	}
}
