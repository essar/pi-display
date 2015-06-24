package it.essar.pidisplay.common.net;

import java.net.URI;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class KeepAliveConnection
{
	protected final Logger log = LogManager.getLogger(KeepAliveConnection.class);
	
	private static final String propertyCreatedTime = "createdTime";
	private static final String propertyInReplyTo = "inReplyTo";
	private static final String propertySenderID = "senderID";
	
	private final String clientID, serverID;
	private final Thread monitorThread;
	
	protected final KeepAliveResponse resp;
	
	private ActiveMQConnectionFactory cxFac;
	private boolean running = true;
	private Connection cxn;
	private ConnectionState cxnState;
	private ReadWriteChannel ka;
	
	protected int maxConnectAttempts = 5;
	protected long connectRetryDelay = 5000L;
	protected long monitorTimeout = 10000L;
	protected long replyDelay = 3000L;
	
	
	public final ConnectionStateProperty cxnStateProperty;
	
	protected KeepAliveConnection(URI brokerURI, String clientID, String serverID) {
		
		this.clientID = clientID;
		this.serverID = serverID;
		this.resp = new KeepAliveResponse();
		this.monitorThread = new Thread(new KeepAliveMonitor(), getLocalID() + "-Monitor");
		
		// Initialise connection factory
		cxFac = new ActiveMQConnectionFactory(brokerURI);
		
		cxnStateProperty = new ConnectionStateProperty();
		setConnectionState(ConnectionState.DISCONNECTED);
		
		log.debug("Initialised KeepAliveConnection from {} to {}", clientID, serverID);
		
	}
	
	protected abstract void cxnDown();
	
	protected abstract boolean cxnTimeout();
	
	protected abstract void cxnUp();
	
	protected abstract void destroyChannels();
	
	protected abstract void initChannels() throws JMSException;
	
	protected abstract String getRemoteID();
	
	protected abstract String getLocalID();


	protected boolean connect() {
		
		int connectAttempts = 0;
		
		setConnectionState(ConnectionState.CONNECTING);
		
		while(getConnectionState() == ConnectionState.CONNECTING) {
		
			connectAttempts ++;
			
			try {
				
				log.debug("Connecting to {}, attempt {}/{}", cxFac.getBrokerURL(), connectAttempts, maxConnectAttempts);
				
				if(cxn == null) {
				
					cxn = cxFac.createConnection();
					
				}
				log.debug("Connection established with {}", cxFac.getBrokerURL());
				
				{
					
					// Create keep-alive channel
					String qName = getClientID() + ".KA";
					String selector = propertySenderID + " = '" + getRemoteID() + "'";
					ka = new ReadWriteChannel(cxn, qName, new KeepAliveMessageListener(), selector);
					log.debug("Created read-write channel on {} using {}", qName, selector);
					
				}
				
				// Create additional channels
				initChannels();
				
				// Start the JMS connection
				cxn.start();
				
				setConnectionState(ConnectionState.CONNECTED);
				
				// Reset response
				resp.reset();
				
				// Call out that connection is up
				log.info("{} connection to {} is UP", getLocalID(), getRemoteID());
				cxnUp();
				
			} catch(JMSException jmse) {
				
				log.info("Unable to connect to {}, retrying...", cxFac.getBrokerURL());
				log.debug("JMSException establishing connection", jmse);
				
				// Try again unless we've already reached maximum
				
				if(maxConnectAttempts > 0 && connectAttempts >= maxConnectAttempts) {
					
					log.error("Could not connect to host: {}", jmse.getMessage());
					return false;
					
				}
				
				try {
					
					// Wait a beat before trying again
					log.debug("Waiting {} ms", connectRetryDelay);
					Thread.sleep(connectRetryDelay);
					
				} catch(InterruptedException ie) {}
			}
		}
		
		return true;
	}
	
	protected void disconnect() {
		
		setConnectionState(ConnectionState.DISCONNECTED);
		log.info("{} connection to {} is DOWN", getLocalID(), getRemoteID());
		
		if(cxn != null) {
			
			try {
				
				// Stop processing messages on the connection
				cxn.stop();
				log.debug("Connection stopped");
				
			} catch(JMSException jmse) {
				
				log.debug("Caught JMSException stopping Connection", jmse);
				
			}
		}
		
		// Close other channels
		destroyChannels();
		
		if(ka != null) {
			
			// Close keep-alive channel
			ka.close();
			log.debug("Keep-alive channel closed");
			
		}
		
		if(cxn != null) {
			
			try {
				
				// Close the connection
				cxn.close();
				log.debug("Connection closed");
				
			} catch(JMSException jmse) {
				
				log.debug("Caught JMSException closing Connection", jmse);
				
			}
			
			cxn = null;

		}
	}
	
	protected long getMillisSinceLastResp() {
		
		return System.currentTimeMillis() - resp.getLastCreatedTime();
		
	}
	
	protected boolean reset() {
		
		log.debug("Connection reset");
		disconnect();
		return connect();
	
	}
	
	protected void sendKA(long inReplyTo) throws JMSException {
		
		if(cxn == null) {
			
			log.debug("sendKA(): Connection is null");
			throw new IllegalStateException("No connection to broker");
			
		}
		
		if(ka == null) {
			
			log.debug("sendKA(): ka is null");
			throw new IllegalStateException("No keep-alive channel");
			
		}
		
		// Build the message
		Message reply = ka.createMessage();
		reply.setStringProperty(propertySenderID, getLocalID());
		reply.setLongProperty(propertyCreatedTime, System.currentTimeMillis());
		reply.setLongProperty(propertyInReplyTo, inReplyTo);
				
		// Send the message
		ka.sendMessage(reply);
		log.debug("Sent message {}", reply.getJMSMessageID());
		
	}
	
	protected synchronized void setConnectionState(ConnectionState cxnState) {
		
		log.debug("Connection state={}", cxnState);
		this.cxnState = cxnState;
		cxnStateProperty.setConnectionState(cxnState);
		
		// Notify any waiting threads of state change
		notifyAll();
		
	}
	
	
	public void close() {
		
		// Stop monitor thread
		log.debug("Closing KeepAliveConnection from {} to {}", clientID, serverID);
		running = false;
		monitorThread.interrupt();
		
	}
	
	public long getConnectRetryDelay() {
		
		return connectRetryDelay;
		
	}
	
	public Connection getConnection() {
		
		return cxn;
		
	}
	
	public ConnectionState getConnectionState() {
		
		return cxnState;
		
	}
	
	public String getClientID() {
		
		return clientID;
		
	}
	
	public int getMaxConnectAttempts() {
		
		return maxConnectAttempts;
		
	}
	
	public long getMonitorTimeout() {
		
		return monitorTimeout;
		
	}
	
	public long getReplyDelay() {
		
		return replyDelay;
		
	}
	
	public String getServerID() {
		
		return serverID;
		
	}
	
	
	public void setConnectRetryDelay(long connectRetryDelay) {
		
		log.debug("ConnectRetryDelay={}", connectRetryDelay);
		this.connectRetryDelay = connectRetryDelay;
		
	}
	
	public void setMaxConnectAttempts(int maxConnectAttempts) {
		
		log.debug("MaxConnectAttempts={}", maxConnectAttempts);
		this.maxConnectAttempts = maxConnectAttempts;
		
	}
	
	public void setMonitorTimeout(long monitorTimeout) {
		
		log.debug("MonitorTimeout={}", monitorTimeout);
		this.monitorTimeout = monitorTimeout;
		
	}
	
	public void setReplyDelay(long replyDelay) {
		
		log.debug("ReplyDelay={}", replyDelay);
		this.replyDelay = replyDelay;
		
	}
	
	public void start() {
		
		// Start monitor thread
		if(monitorThread != null && !monitorThread.isAlive()) {

			log.debug("Calling start() on monitorThread");
			running = true;
			monitorThread.start();
			
		}
	}
	
	public String toString() {
		
		return String.format("%s from %s to %s [%s]", getClass().getName(), clientID, serverID, getConnectionState());
		
	}
	
	private class KeepAliveMessageListener implements MessageListener
	{
		
		@Override
		public void onMessage(Message msg) {
			
			try {

				// Read message properties
				long createdTime = msg.getLongProperty(propertyCreatedTime);
				String senderID = msg.getStringProperty(propertySenderID);
				log.debug("KA received ({}) from {}", createdTime, senderID);
				
				// Update response
				resp.update(createdTime);
				
				// Wait for a short time before sending a message back
				try {
					
					Thread.sleep(replyDelay);

				} catch(InterruptedException ie) { }
				
				// Send message in opposite direction
				sendKA(createdTime);
									
			} catch(JMSException jmse) {
				
				log.warn("JMSException processing KA message", jmse);
				
			} catch(RuntimeException re) {
				
				log.warn("Exception in KeepAliveMessageListener", re);
				
			}
		}
	}
	
	class KeepAliveMonitor implements Runnable
	{

		@Override
		public void run() {
			
			log.debug("Monitor for {} started", getLocalID());
			
			long lastValue = resp.getLastCreatedTime();
			
			while(running) {
				
				try {
					
					while(running && !resp.hasChanged(lastValue, monitorTimeout)) {
						
						// Value in response hasn't changed
						log.info("Last response received from {} received {} ms ago", getRemoteID(), System.currentTimeMillis() - resp.getLastReceivedTime());
						
						// Call out to check if we should keep running
						running = cxnTimeout();
						log.debug("running={}", running);
						
					}
					
					// Save current value
					lastValue = resp.getLastCreatedTime();
					log.debug("lastValue={}", lastValue);
					
				} catch(RuntimeException re) {
					
					log.warn("Exception in ConnectionMonitor", re);
					
				}
			}
			
			log.debug("Monitor for {} stopped, disconnecting", getLocalID());
			disconnect();
			
		}
	}
}
