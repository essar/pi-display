package it.essar.pidisplay.common.net;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class ReadOnlyChannel extends AbstractChannel
{

	private Session sess;
	private MessageConsumer mc;
	
	public ReadOnlyChannel(Connection con, String qName) {
		
		super(con, qName);
		
	}
	
	private void reset() throws JMSException {
		
		close();
		
		// Recreate objects
		sess = cxn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		mc = sess.createConsumer(sess.createQueue(qName));
	
	}
	
	public void close() {
		
		if(sess != null) {
			
			try {
				
				sess.close();

			} catch(JMSException jmse) {

			}
			
			sess = null;
			
		}
	}
	
	public Message readMessage() throws JMSException {
		
		while(mc == null) {
				
			reset();
				
		}
			
		return mc.receive();
			
	}
}
