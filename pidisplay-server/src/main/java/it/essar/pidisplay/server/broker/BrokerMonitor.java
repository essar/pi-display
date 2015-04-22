package it.essar.pidisplay.server.broker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
class BrokerMonitor implements Runnable
{
	private static final Logger log = LogManager.getLogger(BrokerMonitor.class);
	private static final int MAX_RETRY_COUNT = 5;
	
	private final MessageBroker broker;
	
	private int retryCount = 0;
	private boolean running;
	private MonitorThread thread;
	
	public BrokerMonitor(MessageBroker broker) {
		
		this.broker = broker;
	
	}
	
	private void restartBroker() {
		
		if(broker != null) {
			
			try {
				
				if(broker.isProcessRunning()) {
				
					// Stop the broker
					broker.stopProcess();
					
				}
				
				if(! broker.isProcessRunning()) {
					
					// Start the broker
					broker.startProcess();
					
				}
			
			} catch(Exception e) {
				
				throw new RuntimeException("Unable to restart broker", e);
				
			}
		}
	}
	
	
	void start() {
		
		getThread().startMonitor();
		
	}
	
	void stop() {
		
		getThread().stopMonitor();
		
	}
	
	MonitorThread getThread() {
		
		if(thread == null) {
			
			thread = new MonitorThread();
		
		}
		
		return thread;
	}
	
	
	public void run() {
		
		running = true;
		while(running && retryCount < MAX_RETRY_COUNT) {
			
			try {
				
				if(! broker.isProcessRunning()) {
					
					// Broker isn't running, so give it a poke
					restartBroker();
					
				} else {
					
					log.debug("Broker is alive!");
					
					// Reset retry count
					retryCount = 0;
					
				}
				
				// Idle for 1 second
				Thread.sleep(1000L);
				
			} catch(InterruptedException ie) {
				
				// Do nothing other than break out of loop
				log.debug("Interrupt caught in run() loop");
				
			} catch(RuntimeException re) {
				
				retryCount ++;
				log.error(re.getMessage());
				log.debug(re.getMessage(), re);
				
			}
		}
	}

	
	class MonitorThread extends Thread
	{
		
		MonitorThread() {
			
			super(BrokerMonitor.this, "BrokerMonitor");
			
		}
		
		void startMonitor() {
			
			start();
			
		}
		
		void stopMonitor() {
			
			running = false;
			interrupt();

		}
	}
}
