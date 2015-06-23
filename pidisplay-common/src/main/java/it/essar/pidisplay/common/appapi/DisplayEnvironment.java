package it.essar.pidisplay.common.appapi;

public interface DisplayEnvironment
{

	public ControlChannel getControlChannel();
	
	public DataChannel getDataChannel();
	
	public Display getDisplayClient();
	
}
