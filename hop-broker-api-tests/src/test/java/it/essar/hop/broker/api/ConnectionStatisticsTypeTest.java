package it.essar.hop.broker.api;

import java.math.BigInteger;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test case for {@see ConnectionStatisticsType}.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
public class ConnectionStatisticsTypeTest
{
	
	private ConnectionStatisticsType underTest;
	private ObjectFactory of;
	
	@Before
	public void setUp() {
		
		of = new ObjectFactory();
		underTest = of.createConnectionStatisticsType();

	}

	@Test
	public void testDequeues() {
		
		BigInteger tv = BigInteger.valueOf(UUID.randomUUID().getLeastSignificantBits());
		underTest.setDequeues(tv);
		assertEquals("ConnectionStatisticsType.dequeues", tv, underTest.getDequeues());
		
	}
	
	@Test
	public void testEnqueues() {

		BigInteger tv = BigInteger.valueOf(UUID.randomUUID().getLeastSignificantBits());
		underTest.setEnqueues(tv);
		assertEquals("ConnectionStatisticsType.enqueues", tv, underTest.getEnqueues());

	}
}
