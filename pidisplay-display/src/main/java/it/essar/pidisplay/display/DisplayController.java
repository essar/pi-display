package it.essar.pidisplay.display;

import it.essar.pidisplay.apps.dashboard.display.DashboardApplicationPane;
import it.essar.pidisplay.common.appapi.Application;
import it.essar.pidisplay.common.appapi.ControlChannel;
import it.essar.pidisplay.common.appapi.ControlChannelMessage;
import it.essar.pidisplay.common.appapi.DataChannel;
import it.essar.pidisplay.common.appapi.DisplayEnvironment;
import it.essar.pidisplay.common.appapi.ServerInfo;
import it.essar.pidisplay.common.net.ConnectionStateProperty;
import it.essar.pidisplay.display.fx.ClientApplication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayController implements Runnable
{
	private static final Logger log = LogManager.getLogger(DisplayController.class);
	
	private final HashMap<String, Application> apps = new HashMap<String, Application>();
	
	private boolean running;
	private ClientApplication display;
	private ControlChannel ctl;
	private DataChannel dat;
	private String clientID, serverID;
	private Thread controlThread;
	private URI brokerURI;
	
	public DisplayController(ClientApplication display) {
		// TODO Auto-generated constructor stub
		
		this.display = display;
		
		initDataChannel();
		
		initControlChannel();
		
		controlThread = new Thread(this, clientID + "-Control");
		
	}
	
	private void loadApplication(String appID) {
		
		
	}
	
	private void processMessage(ControlChannelMessage cMsg) {
		
		if(cMsg == null) {
			
			throw new IllegalArgumentException("Null message");
			
		}
		
		// Get application ID
		String appID = cMsg.getMessageAppID();
		
		// Pass to relevant application
		if(appID == null || CoreApplication.APPLICATION_ID.equals(appID)) {
			
			// Handle by core application
			switch(CoreApplication.MessageTypes.valueOf(cMsg.getMessageType())) {
			
				case REFRESH:
					
					break;
					
				case RESET:
					
					// TODO Use loadApplication
					display.setApplicationPane(new DashboardApplicationPane(clientID));
					break;
					
				case SHUTDOWN:
					
					stop();
					break;
					
				case UPDATE:
					
					break;
					
				default:
					
					throw new UnsupportedOperationException("Unknown message type");
			
			}

			
			
		} else {
			
			if(apps.containsKey(appID)) {
				
				// Handle by defined application
				apps.get(appID).processMessage(cMsg);
				
			} else {
				
				// Log and ignore
				log.warn("Ignoring message received with unknown application ID: {}", appID);
				
			}
		}
	}
	
	private void initControlChannel() {
		
		log.debug("Initialising control channel");
		
		if(clientID == null || serverID == null) {
			
			throw new IllegalStateException("Unknown clientID or serverID");
			
		}
		
		DisplayControlChannel jmsCtl = new DisplayControlChannel(brokerURI, clientID, serverID);
		ctl = jmsCtl;
		
		// Place observer on connection state
		jmsCtl.cxnStateProperty.addObserver(new ConnectionStateObserver());
		
		// Start control channel
		jmsCtl.start();
		
	}
	
	private void initDataChannel() {
		
		log.debug("Initialising data channel");
		
		String dataURI = "http://localhost:8001";
		try {
		
			log.debug("dataURI={}", dataURI);
			dat = new DisplayDataChannel(new URI(dataURI));
			
		} catch(URISyntaxException use) {
			
			log.warn("Invalid data channel URI: {}", dataURI);
			
		}
		
		if(dat == null) {
			
			log.debug("Null data channel");
			throw new DataChannelException("Data channel not established");
			
		}
			
		// Read information object from data channel
		ServerInfo init = dat.getInfo();
		
		if(init == null) {
			
			log.debug("Null ServerInit");
			throw new DataChannelException("Invalid server information");
			
		}
			
		serverID = init.getServerID();
		log.debug("serverID={}", serverID);
		
		clientID = init.getClientID();
		log.debug("clientID={}", clientID);

		String brokerURIStr = init.getBrokerURL();
		try {
			
			brokerURI = new URI(brokerURIStr);
			log.debug("brokerURI={}", brokerURI.toString());
				
		} catch(URISyntaxException use) {
				
			log.warn("Invalid broker URI: {}", brokerURIStr);
			
		}
	}
	
	public Application getApplication(String appID) {
		
		return apps.get(appID);
		
	}
	
	public void registerApplication(Application app) {
		
		if(app != null) {
			
			log.info("Registering application: {}", app.getApplicationID());
			
			// Store application in application map
			apps.put(app.getApplicationID(), app);
			
			// Initialise application with display environment
			app.init(new DisplayControllerEnvironment());
		
		}
	}
	
	@Override
	public void run() {
		
		log.info("Display controller started");

		while(ctl != null && running) {
			
			try {
				
				// Read a message from the control channel
				ControlChannelMessage cMsg = ctl.readMessage();

				// Process the message
				processMessage(cMsg);
	
			} catch(RuntimeException re) {
					
				// Handle any exception in processing the message
				log.warn("Caught Exception processing control message", re);
					
			}
		}
		
		ctl.close();
		
		log.info("Display controller stopped");
		
	}
	
	public void start() {
		
		running = true;
		controlThread.start();
		
	}
	
	public void stop() {
		
		running = false;
		controlThread.interrupt();
		
	}
	
	private class ConnectionStateObserver implements Observer
	{
		@Override
		public void update(Observable o, Object obj) {
			
			ConnectionStateProperty csp = (ConnectionStateProperty) o;

			log.debug("Connection state changed: {}", csp.getNewState());
			
			if(display != null) {
				
				display.setConnectionState(csp.getNewState());
				
			}
		}
	}
	
	private class DisplayControllerEnvironment implements DisplayEnvironment
	{
		@Override
		public ControlChannel getControlChannel() {

			return ctl;
			
		}
		
		@Override
		public DataChannel getDataChannel() {

			return dat;
		
		}
	}
}
