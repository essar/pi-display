package it.essar.pidisplay.apps.dashboard.display;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class DashboardApplicationPane extends StackPane
{
	private String prePath;
	
	private WebView webContent;
	private WebEngine engine;
	
	public DashboardApplicationPane(String clientID) {
		
		webContent = new WebView();
		engine = webContent.getEngine();
		
	}
	
	void init() {
		
		engine.load("http://localhost:8001/page/" + DashboardApplication.APP_ID + "/dashboard");
		engine.setUserAgent(getClass().getName());
		
		getChildren().add(webContent);
	
	}
	
	void loadPage(String path) {
		
		engine.load(prePath + "" + path);
		
	}
}
