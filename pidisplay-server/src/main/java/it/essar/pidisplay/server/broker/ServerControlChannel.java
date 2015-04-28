package it.essar.pidisplay.server.broker;

import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.WriteOnlyChannel;

import java.net.URI;

import javax.jms.JMSException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerControlChannel extends KeepAliveConnection
{
	private static final Logger log = LogManager.getLogger(ServerControlChannel.class);
	
	private WriteOnlyChannel ctl;
	
	public static long clientTimeout = 30000L;
	
	public ServerControlChannel(URI brokerURI, String clientID, String serverID) {

		super(brokerURI, clientID, serverID);
		
	}
	
	void cleanup() {
		
		// TODO Clean up client resources
		log.debug("Cleaning up after {}", getClientID());
		
	}
	
	@Override
	protected void cxnDown() {
		
		// Client clean-up
		cleanup();
	
	}

	@Override
	protected boolean cxnTimeout() {

		// Try and determine if connection to broker has been lost
		if(ctl != null && ctl.isAlive()) {
			
			if(getMillisSinceLastResp() > clientTimeout) {
			
				// Waited too long, assume client has gone away
				cleanup();
				return false;
				
			}
			
		} else {
			
			// Should try and re-establish connection to broker
			// Reset connection
			return reset();
			
		}
		
		return true;
		
	}
	
	@Override
	protected void cxnUp() {
		
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
	protected void initChannels() throws JMSException {

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
