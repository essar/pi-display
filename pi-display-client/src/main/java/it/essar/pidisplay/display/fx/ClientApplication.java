package it.essar.pidisplay.display.fx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ClientApplication extends Application
{
	
	@Override
	public void start(Stage primaryStage) {
		
		
		StackPane root = new StackPane();
		
		Scene scene = new Scene(root, 1980, 1080);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	public static void main(String[] args) {
		
		launch(args);
		
	}
}
