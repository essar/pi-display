package it.essar.pidisplay.common.appapi;

public interface Application
{

	void init(DisplayEnvironment env);
	
	void load();
	
	void processMessage(ControlChannelMessage cMsg);
	
	public String getApplicationID();
	
}
