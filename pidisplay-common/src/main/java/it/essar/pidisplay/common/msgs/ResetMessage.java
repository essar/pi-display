package it.essar.pidisplay.common.msgs;

import it.essar.pidisplay.common.appapi.ControlChannelMessage;
import it.essar.pidisplay.common.appapi.CoreApplication;

public class ResetMessage implements ControlChannelMessage
{
	@Override
	public String getMessageAppID() {

		return CoreApplication.APPLICATION_ID;
		
	}
	
	@Override
	public String getMessageBody() {

		return null;
	
	}
	
	@Override
	public String getMessageType() {
		
		return CoreApplication.MessageTypes.RESET.name();
		
	}

}
