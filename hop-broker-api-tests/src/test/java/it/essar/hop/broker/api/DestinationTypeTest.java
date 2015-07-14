package it.essar.hop.broker.api;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@see DestinationType}.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
public class DestinationTypeTest
{
	
	private DestinationType underTest;
	private ObjectFactory of;
	
	@Before
	public void setUp() {
		
		of = new ObjectFactory();
		underTest = of.createDestinationType();
		
	}

	@Test
	public void testPhysicalName() {
		
		String tv = UUID.randomUUID().toString();
		underTest.setPhysicalName(tv);
		assertEquals("ConnectionType.physicalName", tv, underTest.getPhysicalName());
		
	}
	
	@Test
	public void testQualifiedName() {
		
		String tv = UUID.randomUUID().toString();
		underTest.setQualifiedName(tv);
		assertEquals("ConnectionType.qualifiedName", tv, underTest.getQualifiedName());
		
	}
	
	@Test
	public void testQueue() {
		
		Boolean tv = true;
		underTest.setQueue(tv);
		assertEquals("ConnectionType.queue", tv, underTest.isQueue());
		
	}
	
	@Test
	public void testTemporary() {
		
		Boolean tv = true;
		underTest.setTemporary(tv);
		assertEquals("ConnectionType.temporary", tv, underTest.isTemporary());
		
	}
	
	@Test
	public void testTopic() {
		
		Boolean tv = true;
		underTest.setTopic(tv);
		assertEquals("ConnectionType.topic", tv, underTest.isTopic());
		
	}
}
