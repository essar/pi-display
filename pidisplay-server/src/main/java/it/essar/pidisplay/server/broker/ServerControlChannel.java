package it.essar.pidisplay.server.broker;

import it.essar.pidisplay.common.net.ConnectionState;
import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.ReadWriteChannel;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerControlChannel extends KeepAliveConnection
{
	private static final Logger log = LogManager.getLogger(ServerControlChannel.class);
	
	private ReadWriteChannel ka;
	
	public ServerControlChannel(URI brokerURI, String clientID, String serverID) {

		super(brokerURI, clientID, serverID);
		
	}
	
	@Override
	protected void cxnDown() {
		
		log.info("Client connection to client is DOWN");
		
	}

	@Override
	protected boolean cxnTimeout() {

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
		/*if(ctl != null) {
	
			ctl.close();
			log.debug("Control channel closed");
		
		}*/
	}
	
	@Override
	protected void initChannels() {

		// Create control channel
		String qName = getClientID() + ".DISP";
	//	ctl = new ReadOnlyChannel(cxn, qName);
	//	log.debug("Created read-only channel on {}", qName);
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
