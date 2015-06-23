package it.essar.pidisplay.common.appapi;

public interface PiDisplayApp
{
	
	boolean handleException(String msg, Throwable t);

	void init(DisplayEnvironment env);
	
	void load();
	
	void processMessage(ControlChannelMessage cMsg);
	
	public String getApplicationID();
	
}
