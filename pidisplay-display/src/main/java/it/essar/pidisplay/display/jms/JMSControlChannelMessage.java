package it.essar.pidisplay.display.jms;

import it.essar.pidisplay.display.ControlChannelMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

class JMSControlChannelMessage implements ControlChannelMessage
{
	private String msgAppID, msgBody, msgType;
	
	public JMSControlChannelMessage(Message msg) throws JMSException {
		
		TextMessage txt = (TextMessage) msg;
		msgAppID = txt.getStringProperty("application-id");
		msgBody = txt.getText();
		msgType = txt.getStringProperty("msgType");
		
	}
	
	//@Override
	public String getMessageAppID() {

		return msgAppID;
		
	}
	
	//@Override
	public String getMessageBody() {

		return msgBody;
	
	}
	
	//@Override
	public String getMessageType() {

		return msgType;
	
	}
}
