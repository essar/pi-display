package it.essar.pidisplay.apps.dashboard.display;

import it.essar.pidisplay.common.appapi.PiDisplayApp;
import it.essar.pidisplay.common.appapi.ControlChannelMessage;
import it.essar.pidisplay.common.appapi.DisplayEnvironment;

public class DashboardApplication implements PiDisplayApp
{
	public static final String APP_ID = "it.essar.pidisplay.apps.dashboard";
	
	private final String clientID;
	
	private DashboardApplicationPane pane;
	private DisplayEnvironment env;
	
	public DashboardApplication(String clientID) {
		
		this.clientID = clientID;
		pane = new DashboardApplicationPane(clientID);
		
	}
	
	@Override
	public String getApplicationID() {
		
		return APP_ID;
	
	}
	
	@Override
	public boolean handleException(String msg, Throwable t) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void init(DisplayEnvironment env) {
		
		this.env = env;

	}
	
	@Override
	public void load() {
		
		// Load initial pane
		env.getDisplayClient().setApplicationPane(pane);
		pane.init();
		
	}
	
	@Override
	public void processMessage(ControlChannelMessage cMsg) {
		
		switch(DashboardApplication.MessageTypes.valueOf(cMsg.getMessageType())) {
		
			// Only functionality of the dashboard application is to display HTML from the server
			case DISPLAY:
		
				String path = cMsg.getMessageBody(); // Read path from msg
				pane.loadPage(path);
				break;
				
			default:
				
				throw new UnsupportedOperationException("Unknown message type");
		
		}
	}
	
	public enum MessageTypes
	{ DISPLAY }
}
