package it.essar.pidisplay.server.http;

import java.net.InetSocketAddress;

import it.essar.pidisplay.common.proc.MonitorableProcess;
import it.essar.pidisplay.common.proc.ProcessMonitor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class HTTPServer implements MonitorableProcess
{
	private static final Logger log = LogManager.getLogger(HTTPServer.class);
	private static HTTPServer hs;
	
	public static final int DEFAULT_PORT = 8080;
	public static final String DEFAULT_HOST = "localhost";
	
	private final BaseHandler base;
	private final ProcessMonitor monitor;
	
	private InetSocketAddress bindAddress;
	private Server httpd;
	
	private HTTPServer(String bindHost, int bindPort) {
		
		bindAddress = InetSocketAddress.createUnresolved(bindHost, bindPort);
		base = new BaseHandler();
		
		createAndConfigureServer();
		
		// Create monitor
		monitor = new ProcessMonitor(this);
	}
	
	private void createAndConfigureServer() {
		
		httpd = new Server(bindAddress);
		
		// Configure HTTP server
		httpd.setHandler(base);
		
	}
	
	public BaseHandler getBaseHandler() {
		
		return base;
		
	}

	public String getProcessName() {
		
		return "HTTPServer";
	
	}
	
	public boolean isProcessRunning() {
		
		if(httpd == null) {
			
			log.debug("Server is null");
			return false;
			
		}
		
		if(! httpd.isRunning()) {
			
			log.debug("Server is not running");
			return false;
			
		}
		
		return true;
	}
	
	public boolean startProcess() {
		
		try {
			
			if(! isProcessRunning()) {
			
				httpd.start();
				
			}
			
			if(! isProcessRunning()) {
				
				// Server did not start correctly, recreate and try again
				createAndConfigureServer();
				httpd.start();
				
			}
			
			if(isProcessRunning()) {
				
				log.info("HTTP Server started");
				return true;
				
			} else {
				
				log.warn("Server failed to start");
				return false;
				
			}
			
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to start server", e);
			
		}
	}
	
	public boolean stopProcess() {

		try {
			
			if(isProcessRunning()) {
			
				httpd.stop();
				
			}
			
			if(! isProcessRunning()) {
				
				log.info("HTTP Server stopped");
				return true;
				
			} else {
				
				log.warn("Server failed to stop");
				return false;
				
			}
			
		} catch(Exception e) {
			
			throw new RuntimeException("Unable to stop server", e);
			
		}
	}
	
	public static void startServer() {
		
		startServer(DEFAULT_HOST, DEFAULT_PORT);
		
	}
	
	public static void startServer(String bindHost, int bindPort) {
		
		if(hs == null) {
				
			hs = new HTTPServer(bindHost, bindPort);
			
		}
			
		// Start server monitor - also starts server
		hs.monitor.start();
		
	}
	
	public static void stopServer() {
		
		if(hs != null) {
			
			// Stop the monitor - also stops server
			hs.monitor.stop();
			
		}
		
		hs = null;
	}
	
	
	public static void main(String[] args) {
		
		// Start the server
		startServer();
		
		try {
			Thread.sleep(10000L);
		} catch(InterruptedException ie) {
			
		}
		
		// Abort the server - monitor should restart it
		try {
			hs.httpd.stop();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		try {
			Thread.sleep(10000L);
		} catch(InterruptedException ie) {
			
		}
		
		// Stop the server
		stopServer();
		
	}
}
