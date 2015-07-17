package it.essar.hop.broker;

import it.essar.hop.broker.api.BrokerInfoType;

import javax.jms.ConnectionFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Manages a singleton Broker used within the HOP server process.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
public class HOPBrokerManager
{

	private static final Logger log = LogManager.getLogger(HOPBrokerManager.class);
	private static final BrokerMonitor monitor = new BrokerMonitor();
	private static HOPBrokerService broker;

	/**
	 * Get the singleton Broker
	 * @return the broker.
	 */
	static HOPBrokerService getBroker () {
		
		return broker;
		
	}
	
	/**
	 * Gets the current broker object.
	 * @return information about the broker.
	 */
	public static BrokerInfoType getBrokerInfo() {
		
		if(broker != null) {

			return broker.getBrokerInfo();
			
		}
		
		return null;
		
	}
	
	/**
	 * Gets an in-JVM JMS Connection Factory for the Broker.
	 * @return a ConnectionFactory refering to the broker.
	 */
	public static ConnectionFactory getLocalConnectionFactory() {
		
		if(broker != null) {
			
			return broker.getLocalConnectionFactory();
			
		} else {
			
			return HOPBrokerService.getDefaultLocalConnectionFactory();
			
		}
	}
	
	/**
	 * Checks if the broker is started.
	 * @return true if the broker has been started, false otherwise or if it has not been initalised.
	 */
	public static boolean isBrokerStarted() {
		
		if(broker != null) {
			
			return broker.isStarted();
		
		}
		
		return false;
	}
	
	/**
	 * Starts the default broker. Blocks until startup is complete.
	 */
	public static void startBroker() { 
		
		if(broker == null) {
			
			broker = new HOPBrokerService();
			
		}
		
		monitor.startMonitor();
		broker.waitUntilStarted();
		log.info("Broker service started");

	}

	/**
	 * Stops the default broker. Blocks until the shutdown is complete.
	 */
	public static void stopBroker() {
		
		if(broker != null) {
			
			monitor.stopMonitor();
			broker.waitUntilStopped();
			log.info("Broker service stopped");
			
		}
		
		// Release resources
		broker = null;
		
	}
	
	
	/**
	 * Internal class for monitoring and restarting the broker.
	 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
	 * @version 1.0
	 */
	private static class BrokerMonitor implements Runnable
	{
		
		private boolean running;
		private Thread monitorThread;
		
		/**
		 * Checks if the broker is alive.
		 * @return true if the broker is alive, false if it is <tt>null</tt> or not alive.
		 * @see HOPBrokerService#isAlive()
		 */
		private boolean isBrokerAlive() {
			
			if(broker == null) {
				
				return false;
				
			}
			
			return broker.isAlive();
			
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
			
					// If it doesn't exist, create it
					if(broker == null) {
						
						log.debug("Creating broker");
						broker = new HOPBrokerService();
						
					}
					
					// If it's not been started, start it
					if(! broker.isInit()) {
					
						log.debug("Starting broker");
						broker.start();
						broker.waitUntilStarted();
						
					}
					
					// If we can't connect to it, stop it
					if(! isBrokerAlive()) {
						
						// Reinitialise the broker
						if(broker != null) {
							
							log.debug("Stopping broker");
							broker.stop();
							broker.waitUntilStopped();
							
						}
						
						broker = null;
						
					} else {
						
						log.debug("Broker is alive");
					
						// Wait before checking again
						try { Thread.sleep(4500L); } catch(InterruptedException ie) { }
					
					}
					
					
				} catch(RuntimeException re) {
					
					log.error(re);
					
				}
				
				// Wait before checking again
				try { Thread.sleep(500L); } catch(InterruptedException ie) { }
			
			}
			
			// Stop the service
			if(broker != null && broker.isStarted()) {
				
				broker.stop();
				
			}
			
			log.info("Monitor stopped");
			
		}
	}
}
