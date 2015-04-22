package it.essar.pidisplay.common.proc;

public interface MonitorableProcess
{
	
	public String getProcessName();
	
	boolean isProcessRunning();
	
	boolean startProcess();
	
	boolean stopProcess();

}
