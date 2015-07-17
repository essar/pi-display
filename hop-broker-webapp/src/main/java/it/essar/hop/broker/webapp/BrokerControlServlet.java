package it.essar.hop.broker.webapp;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet used for controlling the broker.
 * @author Steve Roberts <steve.roberts@essarsoftware.co.uk>
 * @version 1.0
 */
@WebServlet(asyncSupported=false, name="BrokerControlServlet", urlPatterns={"/"})
public class BrokerControlServlet extends HttpServlet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6619068539468601823L;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.getWriter().println("<html><body><h1>Hello world</h1></body></html>");
		
	}
}
