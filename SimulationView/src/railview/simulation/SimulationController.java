package railview.simulation;

import java.io.IOException;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.simulation.events.EventListener;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Duration;
import railapp.units.Time;
import railview.infrastructure.container.NetworkPaneController;

public class SimulationController {
	@FXML
	private AnchorPane networkPaneRoot;
	
	@FXML
	private Label timeLabel;
	
	@FXML
	private Label activeLabel;
	
	@FXML
	private Label terminatedLabel;
	
	@FXML
	private AnchorPane menuPane;
	
	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			StackPane networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();
			AnchorPane.setLeftAnchor(networkPane, (this.networkPaneRoot.prefWidth(-1)/2)-(networkPane.prefWidth(-1)/2));
			AnchorPane.setTopAnchor(networkPane,(this.networkPaneRoot.prefHeight(-1)/2)-(networkPane.prefHeight(-1)/2));
			this.networkPaneRoot.getChildren().add(networkPane);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void startSimulation() {
		if (this.simulator != null) {
			new Thread(this.simulator).start();
			
			Thread t = new Thread(() -> {
				(new SimulationUpdater()).periodicalUpdate(false); 
			});			
			t.setDaemon(true);
			t.start();
		}
	}
	
	@FXML
	public void replaySimulation() {
		if (this.simulator != null) {
			Thread t = new Thread(() -> {
				(new SimulationUpdater()).periodicalUpdate(true); 
			});			
			t.setDaemon(true);
			t.start();
		}
	}
	
	@FXML
	private void fadeRoot() {
	

	                FadeTransition fadeTransition 
	                        = new FadeTransition(javafx.util.Duration.millis(500), menuPane);
	                fadeTransition.setToValue(0.0);
	                fadeTransition.play();
	            }

		  
	@FXML
	private void appear() {
	                FadeTransition fadeTransition 
	                        = new FadeTransition(javafx.util.Duration.millis(500), menuPane);
	                fadeTransition.setFromValue(0.0);
	                fadeTransition.setToValue(1.0);
	                fadeTransition.play();
	            }

  		
	@FXML
	private void fadeMenu() {
		
	                FadeTransition fadeTransition 
	                        = new FadeTransition(javafx.util.Duration.millis(500), menuPane);
	                fadeTransition.setFromValue(1.0);
	                fadeTransition.setToValue(0.0);
	                fadeTransition.play();

				}
	
	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}
	
	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		this.networkPaneController
				.setInfrastructureServiceUtility(serviceUtility);
	}
	
	public void setSimulationManager(SimulationManager simulator) {
		this.simulator = simulator;
	}
	
	private NetworkPaneController networkPaneController;
	private SimulationManager simulator;
	private Duration updateInterval = Duration.fromSecond(60);
	private int UIPause = 100;
	
	class SimulationUpdater {
		private Time time = Time.getInstance(0, 0, 0);
		boolean isUpdateCompleted = false;

		void periodicalUpdate(boolean isReplay) {
			while (! isUpdateCompleted) {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						timeLabel.setText("Simulation Time: " + time.toString());
						
						networkPaneController.updateTrainCoordinates(
								simulator.getTrainCoordinates(time), time);
						
						if (simulator.getStatus() != SimulationManager.INACTIVE) { // initialization is finished
							int numActive = 0;
							int numTerminate = 0;
							for (EventListener listener : simulator.getListeners()) {
								if (listener instanceof AbstractTrainSimulator) {
									AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;
									
									if (trainSimulator.getTerminateTime() != null) {
										if (trainSimulator.getTerminateTime().compareTo(time) < 0) {
											numTerminate++;
										} else {
											if (trainSimulator.getActiveTime().compareTo(time) < 0) {
												numActive++;
											}
										}
									} else {
										if (trainSimulator.getActiveTime() != null && 
												trainSimulator.getActiveTime().compareTo(time) < 0) {
											numActive++;
										}
									}
								}
							}
						
							activeLabel.setText("Active Trains: " + numActive);
							terminatedLabel.setText("Terminated Trains: " + numTerminate);
						} // if (simulator.getTime() != null)
						
						if (simulator.getStatus() == SimulationManager.TERMINATED &&
								time.compareTo(simulator.getTime()) >= 0) {
							isUpdateCompleted = true;
						}
					}
				});
				
				try {
					Thread.sleep(UIPause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (isReplay) {
					time = time.add(updateInterval);
				} else {
					if (simulator.getStatus() == SimulationManager.RUNNING) { // not terminated yet
						time = time.add(updateInterval);
						if (time.compareTo(simulator.getTime()) > 0) {
							time = simulator.getTime(); // if update too fast, slow down
						}
					} else {
						time = time.add(updateInterval);
					}
				}
			}
		} // periodicalUpdate(boolean isReplay)
	} // class SimulationUpdater
}
