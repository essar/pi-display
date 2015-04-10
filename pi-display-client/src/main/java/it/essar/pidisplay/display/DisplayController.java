package it.essar.pidisplay.display;

import it.essar.pidisplay.display.http.HTTPDataChannel;
import it.essar.pidisplay.display.jms.JMSControlChannel;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DisplayController implements Runnable
{
	private static final Logger log = LogManager.getLogger(DisplayController.class);
	
	private final HashMap<String, Application> apps = new HashMap<String, Application>();
	
	private boolean running;
	private ControlChannel ctl;
	private DataChannel dat;
	private String clientID, serverID;
	private Thread controllerThread;
	private URI brokerURI;
	
	public DisplayController() {
		// TODO Auto-generated constructor stub
		
		initDataChannel();
		
		initControlChannel();
		
		controllerThread = new Thread(this, clientID + "-Control");
		controllerThread.start();
		
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
			processCoreMessage(cMsg);
			
		} else {
			
			if(apps.containsKey(appID)) {
				
				// Handle by defined application
				apps.get(appID).processMessage(cMsg);
				
			} else {
				
				// Log and ignore
				log.warn("Message received with unknown application ID: {}", appID);
				
			}
		}
	}
	
	private void processCoreMessage(ControlChannelMessage cMsg) {
		
		if(cMsg == null) {
			
			throw new IllegalArgumentException("Null message");
			
		}
		
		switch(CoreApplication.MessageTypes.valueOf(cMsg.getMessageType())) {
		
			case REFRESH:
				
				log.debug("REFRESH message");
				break;
				
			case RESET:
				
				log.debug("RESET message");
				break;
				
			case SHUTDOWN:
				
				log.debug("SHUTDOWN message");
				running = false;
				break;
				
			case UPDATE:
				
				log.debug("UPDATE message");
				break;
				
			default:
				
				throw new UnsupportedOperationException("Unknown message type");
		}
	}
	
	private void initControlChannel() {
		
		log.info("Initialising control channel");
		
		if(clientID == null || serverID == null) {
			
			throw new IllegalStateException("Unknown clientID or serverID");
			
		}
		
		ctl = new JMSControlChannel(brokerURI, clientID, serverID);
		
	}
	
	private void initDataChannel() {
		
		log.info("Initialising data channel");
		
		String dataURI = "http://localhost:8001";
		try {
		
			log.debug("dataURI={}", dataURI);
			dat = new HTTPDataChannel(new URI(dataURI));
			
		} catch(URISyntaxException use) {
			
			log.error("Invalid data channel URI: {}", dataURI);
			
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
				
			log.error("Invalid broker URI: {}", brokerURIStr);
				
		}
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
	
	//@Override
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
				log.error("Caught {} whilst processing control message: {}", re.getClass().getName(), re.getMessage());
				log.debug(re.getClass().getCanonicalName(), re);
					
			}
		}
		
		ctl.close();
		
		log.info("Display controller stopped");
		
	}
	
	private class DisplayControllerEnvironment implements DisplayEnvironment
	{
		//@Override
		public ControlChannel getControlChannel() {

			return ctl;
			
		}
		
		//@Override
		public DataChannel getDataChannel() {

			return dat;
		
		}
	}
	
	public static void main(String[] args) {
		
		//MessageBroker.startBroker();
		
		new DisplayController();
	
	}
}
