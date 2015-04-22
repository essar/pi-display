package it.essar.pidisplay.server.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

class BaseHandler extends AbstractHandler
{

	public void handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

		/*
		 * http://jlunaquiroga.blogspot.co.uk/2014/01/restful-web-services-with-jetty-and.html?_sm_au_=iHV7DL4sFrvPfHVR
		 * 
		 * Handle page requests
		 * /api/ - API transactions
		 * /ctl/ - pages to be rendered by the controller
		 * /page/ - pages to be rendered by the display
		 */
		
		if(target != null) {
			
			if(target.startsWith("/api/")) {
			
				new ApiHandler().handle(target, baseRequest, req, res);
				return;
			
			}
			
			if(target.startsWith("/ctl/")) {
				
				new ControllerHandler().handle(target, baseRequest, req, res);
				return;
				
			}
			
			if(target.startsWith("/page/")) {
				
				new PageHandler().handle(target, baseRequest, req, res);
				return;
				
			}
		}
		
		// Default - return error status
		res.setContentType("text/html;charset=utf-8");
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        baseRequest.setHandled(true);
        
	}
}
