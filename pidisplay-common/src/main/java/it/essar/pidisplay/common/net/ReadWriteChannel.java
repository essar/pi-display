package it.essar.pidisplay.common.net;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

public class ReadWriteChannel extends AbstractChannel
{
	private Session sess;
	private MessageConsumer mc;
	private MessageListener ml;
	private MessageProducer mp;
	private String selector;
	
	public ReadWriteChannel(Connection con, String qName) throws JMSException {
		
		this(con, qName, null, null);
		
	}
	
	public ReadWriteChannel(Connection con, String qName, MessageListener ml, String selector) throws JMSException {
		
		super(con, qName);
		this.ml = ml;
		this.selector = selector;
		
		// Must call this early to set up message consumer before connection is started
		init();
		
	}
	
	@Override
	protected void init() throws JMSException {

		sess = cxn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
		Queue q = sess.createQueue(qName);
		mc = sess.createConsumer(q, selector);
		mp = sess.createProducer(q);
	
		if(ml != null) {
			
			mc.setMessageListener(ml);
			
		}
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
		
		return createMessage(sess);
		
	}
	
	public Message readMessage() throws JMSException {
		
		return readMessage(mc);
			
	}
	
	public void sendMessage(Message msg) throws JMSException {
		
		sendMessage(mp, msg);
		
	}
}
