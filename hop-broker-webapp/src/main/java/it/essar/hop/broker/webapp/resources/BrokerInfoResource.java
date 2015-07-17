package it.essar.hop.broker.webapp.resources;

import it.essar.hop.broker.HOPBrokerManager;
import it.essar.hop.broker.api.BrokerInfoType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * JAX-RS resource file for Broker Information
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
@Path("/info")
@Produces(MediaType.APPLICATION_JSON)
public class BrokerInfoResource
{

	/**
	 * Get the current broker information.
	 * @return current broker information.
	 */
	@GET 
	public BrokerInfoType getBrokerInfo() {
		
		BrokerInfoType bit = HOPBrokerManager.getBrokerInfo();
		return bit;
		
	}
}
