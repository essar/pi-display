package it.essar.pidisplay.common.appapi;

public interface ControlChannel
{
	
	public void close();

	public ControlChannelMessage readMessage();
	
}
