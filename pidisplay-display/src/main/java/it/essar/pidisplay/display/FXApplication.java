package it.essar.pidisplay.display;

import it.essar.pidisplay.apps.dashboard.display.DashboardApplication;
import it.essar.pidisplay.common.appapi.ControlChannel;
import it.essar.pidisplay.common.appapi.ControlChannelMessage;
import it.essar.pidisplay.common.appapi.DataChannel;
import it.essar.pidisplay.common.appapi.Display;
import it.essar.pidisplay.common.appapi.DisplayEnvironment;
import it.essar.pidisplay.common.appapi.PiDisplayApp;
import it.essar.pidisplay.common.appapi.ServerInfo;
import it.essar.pidisplay.common.net.ConnectionState;
import it.essar.pidisplay.common.net.ConnectionStateProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FXApplication extends Application implements Runnable
{
	// Class logger
	private static final Logger log = LogManager.getLogger(FXApplication.class);
	
	// Map of loaded applications
	private final HashMap<String, PiDisplayApp> apps = new HashMap<>();
	
	private boolean running;
	private ControlChannel ctl;
	private DataChannel dat;
	private String clientID, serverID;
	private Thread controlThread;
	private URI brokerURI;
	
	// FX components
	private BaseScene base;
	private Stage primaryStage;
	
	public FXApplication() {
		
		// Create FX components
		base = new BaseScene();

	}
	
	private void closeApplication() {
		
		primaryStage.close();
		
	}
	
	private void handleException(String message, Throwable t) {
		
		log.error(message, t);
		
		if(getActiveApplicationID() == null || !apps.get(getActiveApplicationID()).handleException(message, t)) {
	
			// Handle exception in base scene
			base.handleException(message, t);
	
		}
	}
	
	private void loadApplication(String appID) {
		
		PiDisplayApp appl = apps.get(appID);
		
		if(appl != null) {
		
			log.info("Loading application ID: {}", appl.getApplicationID());
			appl.load();
			
		}
	}
	
	private void openDataChannel() {
		
		log.debug("Opening data channel");
		
		// Create data channel connection
		dat = new DisplayDataChannel();
		
		if(dat == null) {
			
			log.debug("Null data channel");
			throw new DataChannelException("Data channel not established");
			
		}
			
		// Read information object from data channel
		ServerInfo init = dat.getInfo();
		
		if(init == null) {
			
			log.debug("Null ServerInit | dat={}", dat);
			throw new DataChannelException("Invalid server information");
			
		}
			
		serverID = init.getServerID();
		clientID = init.getClientID();
		log.debug("serverID={} | clientID={}", serverID, clientID);

		String brokerURIStr = init.getBrokerURL();
		try {
			
			brokerURI = new URI(brokerURIStr);
			log.debug("brokerURI={}", brokerURI.toString());
				
		} catch(URISyntaxException use) {
				
			log.warn("Invalid broker URI: {}", brokerURIStr);
			
		}
	}
	
	private void openControlChannel() {
		
		log.debug("Opening control channel");
			
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
	
	private void processMessage(ControlChannelMessage cMsg) {
		
		if(cMsg == null) {
			
			throw new IllegalArgumentException("Null message");
			
		}
		
		
		// Get application ID
		String appID = cMsg.getMessageAppID();
		String msgType = cMsg.getMessageType();
		log.debug("processMessage(): application={}; message type={}", appID, msgType);

		// Pass to relevant application
		if(appID == null || CoreApplication.APPLICATION_ID.equals(appID)) {
		
			log.info("Process core application message: {}", msgType);
			
			// Handle by core application
			switch(CoreApplication.MessageTypes.valueOf(msgType)) {
			
				case REFRESH:
					
					log.debug("Core application: REFRESH");
					break;
					
				case RESET:

					// Load default application
					log.debug("Core application: RESET");
					loadApplication(DashboardApplication.APP_ID);
					break;
					
				case SHUTDOWN:
					
					// Shutdown the application
					log.debug("Core application: SHUTDOWN");
					closeApplication();
					break;
				
				case UPDATE:
					
					log.debug("Core application: UPDATE");
					break;
					
				default:
					
					throw new UnsupportedOperationException("Unknown message type for core application: " + cMsg.getMessageType());
			
			}

		} else {
			
			if(apps.containsKey(appID)) {
				
				// Handle by defined application
				apps.get(appID).processMessage(cMsg);
				
			} else {
				
				// Log and ignore
				log.info("Ignoring message received with unknown application ID: {}", appID);
				
			}
		}
	}
	
	private synchronized void setConnectionState(ConnectionState newState) {
		
		if(base != null) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					base.cxnStatePane.setConnectionState(newState);
				
				}
			});
		}
		
		// Release any waits based on connection state
		notifyAll();
		
	}
	
	public String getActiveApplicationID() {
		
		return null;
		
	}
	
	public PiDisplayApp getApplication(String appID) {
		
		return apps.get(appID);
		
	}
	
	public void registerApplication(PiDisplayApp app) {
		
		if(app != null) {
			
			log.info("Registering application: {}", app.getApplicationID());
			
			// Store application in application map
			apps.put(app.getApplicationID(), app);
			
			// Initialise application with display environment
			app.init(new FXApplicationDisplayEnvironment());
		
		}
	}
	
	@Override
	public void run() {
		
		log.info("{} started", Thread.currentThread().getName());

		while(ctl != null && running) {
			
			try {
				
				// Read a message from the control channel
				ControlChannelMessage cMsg = ctl.readMessage();

				// Process the message
				processMessage(cMsg);
				
			} catch(ControlChannelException cce) {
				
				// Something went wrong on the control channel
				log.warn(cce.getMessage(), cce);
				handleException("Unable to connect to server", cce);
				
				// Wait a little bit
				try {
					
					Thread.sleep(30000L);
					
				} catch(InterruptedException ie) { }
				
				// Re-establish control connection
				openControlChannel();
				
			} catch(RuntimeException re) {
					
				// Handle any other exception in processing the message
				log.error(re.getMessage(), re);
				
				// take a break so don't loop like a fritz
				try {
					
					Thread.sleep(5000L);
				
				} catch(InterruptedException ie) { }
			}
		}
		
		ctl.close();
		
		log.info("{} stopped", Thread.currentThread().getName());
		
	}
	
	@Override
	public void init() throws Exception {
		
		super.init();
		
		try {
			
			// Establish data channel and retrieve client ID
			openDataChannel();
					
			// Establish control channel
			openControlChannel();
			
			// Create control thread
			controlThread = new Thread(this, clientID + "-Control");
		
		} catch(DataChannelException | ControlChannelException e) {
			
			handleException("Unable to connect to server", e);
			
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		running = true;
		if(controlThread != null) {

			controlThread.start();
			
		}
		
		this.primaryStage = primaryStage;
		//primaryStage.setFullScreen(true);
		primaryStage.setScene(base);
		primaryStage.show();
		
	}
	
	@Override
	public void stop() throws Exception {

		running = false;
		if(controlThread != null) {
		
			controlThread.interrupt();
			
		}
		if(ctl != null) {
			
			ctl.close();
		}
		
		super.stop();
		
	}
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
	
	private class ConnectionStateObserver implements Observer
	{
		
		@Override
		public void update(Observable o, Object obj) {
			
			ConnectionStateProperty csp = (ConnectionStateProperty) o;

			log.debug("Connection state changed: {}", csp.getNewState());
			
			setConnectionState(csp.getNewState());

		}
	}
	
	private class FXApplicationDisplayEnvironment implements DisplayEnvironment
	{
		
		@Override
		public ControlChannel getControlChannel() {

			return ctl;
		
		}
		
		@Override
		public DataChannel getDataChannel() {

			return dat;
		
		}
		
		@Override
		public Display getDisplayClient() {
			
			return base;
		
		}
	}
}
