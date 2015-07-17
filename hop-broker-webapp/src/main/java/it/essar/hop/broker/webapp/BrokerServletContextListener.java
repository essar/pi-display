package it.essar.hop.broker.webapp;

import it.essar.hop.broker.HOPBrokerManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Context listener that starts and stops the Broker when the context is deployed.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
@WebListener
public class BrokerServletContextListener implements ServletContextListener
{
	
	private static final Logger log = LogManager.getLogger(BrokerServletContextListener.class);

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent evt) {

		// Stop the broker
		HOPBrokerManager.stopBroker();
		log.info("Stopped HOP Broker");
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent evt) {
		
		// Start up the broker
		HOPBrokerManager.startBroker();
		log.info("Started HOP Broker");
	
	}
}
