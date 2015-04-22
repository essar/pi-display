package it.essar.pidisplay.common.net;

import java.util.Observable;

public class ConnectionStateProperty extends Observable
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