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
 * Test case for {@see HOPBroker}.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 */
public class HOPBrokerTest
{
	private HOPBroker broker;
	
	@After
	public void cleanup() {

		HOPBroker.stopBroker();
		
	}

	@Test
	public void testBrokerStartStopOK() {
		
		HOPBroker.startBroker();
		assertTrue("Broker started", HOPBroker.isBrokerStarted());
		
		HOPBroker.stopBroker();
		assertFalse("Broker not started", HOPBroker.isBrokerStarted());
		
	}
	
	@Test
	public void testBrokerStartVmConnectOK() throws JMSException {
		
		HOPBroker.startBroker();
		assertTrue("Broker started", HOPBroker.isBrokerStarted());
		broker = HOPBroker.getBroker();
		
		Connection cxn = null;
		try {
			
			cxn = broker.getLocalConnectionFactory().createConnection();
			assertNotNull("Connection established locally", cxn);
			
		} finally {
			
			if(cxn != null) {
			
				cxn.close();
				
			}
		}
	}
	
	@Test
	public void testBrokerStartTcpConnectOK() throws JMSException {
		
		HOPBroker.startBroker();
		assertTrue("Broker started", HOPBroker.isBrokerStarted());
		broker = HOPBroker.getBroker();
		
		Connection cxn = null;
		try {
			
			ActiveMQConnectionFactory mqf = new ActiveMQConnectionFactory(HOPBroker.DEF_TCP_ADDRESS);
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
		
		HOPBroker.startBroker();
		assertTrue("Broker started", HOPBroker.isBrokerStarted());
		broker = HOPBroker.getBroker();
		
		try { Thread.sleep(500); } catch(InterruptedException ie) {}
		broker.abort();
		try { Thread.sleep(5500); } catch(InterruptedException ie) {}
		assertTrue("Broker started", HOPBroker.isBrokerStarted());
		
	}
}
