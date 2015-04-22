package it.essar.pidisplay.server.broker;

import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.WriteOnlyChannel;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerControlChannel extends KeepAliveConnection
{
	private static final Logger log = LogManager.getLogger(ServerControlChannel.class);
	
	private WriteOnlyChannel ctl;
	
	public ServerControlChannel(URI brokerURI, String clientID, String serverID) {

		super(brokerURI, clientID, serverID);
		
	}
	
	@Override
	protected void cxnDown() {
		
		log.info("Client connection to client is DOWN");
		
		// TODO Client clean-up
	}

	@Override
	protected boolean cxnTimeout() {

		// Try and determine if connection to broker has been lost
		// Should try and re-establish connection to broker
		// Reset connection
		//return reset();
		return false;
		
	}
	
	@Override
	protected void cxnUp() {
		
		log.info("Client connection to client is UP");

	}
	
	@Override
	protected void destroyChannels() {
		
		// Close control channel
		if(ctl != null) {
	
			ctl.close();
			log.debug("Control channel closed");
		
		}
	}
	
	@Override
	protected void initChannels() {

		// Create control channel
		String qName = getClientID() + ".DISP";
		ctl = new WriteOnlyChannel(getConnection(), qName);
		log.debug("Created write-only channel on {}", qName);
	
	}
	
	@Override
	protected String getLocalID() {

		// Listen to messages from server
		return getServerID();
		
	}
	
	@Override
	protected String getRemoteID() {

		// Listen to messages from client
		return getClientID();
		
	}
}
