package it.essar.pidisplay.server.http;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class APIService
{

	@GET @Path("/clientID")
	@Produces(MediaType.APPLICATION_JSON)
	public String getClientID() {
		
		return null;
		
	}
}
