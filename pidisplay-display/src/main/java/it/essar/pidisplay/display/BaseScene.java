package it.essar.pidisplay.display;

import it.essar.pidisplay.apps.dashboard.display.DashboardApplicationPane;
import it.essar.pidisplay.common.appapi.Display;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

class BaseScene extends Scene implements Display
{
	
	private Pane appPane;
	
	final BorderPane root;
	final ConnectionStatePane cxnStatePane;
	final ErrorPane errPane;
	
	protected BaseScene(BorderPane root) {
		
		super(root, 1920, 1080);
		getStylesheets().add("it/essar/pidisplay/display/base.css");
		
		this.root = root;
		
		// Create components
		cxnStatePane = new ConnectionStatePane();
		errPane = new ErrorPane();

		// Load default application pane
		appPane = new DashboardApplicationPane(null);
		
		root.setBottom(cxnStatePane);
		root.setCenter(appPane);
		
	}
	
	public BaseScene() {
		
		this(new BorderPane());
		
	}
	
	void handleException(String message, Throwable t) {
		
		Platform.runLater(new Runnable() {
		
			@Override
			public void run() {

				// Set the error details
				errPane.setException(message, t);
				
				// Activate the error pane
				root.setCenter(errPane);
				
			}
		});
	}
	
	@Override
	public void setApplicationPane(Pane appPane) {
		
		if(appPane != null) {
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					
					BaseScene.this.appPane = appPane;
					root.setCenter(appPane);
					
				}
			});
		}
	}
}
