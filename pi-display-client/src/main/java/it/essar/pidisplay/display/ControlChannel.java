package it.essar.pidisplay.display;

public interface ControlChannel
{
	
	public void close();

	public ControlChannelMessage readMessage();
	
}
