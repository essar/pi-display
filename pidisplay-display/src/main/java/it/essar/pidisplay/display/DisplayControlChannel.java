package it.essar.pidisplay.display;

import it.essar.pidisplay.common.appapi.ControlChannel;
import it.essar.pidisplay.common.appapi.ControlChannelMessage;
import it.essar.pidisplay.common.net.ConnectionState;
import it.essar.pidisplay.common.net.KeepAliveConnection;
import it.essar.pidisplay.common.net.ReadOnlyChannel;

import java.net.URI;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class DisplayControlChannel extends KeepAliveConnection implements ControlChannel
{
	private static final Logger log = LogManager.getLogger(DisplayControlChannel.class);
	
	private ReadOnlyChannel ctl;
	
	public DisplayControlChannel(URI brokerURI, String clientID, String serverID) {

		super(brokerURI, clientID, serverID);
			
	}

	private synchronized boolean isReady() {
		
		return (ctl != null && ctl.isAlive() && getConnectionState() == ConnectionState.CONNECTED);
		
	}
	
	private synchronized boolean waitForChannel(long timeout) {
		
		long waitTime = 5000L;
		long accumulatedWait = 0L;
		
		while(!isReady() && accumulatedWait < timeout) {
				
			log.debug("Waiting for control channel to be established");
			long startTime = System.currentTimeMillis();
			try {

				wait(waitTime);
				
			} catch(InterruptedException ie) { }
			
			accumulatedWait += System.currentTimeMillis() - startTime;
			
		}
		
		return isReady();

	}
	
	@Override
	protected void cxnDown() {
		
	}
	
	@Override
	protected void cxnUp() {

		try {

			sendKA(0L);
			
		} catch(JMSException jmse) {
			
			log.warn("JMSException sending KA message", jmse);
		}
	}
	
	@Override
	protected void destroyChannels() {
		
		if(ctl != null) {
			
			// Destroy control channel
			ctl.close();
			log.debug("Control channel closed");
			
		}
	}
	
	@Override
	protected void initChannels() throws JMSException {

		// Create control channel
		String qName = getClientID() + ".DISP";
		
		ctl = new ReadOnlyChannel(getConnection(), qName);
		log.debug("Created read-only channel on {}", qName);
		
	}
	
	@Override
	protected boolean cxnTimeout() {

		// Should try and re-establish connection to broker
		// Reset connection
		return reset();
		
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
	public void close() {

		disconnect();
		super.close();
		
	}
	
	@Override
	public ControlChannelMessage readMessage() {
		
		if(! waitForChannel(DisplayProperties.getControlChannelMessageTimeout())) {
			
			throw new ControlChannelException("Timeout waiting for channel to initialise");
			
		}
	
		try {
			
			Message msg = ctl.readMessage();
			if(msg == null) {
				
				log.warn("Read null message from control channel");
				return null;

			}
			log.info("Read message from control channel, messageID={}", msg.getJMSMessageID());
			return new JMSControlChannelMessage(msg);
			
		} catch(JMSException jmse) {
			
			// Wrap and raise
			throw new ControlChannelException("Unable to read message from control channel", jmse);
			
		}
	}
	
	static class JMSControlChannelMessage implements ControlChannelMessage
	{
		private String msgAppID, msgBody, msgType;
		
		public JMSControlChannelMessage(Message msg) throws JMSException {
			
			TextMessage txt = (TextMessage) msg;
			msgAppID = txt.getStringProperty("application-id");
			msgBody = txt.getText();
			msgType = txt.getStringProperty("msgType");
			
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
	}
}
