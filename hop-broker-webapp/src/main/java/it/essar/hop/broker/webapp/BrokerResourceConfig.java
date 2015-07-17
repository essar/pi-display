package it.essar.hop.broker.webapp;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class BrokerResourceConfig extends ResourceConfig
{

	public BrokerResourceConfig() {
		
		packages("it.essar.hop.broker.webapp.resources");
		
	}
}
