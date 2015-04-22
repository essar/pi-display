package it.essar.pidisplay.display.fx;

import it.essar.pidisplay.apps.dashboard.display.DashboardApplicationPane;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

class BaseScene extends Scene
{
	
	private Pane appPane;
	
	final BorderPane root;
	final ConnectionStatePane cxnStatePane;
	
	protected BaseScene(BorderPane root) {
		
		super(root, 1920, 1080);
		getStylesheets().add("it/essar/pidisplay/display/fx/base.css");
		
		this.root = root;
		
		// Create components
		cxnStatePane = new ConnectionStatePane();

		// Load default application pane
		appPane = new DashboardApplicationPane(null);
		
		root.setBottom(cxnStatePane);
		root.setCenter(appPane);
		
	}
	
	public BaseScene() {
		
		this(new BorderPane());
		
	}
	
	void setApplicationPane(Pane appPane) {
		
		this.appPane = appPane;
		root.setCenter(appPane);
		
	}
}
