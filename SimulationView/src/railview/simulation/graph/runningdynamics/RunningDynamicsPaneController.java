package railview.simulation.graph.runningdynamics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.runingdynamics.Course;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Energy;
import railview.simulation.graph.trainrunmonitor.TrainRunMonitorPaneController;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.utilities.DraggableChart;
import railview.simulation.ui.utilities.ZoomOnlyX;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

/**
 * The controller class for RunningDynamicsPane.fxml. The Pane gives a
 * selectable list of trains from the simulation where you can see their
 * speedProfileChart and energyChart.
 *
 */

public class RunningDynamicsPaneController {
	@FXML
	private AnchorPane speedprofilePane, energyPane;

	@FXML
	private ListView<String> trainNumbers;

	@FXML
	private TableView<TableProperty> trainInfoTable;

	@SuppressWarnings("unchecked")
	@FXML
	public void initialize() {
		speedProfileChart = this.createChart();
		energyChart = this.createChart();

		trainNumbers.setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	            String newValue = trainNumbers.getSelectionModel().getSelectedItem();
	            AbstractTrainSimulator train = trainMap.get(newValue);

				trainInfoTable.setItems(TrainRunMonitorPaneController.generateTrainInfo(train, newValue));

				speedProfileChart.getData().clear();
				speedProfileChart.getXAxis().setAutoRanging(true);
				speedProfileChart.getYAxis().setAutoRanging(true);
				drawVelocity(trainMap.get(newValue), speedProfileChart);

				energyChart.getData().clear();
				energyChart.getXAxis().setAutoRanging(true);
				energyChart.getYAxis().setAutoRanging(true);
				drawEnergy(trainMap.get(newValue));
	        }
	    });

		// initialize trainInfoTable
		TableColumn<TableProperty, String> trainItemCol = new TableColumn<TableProperty, String>(
				"Item");
		trainItemCol.setMinWidth(100);
		trainItemCol
				.setCellValueFactory(new PropertyValueFactory<TableProperty, String>(
						"item"));

		TableColumn<TableProperty, String> trainValueCol = new TableColumn<TableProperty, String>(
				"Value");
		trainValueCol.setMinWidth(100);
		trainValueCol
				.setCellValueFactory(new PropertyValueFactory<TableProperty, String>(
						"value"));

		trainInfoTable.getColumns().addAll(trainItemCol, trainValueCol);

		trainValueCol.setCellFactory(TrainRunMonitorPaneController.createCellFactory());

		speedprofilePane.getChildren().add(speedProfileChart);
		energyPane.getChildren().add(energyChart);

		new ZoomOnlyX(speedProfileChart, speedprofilePane);
		new ZoomOnlyX(energyChart, energyPane);

		initializeChart(speedProfileChart);
		initializeChart(energyChart);
	}

	private void initializeChart(DraggableChart<Number, Number> chart) {
		AnchorPane.setTopAnchor(chart, 0.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		AnchorPane.setBottomAnchor(chart, 0.0);

		chart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		chart.startEventHandlers();
	}

	@FXML
	private void resetSpeedProfile(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				speedProfileChart.getXAxis().setAutoRanging(true);
				speedProfileChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	public void setTrainMap(ConcurrentHashMap<String, AbstractTrainSimulator> trainMap) {
		this.trainMap = trainMap;
	}

	public void setTrainNumbers(ObservableList<String> numbers) {
		this.trainNumbers.setItems(numbers);
	}

	private DraggableChart<Number, Number> createChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		DraggableChart<Number, Number> chart = new DraggableChart<>(xAxis,
				yAxis);
		chart.setAnimated(false);
		chart.setCreateSymbols(false);

		return chart;
	}

	private LineChart<Number, Number> drawVelocity(
			AbstractTrainSimulator train, LineChart<Number, Number> chart) {
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		CourseForVelocitySeries.setName("speed profile (km/h)");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (Map.Entry<Double, Double> entry : getCourseForVelocity(train)
					.entrySet()) {
				CourseForVelocitySeries.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
			}
			chart.getData().add(CourseForVelocitySeries);

			XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();
			double y = -1;
			speedLimitSeries.getData().add(new Data<Number, Number>(0, y));
			for (Map.Entry<Double, Double> entry : Course.getSpeedLimits(train).entrySet()) {
				if (y >= 0) {
					speedLimitSeries.getData().add(
							new Data<Number, Number>(entry.getKey(), y));
				}
				speedLimitSeries.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
				y = entry.getValue();
			}
			chart.getData().add(speedLimitSeries);
			speedLimitSeries
					.nodeProperty()
					.get()
					.setStyle(
							"-fx-stroke: #000000; -fx-stroke-dash-array: 0.1 5.0;");

			chart.setLegendVisible(false);

			chart.setCreateSymbols(false);
		}
		return chart;
	}

	// Map: Meter, VelocityInKmH
	public static Map<Double, Double> getCourseForVelocity(AbstractTrainSimulator train) {
		Map<Double, Double> velocityMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double velocityInKmH = 0; // y
		for (DiscretePoint point : train.getWholeCoursePoints()) {
			velocityInKmH = point.getVelocity().getKilometerPerHour();
			meter += point.getDistance().getMeter();
			velocityMap.put(meter, velocityInKmH);
		}
		return velocityMap;
	}



	private void drawEnergy(AbstractTrainSimulator train) {
		XYChart.Series<Number, Number> courseForEnergy = new Series<Number, Number>();
		courseForEnergy.setName("energy consumption (KWH)");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (Map.Entry<Double, Double> entry : getCourseForEnergy(train)
					.entrySet()) {
				courseForEnergy.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
			}
			energyChart.getData().add(courseForEnergy);
			energyChart.setCreateSymbols(false);
		}
	}

	// Map: Meter, VelocityInKmH
	private Map<Double, Double> getCourseForEnergy(AbstractTrainSimulator train) {
		Map<Double, Double> energyMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double accumlatedEnergy = 0; // y
		Energy[] energies = Course.calculateEnergy(
				train.getWholeCoursePoints(), train.getTrainDefinition());
		int index = 0;
		for (DiscretePoint point : train.getWholeCoursePoints()) {
			if (energies[index] != null) {
				accumlatedEnergy += energies[index].getKWH();
			}
			meter += point.getDistance().getMeter();
			energyMap.put(meter, accumlatedEnergy);

			index++;
		}

		return energyMap;
	}

	private DraggableChart<Number, Number> speedProfileChart;
	private DraggableChart<Number, Number> energyChart;
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap;
}
