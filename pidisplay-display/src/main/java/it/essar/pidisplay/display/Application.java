package it.essar.pidisplay.display;

public interface Application
{

	void init(DisplayEnvironment env);
	
	void load();
	
	void processMessage(ControlChannelMessage cMsg);
	
	public String getApplicationID();
	
}
