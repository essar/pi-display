package it.essar.hop.broker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.After;
import org.junit.Test;

/**
 * Test case for {@see HOPBrokerManager}.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 */
public class HOPBrokerManagerTest
{
	@After
	public void cleanup() {

		HOPBrokerManager.stopBroker();
		
	}

	@Test
	public void testBrokerStartStopOK() {
		
		HOPBrokerManager.startBroker();
		assertTrue("Broker started", HOPBrokerManager.isBrokerStarted());
		
		HOPBrokerManager.stopBroker();
		assertFalse("Broker not started", HOPBrokerManager.isBrokerStarted());
		
	}
	
	@Test
	public void testBrokerStartVmConnectOK() throws JMSException {
		
		HOPBrokerManager.startBroker();
		assertTrue("Broker started", HOPBrokerManager.isBrokerStarted());
		
		Connection cxn = null;
		try {
			
			cxn = HOPBrokerManager.getLocalConnectionFactory().createConnection();
			assertNotNull("Connection established locally", cxn);
			
		} finally {
			
			if(cxn != null) {
			
				cxn.close();
				
			}
		}
	}
	
	@Test
	public void testBrokerStartTcpConnectOK() throws JMSException {
		
		HOPBrokerManager.startBroker();
		assertTrue("Broker started", HOPBrokerManager.isBrokerStarted());
		
		Connection cxn = null;
		try {
			
			ActiveMQConnectionFactory mqf = HOPBrokerService.getDefaultTCPConnectionFactory();
			cxn = mqf.createConnection();
			assertNotNull("Connection established over TCP", cxn);
			
		} finally {
			
			if(cxn != null) {
			
				cxn.close();
				
			}
		}
	}
	
	@Test
	public void testBrokerAutoRestartOK() throws JMSException {
		
		HOPBrokerManager.startBroker();
		
		// Wait for start up proccess to complete
		try { Thread.sleep(500); } catch(InterruptedException ie) {}
		
		assertTrue("Broker started", HOPBrokerManager.isBrokerStarted());
		HOPBrokerManager.getBroker().stop();
		
		// Wait for more than 5 seconds so that monitor can run
		try { Thread.sleep(5500L); } catch(InterruptedException ie) {}
		
		// Check that broker has restarted
		assertTrue("Broker started", HOPBrokerManager.isBrokerStarted());
		
	}
}
