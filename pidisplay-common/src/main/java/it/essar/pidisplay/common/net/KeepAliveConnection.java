package it.essar.pidisplay.common.net;

import java.util.Observable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class KeepAliveConnection
{
	protected final Logger log = LogManager.getLogger(KeepAliveConnection.class);
	
	private final String clientID, serverID;
	private final Thread monitorThread;
	
	protected final KeepAliveResponse resp;
	
	private boolean running = true;
	private ConnectionState cxnState;
	
	protected long monitorTimeout = 10000L;
	protected long replyDelay = 3000L;
	
	public final ConnectionStateProperty cxnStateProperty;
	
	protected KeepAliveConnection(String clientID, String serverID) {
		
		this.clientID = clientID;
		this.serverID = serverID;
		this.resp = new KeepAliveResponse();
		this.monitorThread = new Thread(new ConnectionMonitor(), getLocalID() + "-Monitor");
		
		cxnStateProperty = new ConnectionStateProperty();
		setConnectionState(ConnectionState.DISCONNECTED);
		
		log.debug("Initialised KeepAliveConnection from {} to {}", clientID, serverID);
		
	}
	
	
	protected abstract boolean connect();
	
	protected abstract void disconnect();
	
	protected abstract boolean cxnTimeout();
	
	protected abstract String getRemoteID();
	
	protected abstract String getLocalID();

	protected abstract void sendKA(long inReplyTo);
	
	
	protected void processKeepAlive(String senderID, long createdTime) {
		
		log.debug("KA received ({}) from {}", createdTime, senderID);
		
		// Update response
		resp.update(createdTime);
		
		// Wait for a short time before sending a message back
		try {
			
			Thread.sleep(replyDelay);

		} catch(InterruptedException ie) { }
		
		// Send message in opposite direction
		sendKA(createdTime);
		
	}
	
	protected boolean reset() {
		
		disconnect();
		return connect();
	
	}
	
	protected void setConnectionState(ConnectionState cxnState) {
		
		log.debug("Connection state={}", cxnState);
		this.cxnState = cxnState;
		cxnStateProperty.setConnectionState(cxnState);
		
	}
	
	public void close() {
		
		// Stop monitor thread
		log.debug("Closing KeepAliveConnection from {} to {}", clientID, serverID);
		running = false;
		monitorThread.interrupt();
		
	}
	
	public ConnectionState getConnectionState() {
		
		return cxnState;
		
	}
	
	public String getClientID() {
		
		return clientID;
		
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
	
	class ConnectionMonitor implements Runnable
	{

		//@Override
		public void run() {
			
			log.debug("Monitor for {} started", getLocalID());
			
			long lastValue = resp.getLastCreatedTime();
			
			while(running) {
				
				try {
					
					while(running && !resp.hasChanged(lastValue, monitorTimeout)) {
						
						// Value in response hasn't changed - increment the timeout counter
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
			
			log.debug("Monitor for {} stopped", getLocalID());
			disconnect();
			
		}
	}
	
	public static class ConnectionStateProperty extends Observable
	{
		
		private ConnectionState oldState, newState;
		
		public ConnectionState getOldState() {
			
			return oldState;
			
		}
		
		public ConnectionState getNewState() {
			
			return newState;
			
		}
		
		void setConnectionState(ConnectionState cxnState) {
			
			oldState = newState;
			newState = cxnState;
			
			if(newState != oldState) {
				
				setChanged();
				
			}
			
			notifyObservers();
		}
	}
}
