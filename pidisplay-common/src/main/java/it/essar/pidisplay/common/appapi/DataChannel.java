package it.essar.pidisplay.common.appapi;

public interface DataChannel
{

	public byte[] getElement(String path);
	
	public ServerInfo getInfo();
	
}
