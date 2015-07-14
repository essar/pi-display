package it.essar.hop.broker.api;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@see ObjectFactory}.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
public class ObjectFactoryTest
{
	
	private ObjectFactory underTest;
	
	@Before
	public void setUp() {
		
		underTest = new ObjectFactory();

	}

	@Test
	public void testCreateBrokerInfoType() {
		
		BrokerInfoType bit = underTest.createBrokerInfoType();
		assertNotNull("BrokerInfoType not null", bit);

	}

	@Test
	public void testCreateConnectionSet() {
		
		ConnectionSet cs = underTest.createConnectionSet();
		assertNotNull("ConnectionSet not null", cs);
		
	}
	
	@Test
	public void testCreateConnectionType() {
		
		ConnectionType ct = underTest.createConnectionType();
		assertNotNull("ConnectionType not null", ct);
		
	}
	
	@Test
	public void testCreateConnectionStatisticsType() {
		
		ConnectionStatisticsType cs = underTest.createConnectionStatisticsType();
		assertNotNull("ConnectionStatisticsType not null", cs);
		
	}
	
	@Test
	public void testCreateDestinationSet() {
		
		DestinationSet ds = underTest.createDestinationSet();
		assertNotNull("DestinationSet not null", ds);
		
	}
	
	@Test
	public void testCreateDestinationType() {
		
		DestinationType dt = underTest.createDestinationType();
		assertNotNull("DestinationType not null", dt);
		
	}
}
