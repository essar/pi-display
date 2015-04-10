package it.essar.pidisplay.display;

public interface DataChannel
{

	public byte[] getElement(String path);
	
	public ServerInfo getInfo();
	
}
