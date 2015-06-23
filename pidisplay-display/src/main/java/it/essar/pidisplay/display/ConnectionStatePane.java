package it.essar.pidisplay.display;

import it.essar.pidisplay.common.net.ConnectionState;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

class ConnectionStatePane extends FlowPane
{
	private static final Color COL_CONNECTED = new Color(0.0, 1.0, 0.0, 1.0);
	private static final Color COL_CONNECTING = new Color(0.0, 1.0, 0.0, 1.0);
	private static final Color COL_DISCONNECTED = new Color(1.0, 0.0, 0.0, 1.0);
	
	private ConnectionStateIcon icon;
	private Label lblCxnState;
	
	public ConnectionStatePane() {
		
		this(ConnectionState.DISCONNECTED);
		
	}
	
	public ConnectionStatePane(ConnectionState cxnState) {
		
		// Create sub components
		icon = new ConnectionStateIcon();
		lblCxnState = new Label();
		
		setId("cxn-state");
		setAlignment(Pos.CENTER_RIGHT);
		setHgap(3.0);
		
		// Add to panel
		getChildren().addAll(lblCxnState, icon);

		setConnectionState(cxnState);
				
	}
	
	public void setConnectionState(ConnectionState cxnState) {
		
		switch(cxnState) {

			case CONNECTING:
			
				icon.connecting();
				lblCxnState.setText("Connecting");
				lblCxnState.setTextFill(COL_CONNECTING);
				lblCxnState.getStyleClass().add("cxnstate-connecting");
				break;
				
			case CONNECTED:
				
				icon.connected();
				lblCxnState.setText("Connected");
				lblCxnState.setTextFill(COL_CONNECTED);
				lblCxnState.getStyleClass().add("cxnstate-connected");
				break;
				
			case DISCONNECTED:
				
				icon.disconnected();
				lblCxnState.setText("Disconnected");
				lblCxnState.setTextFill(COL_DISCONNECTED);
				lblCxnState.getStyleClass().add("cxnstate-disconnected");
				break;
		
		}
	}
	
	static class ConnectionStateIcon extends Circle
	{
		private static double radius = 6.0;
		
		private FadeTransition ftx;
		
		ConnectionStateIcon() {
			
			super(radius);
			ftx = null;
			
		}
		
		private void clearFader() {
			
			// Remove any transition
			if(ftx != null) {
				
				ftx.stop();
				
			}
		}
		
		private void setFader(long duration, long delay) {
			
			// Create transition if not already setup
			if(ftx == null) {
				
				ftx = new FadeTransition(Duration.millis(duration), this);
				ftx.setFromValue(1.0);
				ftx.setToValue(0.3);
				ftx.setCycleCount(Timeline.INDEFINITE);
				ftx.setAutoReverse(true);
				
			}
			
			// Stop if already running
			ftx.stop();
			
			// Set duration
			ftx.setDuration(Duration.millis(duration));
			ftx.setDelay(Duration.millis(delay));
			
			ftx.playFromStart();
			
		}
		
		private void connected() {

			// Set up transition - one second cycle and 3 second delay
			clearFader();
			setFill(COL_CONNECTED);

		}
		
		private void connecting() {
			
			// Set up transition - half second cycle and no delay
			setFill(COL_CONNECTING);
			setFader(500, 0);
						
		}
		
		private void disconnected() {
		
			clearFader();
			setFill(COL_DISCONNECTED);
			
		}
	}
}
