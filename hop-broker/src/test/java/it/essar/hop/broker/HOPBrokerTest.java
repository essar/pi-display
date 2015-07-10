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
	
	@After
	public void cleanup() {

		HOPBroker.stopBroker();
		
	}

	@Test
	public void testBrokerStartStopOK() {
		
		HOPBroker.startBroker();
		assertTrue("Broker started", HOPBroker.isStarted());
		
		HOPBroker.stopBroker();
		assertFalse("Broker not started", HOPBroker.isStarted());
		
	}
	
	@Test
	public void testBrokerStartVmConnectOK() throws JMSException {
		
		HOPBroker.startBroker();
		assertTrue("Broker started", HOPBroker.isStarted());
		
		Connection cxn = null;
		try {
			
			cxn = HOPBroker.getLocalConnectionFactory().createConnection();
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
		assertTrue("Broker started", HOPBroker.isStarted());
		
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
	
	@Test(expected=IllegalStateException.class)
	public void testBrokerConnectNotStartedFail() throws JMSException {
		
		HOPBroker.getLocalConnectionFactory().createConnection();
		
	}
}
