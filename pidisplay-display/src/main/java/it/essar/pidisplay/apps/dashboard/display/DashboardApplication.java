package it.essar.pidisplay.apps.dashboard.display;

import it.essar.pidisplay.display.Application;
import it.essar.pidisplay.display.ControlChannelMessage;
import it.essar.pidisplay.display.DisplayEnvironment;

public class DashboardApplication implements Application
{
	public static final String APP_ID = "it.essar.pidisplay.apps.dashboard";
	
	private DashboardApplicationPane pane;
	
	public DashboardApplication(String clientID) {
		
		pane = new DashboardApplicationPane(clientID);
		
	}
	
	@Override
	public String getApplicationID() {
		
		return APP_ID;
	
	}
	
	@Override
	public void init(DisplayEnvironment env) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void processMessage(ControlChannelMessage cMsg) {
		// TODO Auto-generated method stub
		
	}
}
