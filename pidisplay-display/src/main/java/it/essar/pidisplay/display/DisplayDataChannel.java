package it.essar.pidisplay.display;

import it.essar.pidisplay.common.appapi.DataChannel;
import it.essar.pidisplay.common.appapi.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DisplayDataChannel implements DataChannel
{
	private static final Logger log = LogManager.getLogger(DisplayDataChannel.class);
	
	private HttpURLConnection data;
	private final URL url;
	
	public DisplayDataChannel() {
		
		this(DisplayProperties.getDataConnectionURL());

	}

	public DisplayDataChannel(URL serverURL) {
		
		this.url = serverURL;
		
	}
	
	private void openConnection(String path) throws IOException {
		
		if(url == null) {
			
			throw new DataChannelException("No URL");
			
		}
		URL u = new URL(url, path);
		log.debug("Opening connection to server: {}", u.toExternalForm());

		data = (HttpURLConnection) u.openConnection();
		log.debug("Connection established");
		
	}
	
	private JSONObject readJSONObject(String path) throws IOException, ParseException {
		
		BufferedReader r = null;
		JSONObject obj = null;
		
		try {
			
			openConnection(path);
			r = new BufferedReader(new InputStreamReader(data.getInputStream(), data.getContentEncoding()));
			log.debug("Opened reader to {}", path);
			
			JSONParser p = new JSONParser();
			obj = (JSONObject) p.parse(r);
			log.debug("Parsed JSON object: {}", obj.toJSONString());
			
		} finally {
			
			try {
			
				if(r != null) {

					r.close();
				
				}
				
			} catch(IOException ioe) {
			
				log.warn("Caught IOException closing reader", ioe);
				
			}
		}

		return obj;
		
	}
	
	@Override
	public byte[] getElement(String path) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public ServerInfo getInfo() {

		try {

			JSONObject obj = readJSONObject("/clientID");
			
			String clientID = (String) obj.get("clientID");
			String serverID = (String) obj.get("serverID");
			String brokerURL = (String) obj.get("brokerURL");
			
			ServerInfo init = new ServerInfo();
			init.setClientID(clientID);
			init.setServerID(serverID);
			init.setBrokerURL(brokerURL);
			
			return init;
			
		} catch(IOException | ParseException e) {
			
			// Log, wrap and raise
			throw new DataChannelException("Unable to read server info", e);
			
		}
	}
}
