package railview.simulation.graph.trainrunmonitor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railview.simulation.setting.UIInfrastructureSetting;
import railview.simulation.ui.components.ChartTripBlockingTime;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.DraggableChart;
import railview.simulation.ui.utilities.Zoom;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

public class TripMonitorPaneController {
	@FXML
	private AnchorPane blockingTimePane, headwayPane, snapshotRoot;

	@FXML
	private Label eventLabel;

	@FXML
	private TableView<TableProperty> eventTable;

	private ChartTripBlockingTime<Number, Number> tripChart;
	private DraggableChart<Number, Number> headwayChart;

	private StackPane snapshotPane;
	private SnapshotPaneController snapshotPaneController;

	private AbstractTrainSimulator train;

	@FXML
	public void initialize() {
		this.initializeSnapshot();

		this.initializeBlockingTime();

		this.initializeEventTable();
	}

	private void initializeBlockingTime() {
		tripChart = ChartTripBlockingTime.createBlockingTimeForTripChart(eventLabel, eventTable, this);

		blockingTimePane.getChildren().add(tripChart);
		new Zoom(tripChart, blockingTimePane);

		this.headwayChart = new DraggableChart<>(new NumberAxis(), new NumberAxis());
		this.headwayChart.setAnimated(false);
		this.headwayChart.setCreateSymbols(false);
		headwayPane.getChildren().add(headwayChart);
		new Zoom(headwayChart, headwayPane);

		AnchorPane.setTopAnchor(headwayChart, 0.0);
		AnchorPane.setLeftAnchor(headwayChart, 0.0);
		AnchorPane.setRightAnchor(headwayChart, 0.0);
		AnchorPane.setBottomAnchor(headwayChart, 0.0);

		blockingTimePane.widthProperty().addListener(
			(obs, oldVal, newVal) -> {
				tripChart.getData().clear();
				tripChart.getBlockingTimeChartPlotChildren().clear();
		});

		blockingTimePane.heightProperty().addListener(
			(obs, oldVal, newVal) -> {
				blockingTimePane.setLayoutY((newVal.doubleValue() / 2)
						- (blockingTimePane.prefHeight(-1) / 2));

				tripChart.getData().clear();
				tripChart.getBlockingTimeChartPlotChildren().clear();
		});

		eventLabel.toFront();
	}

	@SuppressWarnings("unchecked")
	private void initializeEventTable() {
		TableColumn<TableProperty, String> eventItemCol = new TableColumn<TableProperty, String>("Item");
		eventItemCol.setMinWidth(100);
		eventItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn<TableProperty, String> eventValueCol = new TableColumn<TableProperty, String>("Value");
		eventValueCol.setMinWidth(100);
		eventValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		eventTable.getColumns().addAll(eventItemCol, eventValueCol);
		eventValueCol.setCellFactory(TrainRunMonitorPaneController.createCellFactory());
	}

	private void initializeSnapshot() {
		try {
			FXMLLoader snapshotPaneLoader = new FXMLLoader();
			URL location = SnapshotPaneController.class
					.getResource("SnapshotPane.fxml");
			snapshotPaneLoader.setLocation(location);
			snapshotPane = (StackPane) snapshotPaneLoader.load();
			this.snapshotPaneController = snapshotPaneLoader.getController();

			this.snapshotRoot.getChildren().add(snapshotPane);
		} catch (IOException e) {
			e.printStackTrace();
		}

		snapshotRoot.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldSceneWidth, Number newSceneWidth) {
				snapshotPane.setLayoutX((newSceneWidth.doubleValue() / 2)
						- (snapshotPane.prefWidth(-1) / 2));
			}
		});
	}

	/**
	 * reset the zoom of the blockingTimeChart
	 *
	 * @param event
	 */
	@FXML
	private void resetZoomBlockingTime(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				tripChart.setChartBound();
			}
		}
	}

	@FXML
	private void resetHeadway(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				headwayChart.getXAxis().setAutoRanging(true);
				headwayChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.snapshotPaneController.setInfrastructureServiceUtility(infraServiceUtility);
	}

	public void setUIInfraSetting(UIInfrastructureSetting uiInfraSetting) {
		this.snapshotPaneController.setUIInfraSetting(uiInfraSetting);
	}

	/**
	 * draw the event points on the path in the snapshotPane
	 *
	 * @param td
	 */
	public void drawEventOnSnap(TimeDistance td) {
		Coordinate coordinate = train.getFullPath().getCoordinate(
				Length.fromMeter(td.getDistance()));
		snapshotPaneController.setEventPoint(coordinate);
		snapshotPaneController.draw();
	}

	public void updateUI(AbstractTrainSimulator train,
			List<Coordinate> path,
			List<BlockingTime> blockingTime,
			List<TimeDistance> timeDistances,
			Map<TimeDistance, List<EventData>> events) {

		this.train = train;

		snapshotPaneController.setHighlightedPath(path);
		snapshotPaneController.setEventPoint(null);
		snapshotPaneController.draw();

		this.headwayChart.getData().clear();
		this.headwayChart.getXAxis().setAutoRanging(true);
		this.headwayChart.getYAxis().setAutoRanging(true);
		drawHeadway(blockingTime);

		Time startTime = train.getTripSection().getStartTime();

		((NumberAxis) tripChart.getYAxis()).setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number t) {
				Time testTime = startTime.add(Duration.fromTotalSecond(-t.doubleValue()));
				return testTime.toString();
			}

			@Override
			public Number fromString(String string) {
				return 1;
			}
		});

		tripChart.getData().clear();
		tripChart.getBlockingTimeChartPlotChildren().clear();

		tripChart.setBlockingTime(blockingTime);
		tripChart.setTimeDistances(timeDistances);
		tripChart.setEventsMap(events);

		tripChart.setAnimated(false);
		tripChart.setCreateSymbols(false);
	}

	private LineChart<Number, Number> drawHeadway(List<BlockingTime> blockingTime) {
		XYChart.Series<Number, Number> headwaySeries = new Series<Number, Number>();
		headwaySeries.setName("Headway (seconds)");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (BlockingTime entry : blockingTime) {
				headwaySeries.getData().add(new Data<Number, Number>(
					entry.getRelativeStartDistance(), entry.getDurationInSecond()));
				headwaySeries.getData().add(new Data<Number, Number>(
						entry.getRelativeEndDistance(), entry.getDurationInSecond()));
			}
			this.headwayChart.getData().add(headwaySeries);

			this.headwayChart.setLegendVisible(false);

			this.headwayChart.setCreateSymbols(false);
		}
		return this.headwayChart;
	}
}
