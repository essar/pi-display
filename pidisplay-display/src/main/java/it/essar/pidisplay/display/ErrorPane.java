package it.essar.pidisplay.display;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ErrorPane extends StackPane
{
	
	private Label lblMessage;
	
	public ErrorPane() {
		
		// Create sub components
		lblMessage = new Label();
				
		setId("err-msg");
		setAlignment(Pos.CENTER);
				
		// Add to panel
		getChildren().addAll(lblMessage);
		
	}
	
	void setException(String message, Throwable t) {
		
		lblMessage.setText(message);
	
	}
}	
