package railview.simulation.graph;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.logs.InfrastructureOccupancyAndPendingLogger;
import railapp.simulation.train.AbstractTrainSimulator;
import railview.simulation.graph.occupancypending.OccupancyAndPendingPaneController;
import railview.simulation.graph.runningdynamics.RunningDynamicsPaneController;
import railview.simulation.graph.trainrunmonitor.TrainRunMonitorPaneController;
import railview.simulation.setting.UIInfrastructureSetting;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

/**
 * The controller class for GraphPane.fxml. The Pane shows different kind of
 * data for the simulation.
 *
 */
public class GraphPaneController {

	@FXML
	private AnchorPane graphPaneRoot, occupancyTabAnchorPane,
			runningDynamicsRoot, runningdynamicsPane, trainRunMonitorRoot,
			trainRunMonitorPane, occupancyAndPendingRoot,
			occupancyAndPendingPane;

	@FXML
	private TabPane tabPane;

	@FXML
	private Button forwardButton, backButton;

	@FXML
	public void initialize() {
		tabPane.setSide(Side.BOTTOM);
		forwardButton.setStyle("-fx-background-radius: 50px; "
				+ "-fx-min-width: 50px; " + "-fx-min-height: 50px; "
				+ "-fx-max-width: 50px; " + "-fx-max-height: 50px;"
				+ "-fx-background-color: #B2B2B2; ");

		backButton.setStyle("-fx-background-radius: 50px; "
				+ "-fx-min-width: 50px; " + "-fx-min-height: 50px; "
				+ "-fx-max-width: 50px; " + "-fx-max-height: 50px;"
				+ "-fx-background-color: #B2B2B2; ");

		try {
			FXMLLoader runningDynamicsLoader = new FXMLLoader();
			URL location = RunningDynamicsPaneController.class
					.getResource("RunningDynamicsPane.fxml");
			runningDynamicsLoader.setLocation(location);
			this.runningdynamicsPane = (AnchorPane) runningDynamicsLoader
					.load();
			this.runningDynamicsPaneController = runningDynamicsLoader
					.getController();
			this.runningDynamicsPaneController.setTrainMap(this.trainMap);

			this.runningDynamicsRoot.getChildren().add(runningdynamicsPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FXMLLoader trainRunMonitorLoader = new FXMLLoader();
			URL location = TrainRunMonitorPaneController.class
					.getResource("TrainRunMonitorPane.fxml");
			trainRunMonitorLoader.setLocation(location);
			this.trainRunMonitorPane = (AnchorPane) trainRunMonitorLoader
					.load();
			this.trainRunMonitorController = trainRunMonitorLoader
					.getController();
			this.trainRunMonitorController.setTrainMap(this.trainMap);

			this.trainRunMonitorRoot.getChildren().add(trainRunMonitorPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FXMLLoader occupancyAndPendingLoader = new FXMLLoader();
			URL location = OccupancyAndPendingPaneController.class
					.getResource("OccupancyAndPendingPane.fxml");
			occupancyAndPendingLoader.setLocation(location);
			occupancyAndPendingPane = (AnchorPane) occupancyAndPendingLoader
					.load();
			this.occupancyAndPendingPaneController = occupancyAndPendingLoader
					.getController();

			this.occupancyAndPendingRoot.getChildren().add(
					occupancyAndPendingPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

		tabPane.getSelectionModel().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> ov,
							Number oldValue, Number newValue) {
						if (isActive && newValue.intValue() == 2) {
							occupancyAndPendingPaneController.setActive(true);
						} else {
							occupancyAndPendingPaneController.setActive(false);
						}
					}
				});

		occupancyTabAnchorPane.widthProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observableValue,
							Number oldSceneWidth, Number newSceneWidth) {
						occupancyAndPendingRoot.setLayoutX((newSceneWidth
								.doubleValue() / 2)
								- (occupancyAndPendingRoot.prefWidth(-1) / 2));
					}
				});

		occupancyTabAnchorPane.heightProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observableValue,
							Number oldSceneHeight, Number newSceneHeight) {
						occupancyAndPendingRoot.setLayoutY((newSceneHeight
								.doubleValue() / 2)
								- (occupancyAndPendingRoot.prefHeight(-1) / 2));
					}
				});
	}

	public void setUIInfraSetting(UIInfrastructureSetting uiInfraSetting) {
		this.trainRunMonitorController.setUIInfraSetting(uiInfraSetting);
		this.occupancyAndPendingPaneController.setUIInfraSetting(uiInfraSetting);
	}

	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility infraServiceUtility) {
		this.trainRunMonitorController
				.setInfrastructureServiceUtility(infraServiceUtility);
		this.occupancyAndPendingPaneController
				.setInfrastructureServiceUtility(infraServiceUtility);
	}

	public void setInfrastructureOccupancyAndPendingLogger(
			InfrastructureOccupancyAndPendingLogger logger) {
		this.occupancyAndPendingPaneController.setLogger(logger);
	}

	public void updateTrainMap(List<AbstractTrainSimulator> trainList) {
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);
		for (AbstractTrainSimulator trainSimulator : tempList) {
			if (trainSimulator.getTrain().getStatus() == SimpleTrain.INACTIVE) {
				continue;
			}

			String trainNumber = trainSimulator.getTrain().getDefinition()
					.getTrainClass().getClassName()
					+ " " + trainSimulator.getTrain().getNumber();

			this.trainMap.put(trainNumber, trainSimulator);

			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE
					&& !numbers.contains(trainNumber)) {
				numbers.add(trainNumber);
			}
		}

		this.trainRunMonitorController.setTrainNumbers(numbers);
		this.runningDynamicsPaneController.setTrainNumbers(numbers);
	}

	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}

	public void setActive(boolean active) {
		this.isActive = active;

		if (!active) {
			this.occupancyAndPendingPaneController.setActive(false);
		}
	}

	@FXML
	private void navigate() {
		this.occupancyAndPendingPaneController.navigate();
	}

	@FXML
	private void onSelectionChanged() {

	}

	@FXML
	private void onForwardButtonEntered() {
		forwardButton.setStyle("-fx-background-radius: 50px; "
				+ "-fx-min-width: 50px; " + "-fx-min-height: 50px; "
				+ "-fx-max-width: 50px; " + "-fx-max-height: 50px;"
				+ "-fx-background-color: #EBEBEB; ");
	}

	@FXML
	private void onForwardButtonExited() {
		forwardButton.setStyle("-fx-background-radius: 50px; "
				+ "-fx-min-width: 50px; " + "-fx-min-height: 50px; "
				+ "-fx-max-width: 50px; " + "-fx-max-height: 50px;"
				+ "-fx-background-color: #B2B2B2; ");
	}

	@FXML
	private void onBackButtonEntered() {
		backButton.setStyle("-fx-background-radius: 50px; "
				+ "-fx-min-width: 50px; " + "-fx-min-height: 50px; "
				+ "-fx-max-width: 50px; " + "-fx-max-height: 50px;"
				+ "-fx-background-color: #EBEBEB; ");
	}

	@FXML
	private void onBackButtonExited() {
		backButton.setStyle("-fx-background-radius: 50px; "
				+ "-fx-min-width: 50px; " + "-fx-min-height: 50px; "
				+ "-fx-max-width: 50px; " + "-fx-max-height: 50px;"
				+ "-fx-background-color: #B2B2B2; ");
	}

	private boolean isActive = false;

	private RunningDynamicsPaneController runningDynamicsPaneController;

	private TrainRunMonitorPaneController trainRunMonitorController;

	private OccupancyAndPendingPaneController occupancyAndPendingPaneController;

	private ObservableList<String> numbers = FXCollections.observableArrayList();

	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap = new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
