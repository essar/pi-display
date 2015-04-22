package it.essar.pidisplay.display.fx;

import it.essar.pidisplay.common.net.ConnectionState;
import it.essar.pidisplay.display.DisplayController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class ClientApplication extends Application
{
	
	private DisplayController ctl;
	
	private BaseScene base;
	
	public ClientApplication() {
		
		base = new BaseScene();
		
	}

	
	private void initController() {
		
		try {
			
			ctl = new DisplayController(this);
			
		} catch(Exception e) {
			
			e.printStackTrace(System.err);
			
		}
	}
	
	@Override
	public void init() throws Exception {
		
		super.init();
		
		initController();
		
	}
	
	public void setApplicationPane(Pane appPane) {
		
		if(base != null) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					base.setApplicationPane(appPane);
					
				}
			});
		}
	}
	
	public void setConnectionState(ConnectionState newState) {
		
		if(base != null) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					base.cxnStatePane.setConnectionState(newState);
				
				}
			});
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		
		primaryStage.setFullScreen(true);
		primaryStage.setScene(base);
		primaryStage.show();
		
	}
	
	@Override
	public void stop() throws Exception {

		ctl.stop();
		super.stop();
		
	}
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
}
