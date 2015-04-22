package it.essar.pidisplay.common.proc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProcessMonitor implements Runnable
{
	private static Logger log = LogManager.getLogger(ProcessMonitor.class);
	
	public static final int MAX_RETRIES = 10;
	
	private Thread thd;
	private boolean running;
	private final MonitorableProcess process;

	public boolean autoStop = true;
	public long delay = 1000L;
	
	public ProcessMonitor(MonitorableProcess process) {
		
		this.process = process;
		
	}
	
	private Thread getThread() {
		
		if(thd == null) {
			
			thd = new Thread(ProcessMonitor.this, process.getProcessName() + "-Monitor");
			
		}
	
		return thd;
	}
	
	public void run() {
		
		int retryCount = 0;
		running = true;
		
		while(running && (retryCount < MAX_RETRIES)) {

			try {
				
				if(! process.isProcessRunning()) {
					
					// Process isn't running, so start it
					process.stopProcess();
					process.startProcess();
					
				} else {
					
					log.debug("{} is alive", process.getProcessName());
					
					// Reset retry counter
					retryCount = 0;
					
				}
			
				// Sleep until next check
				Thread.sleep(delay);
				
			} catch(RuntimeException re) {
				
				log.warn("{} in monitor: {}", re.getClass().getName(), re.getMessage());
				log.debug(re.getClass().getCanonicalName(), re);
				retryCount ++;
				
			} catch(InterruptedException ie) { }
			
		}
		
		// Stop monitored process when stopping monitor
		if(autoStop) {
			
			process.stopProcess();
			
		}
		
		// Dispose of thread
		thd = null;
		
	}
	
	public synchronized void start() {
		
		// Start thread
		getThread().start();
		
		log.info("{} Monitor started", process.getProcessName());
		
	}
	
	public synchronized void stop() {
		
		// Interrupt thread
		running = false;
		getThread().interrupt();
		
		log.info("{} Monitor stopped", process.getProcessName());
		
	}
}
