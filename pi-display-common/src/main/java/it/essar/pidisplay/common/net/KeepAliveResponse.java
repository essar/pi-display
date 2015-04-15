package it.essar.pidisplay.common.net;

class KeepAliveResponse
{
	private long createdTime, receivedTime, stt;
	
	KeepAliveResponse() {
		
		createdTime = System.currentTimeMillis();
		receivedTime = System.currentTimeMillis();
		stt = 0L;
		
	}
	
	synchronized boolean hasChanged(long since, long wait) {
		
		if(createdTime != since) {
			
			return true;
		}
		try {
			
			wait(wait);
			
		} catch(InterruptedException ie) { }
		
		return createdTime != since;
		
	}
	
	long getLastCreatedTime() {
		
		return createdTime;
		
	}
	
	long getLastReceivedTime() {
		
		return receivedTime;
		
	}
	
	long getLastStt() {
		
		return stt;
		
	}
	
	synchronized void update(long createdTime) {
		
		this.createdTime = createdTime;
		this.receivedTime = System.currentTimeMillis();
		this.stt = this.receivedTime - this.createdTime;
		notifyAll();
		
	}
}
