package it.essar.pidisplay.display.http;

import it.essar.pidisplay.display.DataChannel;
import it.essar.pidisplay.display.DataChannelException;
import it.essar.pidisplay.display.ServerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HTTPDataChannel implements DataChannel
{
	private static final Logger log = LogManager.getLogger(HTTPDataChannel.class);
	
	private HttpURLConnection data;
	private URI uri;

	public HTTPDataChannel(URI serverURI) {
		
		this.uri = serverURI;
		
	}
	
	private void openConnection(String path) throws IOException {
		
		URL u = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), path);
		log.debug("Opening connection to server: {}", u.toExternalForm());

		data = (HttpURLConnection) u.openConnection();
		
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
			
				log.debug("Caught IOException closing reader", ioe);
				
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
			
		} catch(IOException ioe) {
			
			// Log, wrap and raise
			log.debug(ioe.getClass().getCanonicalName(), ioe);
			throw new DataChannelException("Unable to read server info", ioe);
			
		} catch(ParseException pe) {
			
			// Log, wrap and raise
			log.debug(pe.getClass().getCanonicalName(), pe);
			throw new DataChannelException("Unable to read server info", pe);
			
		}
	}
}
