package it.essar.pidisplay.common.net;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

class AbstractChannel
{

	protected final Connection cxn;
	protected final String qName;
	
	protected AbstractChannel(Connection cxn, String qName) {
		
		this.cxn = cxn;
		this.qName = qName;
		
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
