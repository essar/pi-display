package it.essar.pidisplay.display.jms;

import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.jms.ReadOnlyChannel;
import it.essar.pidisplay.common.net.jms.ReadWriteChannel;
import it.essar.pidisplay.display.ControlChannel;
import it.essar.pidisplay.display.ControlChannelException;
import it.essar.pidisplay.display.ControlChannelMessage;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JMSControlChannel extends KeepAliveConnection implements ControlChannel
{
	private static final Logger log = LogManager.getLogger(JMSControlChannel.class);
	
	private static final String propertyCreatedTime = "createdTime";
	private static final String propertyInReplyTo = "inReplyTo";
	private static final String propertySenderID = "senderID";
	
	private ActiveMQConnectionFactory cxFac;
	private Connection cxn;

	private ReadOnlyChannel ctl;
	private ReadWriteChannel ka;
	
	public JMSControlChannel(URI brokerURI, String clientID, String serverID) {

		super(clientID, serverID);
		
		// Initialise connection factory
		cxFac = new ActiveMQConnectionFactory(brokerURI);
			
		init();

	}
	
	@Override
	protected boolean connect() {
		
		int connectAttempts = 0;
		int maxConnectAttempts = 5;
		
		cxnState = ConnectionState.CONNECTING;
		
		while(cxnState == ConnectionState.CONNECTING) {
		
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
					ka = new ReadWriteChannel(cxn, qName, new PingerMessageListener(), selector);
					log.info("Created read-write channel on {} using {}", qName, selector);
					
				}
				
				{
					
					// Create control channel
					String qName = getClientID() + ".DISP";
					ctl = new ReadOnlyChannel(cxn, qName);
					log.info("Created read-only channel on {}", qName);
					
				}
				
				cxn.start();
				cxnState = ConnectionState.CONNECTED;
					
				log.info("Connected to {}", cxFac.getBrokerURL());
				
				// Call back method indicating connection is up
				cxnUp();
				
			} catch(JMSException jmse) {
				
				log.info("Unable to connect, retrying...");
				log.debug(jmse.getClass().getName(), jmse);
				
				// Try again unless we've already reached maximum
				
				if(connectAttempts >= maxConnectAttempts) {
					
					cxnState = ConnectionState.DISCONNECTED;
					
					log.warn("Could not connect to host: {}", jmse.getMessage());
					return false;
					
				}
				
				try {
					
					// Wait a beat before trying again
					Thread.sleep(3000L);
					
				} catch(InterruptedException ie) {}
			}
		}
		
		return true;
		
	}
	
	@Override
	protected void disconnect() {
		
		cxnState = ConnectionState.DISCONNECTED;
		
		if(cxn != null) {
			
			try {
				
				cxn.stop();
				log.debug("Connection stopped");
				
			} catch(JMSException jmse) {
				
				log.warn("Caught exception stopping Connection: {}", jmse.getMessage());
				log.debug(jmse.getClass().getName(), jmse);
				
			}
			
		}
		
		// Close control channel
		if(ctl != null) {

			ctl.close();
			log.info("Control channel closed");
		
		}
		
		// Close keep-alive channel
		if(ka != null) {
			
			ka.close();
			log.info("Keep-alive channel closed");
			
		}
		
		if(cxn != null) {
			
			try {
				
				cxn.close();
				log.debug("Connection closed");
				
			} catch(JMSException jmse) {
				
				log.warn("Caught exception closing Connection: {}", jmse.getMessage());
				log.debug(jmse.getClass().getName(), jmse);
				
			}
			
			cxn = null;

		}
	}
	
	@Override
	protected boolean cxnDown() {

		log.info("Client connection to server is DOWN");

		// Should try and re-establish connection to broker
		// Reset connection
		return reset();
		
	}
	
	@Override
	protected boolean cxnUp() {

		log.info("Client connection to server is UP");
		
		sendPing(0L);
		return true;

	}
	
	@Override
	protected String getLocalID() {

		// Listen to messages from server
		return getClientID();
		
	}
	
	@Override
	protected String getRemoteID() {

		// Listen to messages from server
		return getServerID();
		
	}
	
	@Override
	protected void sendPing(long inReplyTo) {

		if(cxn == null) {
			
			log.debug("sendPing(): Connection is null");
			throw new IllegalStateException("No connection to broker");
			
		}
		
		if(ka == null) {
			
			log.debug("sendPing(): ka is null");
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
			
			log.warn("Caught {} sending message: {}", jmse.getClass().getName(), jmse.getMessage());
			log.debug(jmse.getClass().getCanonicalName(), jmse);
			
		}
	}
	
	@Override
	public void close() {

		disconnect();
		super.close();
		
	}
	
	//@Override
	public ControlChannelMessage readMessage() {
	
		try {
			
			return new JMSControlChannelMessage(ctl.readMessage());
			
		} catch(JMSException jmse) {
			
			// Wrap and raise
			throw new ControlChannelException("Unable to read message from control channel", jmse);
			
		}
	}
	
	private class PingerMessageListener implements MessageListener
	{
		
		//@Override
		public void onMessage(Message msg) {
			
			try {

				// Read message properties
				long createdTime = msg.getLongProperty(propertyCreatedTime);
				String senderID = msg.getStringProperty(propertySenderID);
				processPing(senderID, createdTime);
									
			} catch(JMSException jmse) {
				
				log.warn("Caught {} processing JMS message: {}", jmse.getClass().getName(), jmse.getMessage());
				log.debug(jmse.getClass().getCanonicalName(), jmse);
				
			}
		}
	}
}
