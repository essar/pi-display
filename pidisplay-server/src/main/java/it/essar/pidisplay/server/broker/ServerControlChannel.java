package it.essar.pidisplay.server.broker;

import it.essar.pidisplay.common.appapi.ControlChannelMessage;
import it.essar.pidisplay.common.msgs.ResetMessage;
import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.WriteOnlyChannel;

import java.net.URI;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

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

		
		try {
			// Temporary send reset message
		
			ctl.sendMessage(new JMSControlChannelMessage().getMessage(ctl.createMessage(), new ResetMessage()));
			
		} catch(JMSException jmse) {
			
			log.warn(jmse.getMessage(), jmse);
			
		}
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
	
	static class JMSControlChannelMessage implements ControlChannelMessage
	{
		private String msgAppID, msgBody, msgType;
		
		public JMSControlChannelMessage() {
			
		}
		
		Message getMessage(Message msg, ControlChannelMessage ccm) throws JMSException {
			
			TextMessage txt = (TextMessage) msg;
			txt.setStringProperty("application-id", ccm.getMessageAppID());
			txt.setText(ccm.getMessageBody());
			txt.setStringProperty("msgType", ccm.getMessageType());
			return msg;
			
		}
		
		@Override
		public String getMessageAppID() {

			return msgAppID;
			
		}
		
		@Override
		public String getMessageBody() {

			return msgBody;
		
		}
		
		@Override
		public String getMessageType() {

			return msgType;
		
		}
		
		@Override
		public String toString() {
			
			return String.format("%s|%s", msgType, msgAppID);
			
		}
	}
}
