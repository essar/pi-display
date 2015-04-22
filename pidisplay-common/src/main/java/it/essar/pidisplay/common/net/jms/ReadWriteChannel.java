package it.essar.pidisplay.common.net.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

public class ReadWriteChannel
{
	private final Connection cxn;
	private final String qName;
	
	private Session sess;
	private MessageConsumer mc;
	private MessageListener ml;
	private MessageProducer mp;
	private String selector;
	
	public ReadWriteChannel(Connection con, String qName) {
		
		this(con, qName, null, null);
		
	}
	
	public ReadWriteChannel(Connection con, String qName, MessageListener ml, String selector) {
		
		this.cxn = con;
		this.qName = qName;
		this.ml = ml;
		this.selector = selector;
		
	}
	
	private void reset() throws JMSException {
		
		close();
		
		// Recreate objects
		sess = cxn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Queue q = sess.createQueue(qName);
		mc = sess.createConsumer(q, selector);
		mp = sess.createProducer(q);
	
		if(ml != null) {
			
			mc.setMessageListener(ml);
			
		}
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
	
	public Message createMessage() throws JMSException {
		
		while(sess == null) {
			
			reset();
			
		}
		
		return sess.createTextMessage();
		
	}
	
	public Message readMessage() throws JMSException {
		
		while(mc == null) {
				
			reset();
				
		}
			
		return mc.receive();
			
	}
	
	public void sendMessage(Message msg) throws JMSException {
		
		while(mp == null) {
			
			reset();
			
		}
		
		mp.send(msg);
	
	}
}
