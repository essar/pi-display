package it.essar.hop.broker.api;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@see ConnectionType).
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
public class ConnectionTypeTest
{
	
	private ConnectionType underTest;
	private ObjectFactory of;
	
	@Before
	public void setUp() {
		
		of = new ObjectFactory();
		underTest = of.createConnectionType();
		
	}

	@Test
	public void testConnectionID() {
		
		String tv = UUID.randomUUID().toString();
		underTest.setConnectionID(tv);
		assertEquals("ConnectionType.connectionID", tv, underTest.getConnectionID());
		
	}
	
	@Test
	public void testNetworkConnection() {
		
		Boolean tv = true;
		underTest.setNetworkConnection(tv);
		assertEquals("ConnectionType.networkConnection", tv, underTest.isNetworkConnection());
		
	}
		
	@Test
	public void testRemoteAddress() {
		
		String tv = UUID.randomUUID().toString();
		underTest.setRemoteAddress(tv);
		assertEquals("ConnectionType.remoteAddress", tv, underTest.getRemoteAddress());
		
	}
}
