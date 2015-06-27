package it.essar.pidisplay.common.net;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

abstract class AbstractChannel
{

	protected final Connection cxn;
	protected final String qName;
	
	protected AbstractChannel(Connection cxn, String qName) {
		
		this.cxn = cxn;
		this.qName = qName;
		
	}
	
	protected abstract void init() throws JMSException;
	
	protected Message createMessage(Session sess) throws JMSException {
		
		while(sess == null) {
			
			reset();
			
		}
		
		return sess.createTextMessage();
		
	}
	
	protected Message readMessage(MessageConsumer mc) throws JMSException {
		
		while(mc == null) {
			
			reset();
				
		}
			
		return mc.receive();
		
	}
	
	protected void sendMessage(MessageProducer mp, Message msg) throws JMSException {
		
		while(mp == null) {
			
			reset();
			
		}
		
		mp.send(msg);
		
	}
	
	public abstract void close() throws JMSException;
	
	public void reset() throws JMSException {
		
		close();
		init();
		
	}
	
	public boolean isAlive() {
		
		if(cxn == null) {
			
			return false;
			
		}
		
		try {
			
			cxn.createSession(false, Session.AUTO_ACKNOWLEDGE).close();
			
		} catch(JMSException jmse) {
			
			return false;
			
		}
		
		return true;
		
	}
}
