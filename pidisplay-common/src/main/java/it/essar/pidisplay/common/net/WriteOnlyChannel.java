package it.essar.pidisplay.common.net;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

public class WriteOnlyChannel extends AbstractChannel
{
	
	private Session sess;
	private MessageProducer mp;
	
	public WriteOnlyChannel(Connection con, String qName) throws JMSException {
		
		super(con, qName);
		
		// Must call this early to set up message consumer before connection is started
		init();

	}
	
	@Override
	protected void init() throws JMSException {
		
		sess = cxn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Queue q = sess.createQueue(qName);
		mp = sess.createProducer(q);
	
	}
	
	@Override
	public void close() {
		
		if(sess != null) {
			
			try {
				
				sess.close();

			} catch(JMSException jmse) {

			}
			
			sess = null;
			
		}
	}
	
	public Message createMessage() throws JMSException {
		
		while(sess == null) {
			
			reset();
			
		}
		
		return sess.createTextMessage();
		
	}
	
	public void sendMessage(Message msg) throws JMSException {
		
		while(mp == null) {
			
			reset();
			
		}
		
		mp.send(msg);
	
	}
}
