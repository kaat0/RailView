package railview.simulation;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.simulation.events.EventListener;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Duration;
import railview.controller.framework.AbstractSimulationController;
import railview.simulation.ui.GraphPaneController;
import railview.infrastructure.container.NetworkPaneController;

public class SimulationController extends AbstractSimulationController {
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
	private AnchorPane symbolPane;
	
	@FXML
	protected Button startButton;

	@FXML
	protected Button pauseButton;

	@FXML
	protected Button stopButton;
	
	@FXML
	private Button graphButton;

	@FXML
	private Button swarmButton;
	
	@FXML
	private Button lockButton;
	
	@FXML
	private Button unlockButton;
	
	@FXML
	private Button openFile;
	
	@FXML
	private Slider speedBar;

	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();
			
			FXMLLoader graphpaneloader = new FXMLLoader();
			URL graphpanelocation = GraphPaneController.class
					.getResource("GraphPane.fxml");
			graphpaneloader.setLocation(graphpanelocation);
			graphPane = (AnchorPane) graphpaneloader.load();
			
			this.networkPaneRoot.getChildren().addAll(networkPane, graphPane);
			
			networkPaneRoot.widthProperty().addListener(new ChangeListener<Number>() {
			    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
			    	networkPane.setLayoutX((newSceneWidth.doubleValue() / 2)- (networkPane.prefWidth(-1) / 2));
			    }
			});

			networkPaneRoot.heightProperty().addListener(new ChangeListener<Number>() {
			    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
			    	networkPane.setLayoutY((newSceneHeight.doubleValue() / 2)- (networkPane.prefHeight(-1) / 2));
			    }
			});
			
			graphPane.setVisible(false);
			symbolPane.setOpacity(0.0);
			openFile();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void startSimulation() {
		super.startSimulation();

		this.startButton.setDisable(true);
		this.pauseButton.setDisable(false);
		this.stopButton.setDisable(false);
	}

	@FXML
	public void pauseSimulation() {
		super.pauseSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(false);
	}

	@FXML
	public void stopSimulation() {
		super.stopSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(true);
	}
	@FXML
	public void lock(){
		
		menuPane.setOnMouseEntered(new EventHandler<MouseEvent>(){

            public void handle(MouseEvent event)
            {
            	menuPane.setOpacity(1.0);
            }
		});
		menuPane.setOnMouseExited(new EventHandler<MouseEvent>(){

            public void handle(MouseEvent event)
            {
            	menuPane.setOpacity(1.0);
            }
		});
		lockButton.setVisible(false);
		unlockButton.setVisible(true);
	}
	
	@FXML
	public void unlock(){
		
			menuPane.setOnMouseEntered(new EventHandler<MouseEvent>(){

	            public void handle(MouseEvent event)
	            {
			FadeTransition fadeTransition1 = new FadeTransition(
					javafx.util.Duration.millis(500), menuPane);
			fadeTransition1.setFromValue(0);
			fadeTransition1.setToValue(1.0);
			fadeTransition1.play();
	            }
			});
			
			menuPane.setOnMouseExited(new EventHandler<MouseEvent>(){

	            public void handle(MouseEvent event)
	            {
	    			FadeTransition fadeTransition2 = new FadeTransition(
	    					javafx.util.Duration.millis(500), menuPane);
	    			fadeTransition2.setFromValue(1.0);
	    			fadeTransition2.setToValue(0.0);
	    			fadeTransition2.play();
	            }
			});
			
			unlockButton.setVisible(false);
			lockButton.setVisible(true);
			
			
}
	
	@FXML
	private void appearSymbolPane() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		fadeTransition.play();
	}

	@FXML
	private void fadeSymbolPane() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();

	}

	@FXML
	public void graphButtonClicked() {
		graphPane.setVisible(true);
		networkPane.setVisible(false);
	}

	@FXML
	public void swarmButtonClicked() {
		graphPane.setVisible(false);
		networkPane.setVisible(true);
	}

	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}

	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		this.networkPaneController
			.setInfrastructureServiceUtility(serviceUtility);
	}

	@Override
	protected void updateUI() {
		networkPaneController.updateTrainCoordinates(
			simulator.getTrainCoordinates(this.updateTime), this.updateTime);
		updateStatusBar();
	}

	private void updateStatusBar() {
		timeLabel.setText("Simulation Time: " + this.updateTime.toString());

		int numActive = 0;
		int numTerminate = 0;

		if (simulator.getStatus() != SimulationManager.INACTIVE) {
			for (EventListener listener : simulator.getListeners()) {
				if (listener instanceof AbstractTrainSimulator) {
					AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;

					if (trainSimulator.getTerminateTime() != null) {
						if (trainSimulator.getTerminateTime().compareTo(this.updateTime) < 0) {
							numTerminate++;
						} else {
							if (trainSimulator.getActiveTime().compareTo(this.updateTime) < 0) {
								numActive++;
							}
						}
					} else {
						if (trainSimulator.getActiveTime() != null &&
							trainSimulator.getActiveTime().compareTo(this.updateTime) < 0) {
							numActive++;
						}
					}
				}
			}
		} // if (simulator.getStatus() != SimulationManager.INACTIVE)

		activeLabel.setText("Active Trains: " + numActive);
		terminatedLabel.setText("Terminated Trains: " + numTerminate);
	}

	@Override
	protected void setTime(boolean isReplay) {
		Duration updateInterval = Duration.fromTotalMilliSecond(UIPause);
		if (simulator.getStatus() != SimulationManager.INACTIVE) {
			updateInterval = Duration.fromTotalMilliSecond(MAXSpeed * speedBar.getValue()/100);
			if (speedBar.getValue() == speedBar.getMin()) {
				updateInterval = Duration.fromTotalMilliSecond(UIPause);
			}

			if (speedBar.getValue() == speedBar.getMax() &&
				simulator.getTime() != null &&
				simulator.getStatus() != SimulationManager.TERMINATED) {

				updateInterval = simulator.getTime().getDifference(updateTime);
			}
		}
		if (isReplay) {
			this.updateTime = this.updateTime.add(updateInterval);
		} else {
			if (simulator.getStatus() == SimulationManager.RUNNING) { // not terminated yet
				this.updateTime = this.updateTime.add(updateInterval);
				if (this.updateTime.compareTo(simulator.getTime()) > 0) {
					this.updateTime = simulator.getTime(); // if update too fast, slow down
				}
			} else {
				this.updateTime = this.updateTime.add(updateInterval);
			}
		}
	}
	
	public void openFile() {
	openFile.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    File file = fileChooser.showOpenDialog(networkPaneRoot.getScene().getWindow());
                    if (file != null) {
                        openFile(file);
                    }
                }
            });
	}

    private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                SimulationController.class.getName()).log(
                    Level.SEVERE, null, ex
                );
        }
    }
    
    final FileChooser fileChooser = new FileChooser();
	private Desktop desktop = Desktop.getDesktop();
	private StackPane networkPane;
	private AnchorPane graphPane;
	private NetworkPaneController networkPaneController;
	private Duration updateInterval = Duration.fromSecond(60);
	private int UIPause = 100;
	private int MAXSpeed = 20000; // 1 : 200
}
