package it.essar.pidisplay.common.appapi;

public class ServerInfo
{
	
	private String brokerURL, clientID, serverID;
	
	private String[] appIDs;

	
	public String getBrokerURL() {
		
		return brokerURL;
		
	}
	
	public String getClientID() {
		
		return clientID;
		
	}
	
	public String getServerID() {
		
		return serverID;
		
	}
	
	public void setBrokerURL(String brokerURL) {
		
		this.brokerURL = brokerURL;
		
	}
	
	public void setClientID(String clientID) {
		
		this.clientID = clientID;
		
	}
	
	public void setServerID(String serverID) {
		
		this.serverID = serverID;
		
	}
	
}
