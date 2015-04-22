package it.essar.pidisplay.common.net.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

public class ReadOnlyChannel
{
	private final Connection cxn;
	private final String qName;
	
	private Session sess;
	private MessageConsumer mc;
	
	public ReadOnlyChannel(Connection con, String qName) {
		
		this.cxn = con;
		this.qName = qName;
		
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
