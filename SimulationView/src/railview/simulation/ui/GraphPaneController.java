package railview.simulation.ui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.logs.InfrastructureOccupancyAndPendingLogger;
import railapp.simulation.train.AbstractTrainSimulator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class GraphPaneController {

	@FXML
	private AnchorPane graphPaneRoot;
	
	@FXML
	private AnchorPane occupancyTabAnchorPane;
	
	@FXML
	private TabPane tabPane;

	@FXML
	private AnchorPane runningDynamicsRoot;
	
	@FXML
	private AnchorPane runningdynamicsPane;
	
	@FXML
	private AnchorPane trainRunMonitorRoot;
	
	@FXML
	private AnchorPane trainRunMonitorPane;
	
	@FXML
	private AnchorPane occupancyAndPendingRoot;
	
	private AnchorPane occupancyAndPendingPane;

	@FXML
	public void initialize() {
		tabPane.setSide(Side.BOTTOM);

		try {
			FXMLLoader runningDynamicsLoader = new FXMLLoader();
			URL location = RunningDynamicsPaneController.class
					.getResource("RunningDynamicsPane.fxml");
			runningDynamicsLoader.setLocation(location);
			this.runningdynamicsPane = (AnchorPane) runningDynamicsLoader.load();
			this.runningDynamicsPaneController = runningDynamicsLoader.getController();
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
			this.trainRunMonitorPane = (AnchorPane) trainRunMonitorLoader.load();
			this.trainRunMonitorController = trainRunMonitorLoader.getController();
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
			occupancyAndPendingPane = (AnchorPane) occupancyAndPendingLoader.load();
			this.occupancyAndPendingPaneController = occupancyAndPendingLoader.getController();
			
			this.occupancyAndPendingRoot.getChildren().add(occupancyAndPendingPane);
			

			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		occupancyTabAnchorPane.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	occupancyAndPendingRoot.setLayoutX((newSceneWidth.doubleValue() / 2)- (occupancyAndPendingRoot.prefWidth(-1) / 2));
		    }
		});

		occupancyTabAnchorPane.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	occupancyAndPendingRoot.setLayoutY((newSceneHeight.doubleValue() / 2)- (occupancyAndPendingRoot.prefHeight(-1) / 2));
		    }
		});
	}
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.trainRunMonitorController.setInfrastructureServiceUtility(infraServiceUtility);
		this.occupancyAndPendingPaneController.setInfrastructureServiceUtility(infraServiceUtility);
	}
	
	public void setInfrastructureOccupancyAndPendingLogger(InfrastructureOccupancyAndPendingLogger logger) {
		this.occupancyAndPendingPaneController.setLogger(logger);
	}

	public void updateTrainMap(List<AbstractTrainSimulator> trainList) {
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);
		for (AbstractTrainSimulator trainSimulator : tempList) {
			this.trainMap.put(trainSimulator.getTrain().getNumber(),
					trainSimulator);
			String trainNumber = trainSimulator.getTrain().getNumber();
			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE
					&& !numbers.contains(trainNumber)) {
				numbers.add(trainNumber);
			}
		}
		
		this.runningDynamicsPaneController.setTrainNumbers(numbers);
		this.trainRunMonitorController.setTrainNumbers(numbers);
	}

	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}
	
	private RunningDynamicsPaneController runningDynamicsPaneController;
	
	private TrainRunMonitorPaneController trainRunMonitorController;
	
	private OccupancyAndPendingPaneController occupancyAndPendingPaneController;

	private ObservableList<String> numbers = FXCollections.observableArrayList();
	
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap = 
			new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
