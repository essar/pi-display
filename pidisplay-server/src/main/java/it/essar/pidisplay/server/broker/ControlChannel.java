package it.essar.pidisplay.server.broker;

import it.essar.pidisplay.common.net.ConnectionState;
import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.jms.ReadWriteChannel;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ControlChannel extends KeepAliveConnection
{
	private static final Logger log = LogManager.getLogger(ControlChannel.class);
	
	private static final String propertyCreatedTime = "createdTime";
	private static final String propertyInReplyTo = "inReplyTo";
	private static final String propertySenderID = "senderID";
	
	private ActiveMQConnectionFactory cxFac;
	private Connection cxn;
	
	private int maxConnectAttempts = 5;
	private long connectRetryDelay = 3000L;

	//private ReadOnlyChannel ctl;
	private ReadWriteChannel ka;
	
	public ControlChannel(URI brokerURI, String clientID, String serverID) {

		super(clientID, serverID);
		
		// Initialise connection factory
		cxFac = new ActiveMQConnectionFactory(brokerURI);
			
	}
	
	@Override
	protected boolean connect() {
		
		int connectAttempts = 0;
		
		setConnectionState(ConnectionState.CONNECTING);
		
		while(getConnectionState() == ConnectionState.CONNECTING) {
		
			connectAttempts ++;
			
			try {
				
				log.info("Connecting to {}, attempt {}/{}", cxFac.getBrokerURL(), connectAttempts, maxConnectAttempts);
				
				if(cxn == null) {
					
					cxn = cxFac.createConnection();
					
				}
				
				{
					
					// Create keep-alive channel
					String qName = getClientID() + ".KA";
					String selector = propertySenderID + " = '" + getRemoteID() + "'";
					ka = new ReadWriteChannel(cxn, qName, new KeepAliveMessageListener(), selector);
					log.debug("Created read-write channel on {} using {}", qName, selector);
					
				}
				
				{
					
					// Create control channel
					String qName = getClientID() + ".DISP";
				//	ctl = new ReadOnlyChannel(cxn, qName);
				//	log.debug("Created read-only channel on {}", qName);
					
				}
				
				// Start the JMS connection
				cxn.start();
				
				setConnectionState(ConnectionState.CONNECTED);
				log.info("Client connection to client is UP");
				
				//sendKA(0L);
				
			} catch(JMSException jmse) {
				
				log.info("Unable to connect, retrying...");
				log.debug("JMSException establishing connection", jmse);
				
				// Try again unless we've already reached maximum
				
				if(maxConnectAttempts > 0 && connectAttempts >= maxConnectAttempts) {
					
					log.error("Could not connect to host: {}", jmse.getMessage());
					return false;
					
				}
				
				try {
					
					// Wait a beat before trying again
					Thread.sleep(connectRetryDelay);
					
				} catch(InterruptedException ie) {}
			}
		}
		
		return true;
		
	}
	
	@Override
	protected void disconnect() {
		
		setConnectionState(ConnectionState.DISCONNECTED);
		log.info("Client connection to client is DOWN");
		
		if(cxn != null) {
			
			try {
				
				cxn.stop();
				log.debug("Connection stopped");
				
			} catch(JMSException jmse) {
				
				log.debug("Caught JMSException stopping Connection", jmse);
				
			}
		}
		
		// Close control channel
		/*if(ctl != null) {

			ctl.close();
			log.debug("Control channel closed");
		
		}*/
		
		// Close keep-alive channel
		if(ka != null) {
			
			ka.close();
			log.debug("Keep-alive channel closed");
			
		}
		
		if(cxn != null) {
			
			try {
				
				cxn.close();
				log.debug("Connection closed");
				
			} catch(JMSException jmse) {
				
				log.debug("Caught JMSException closing Connection", jmse);
				
			}
			
			cxn = null;

		}
	}
	
	@Override
	protected boolean cxnTimeout() {

		// Should try and re-establish connection to broker
		// Reset connection
		//return reset();
		return false;
	}

	@Override
	protected String getLocalID() {

		// Listen to messages from server
		return getServerID();
		
	}
	
	@Override
	protected String getRemoteID() {

		// Listen to messages from server
		return getClientID();
		
	}
	
	@Override
	protected void sendKA(long inReplyTo) {

		if(cxn == null) {
			
			log.debug("sendKA(): Connection is null");
			throw new IllegalStateException("No connection to broker");
			
		}
		
		if(ka == null) {
			
			log.debug("sendKA(): ka is null");
			throw new IllegalStateException("No keep-alive channel");
			
		}
		
		try {
		
			// Build the message
			Message reply = ka.createMessage();
			reply.setStringProperty(propertySenderID, getLocalID());
			reply.setLongProperty(propertyCreatedTime, System.currentTimeMillis());
			reply.setLongProperty(propertyInReplyTo, inReplyTo);
				
			// Send the message
			ka.sendMessage(reply);
			log.info("Sent message {}", reply.getJMSMessageID());
		
		} catch(JMSException jmse) {
			
			// Log wrap & raise
			log.debug("JMSException whilst sending KA message", jmse);
			//throw new ControlChannelException("Unable to send KA message", jmse);
			
		}
	}
	
	private class KeepAliveMessageListener implements MessageListener
	{
		
		//@Override
		public void onMessage(Message msg) {
			
			try {

				// Read message properties
				long createdTime = msg.getLongProperty(propertyCreatedTime);
				String senderID = msg.getStringProperty(propertySenderID);
				processKeepAlive(senderID, createdTime);
									
			} catch(JMSException jmse) {
				
				log.warn("JMSException processing KA message", jmse);
				
			} catch(RuntimeException re) {
				
				log.warn("Exception in KeepAliveMessageListener", re);
				
			}
		}
	}
}
