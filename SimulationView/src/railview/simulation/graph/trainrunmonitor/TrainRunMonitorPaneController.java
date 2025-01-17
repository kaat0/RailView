package railview.simulation.graph.trainrunmonitor;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railview.simulation.setting.UIInfrastructureSetting;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.LineData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.data.TrainRunDataManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * The controller class for TrainRunMonitorPane.fxml. The Pane gives a
 * selectable list of trains where you can see their blockingTimeChart.
 *
 */
public class TrainRunMonitorPaneController {
	@FXML
	private AnchorPane blockingTimePane, snapshotRoot, tripRoot, lineRoot, lineMonitorPane;

	@FXML
	private TabPane tripLineTab;

	@FXML
	private SplitPane tripMonitorPane;

	@FXML
	private ListView<String> trainNumbers, lineListView, stationListView;

	@FXML
	private TableView<TableProperty> trainInfoTable;

	@FXML
	private CheckBox selfEventCheckBox, inEventCheckBox, outEventCheckBox;

	private TripMonitorPaneController tripMonitorPaneController;
	private LineMonitorPaneController lineMonitorPaneController;
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap;
	private IInfrastructureServiceUtility infrastructureServiceUtility;
	private static HashMap<String, Line> lineMap = new HashMap<String, Line>();
	private TrainRunDataManager trainRunDataManager = new TrainRunDataManager();

	/**
	 * initialize the trainRunMonitorPane, add blockingTimeChart on top of it,
	 * add zoom function, load snapshotPane, add window resize listener, create
	 * eventTable and trainInfoTable
	 */
	@FXML
	public void initialize() {
		this.initTripMonitor();
		this.initLineMonitor();

		this.tripLineTab.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
		    @Override
		    public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
		    	if (newValue.intValue() == 0) {
		        	lineRoot.setVisible(false);
					tripRoot.setVisible(true);
		        } else if (newValue.intValue() == 1) {
		        	lineRoot.setVisible(true);
					tripRoot.setVisible(false);
		        }
		    }
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initTripMonitor() {
		try {
			FXMLLoader tripMonitorPaneLoader = new FXMLLoader();
			URL location = TripMonitorPaneController.class.getResource("TripMonitorPane.fxml");
			tripMonitorPaneLoader.setLocation(location);
			tripMonitorPane = (SplitPane) tripMonitorPaneLoader.load();
			this.tripMonitorPaneController = tripMonitorPaneLoader.getController();

			this.tripRoot.getChildren().add(tripMonitorPane);
		} catch (IOException e) {
			e.printStackTrace();
		}

		trainNumbers.setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	            String newValue = trainNumbers.getSelectionModel().getSelectedItem();
	            AbstractTrainSimulator train = trainMap.get(newValue);

				trainInfoTable.setItems(generateTrainInfo(train, newValue));

				List<Coordinate> path = trainRunDataManager.getTrainPathCoordinates(train);

				List<BlockingTime> blockingTime = trainRunDataManager.getBlockingTimeStairway(train, null);

				Map<TimeDistance, List<EventData>> events = trainRunDataManager.getEvents(train);

				List<TimeDistance> timeDistances = trainRunDataManager.getTimeInDistance(train, null);

				tripMonitorPaneController.updateUI(train, path, blockingTime, timeDistances, events);
	        }
	    });

		// initialize trainInfoTable
		TableColumn trainItemCol = new TableColumn("Item");
		trainItemCol.setMinWidth(100);
		trainItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn trainValueCol = new TableColumn("Value");
		trainValueCol.setMinWidth(100);
		trainValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		trainInfoTable.getColumns().addAll(trainItemCol, trainValueCol);

		trainValueCol.setCellFactory(createCellFactory());
	}

	private void initLineMonitor() {
		try {
			FXMLLoader lineMonitorPaneLoader = new FXMLLoader();
			URL location = LineMonitorPaneController.class.getResource("LineMonitorPane.fxml");
			lineMonitorPaneLoader.setLocation(location);
			lineMonitorPane = (AnchorPane) lineMonitorPaneLoader.load();
			this.lineMonitorPaneController = lineMonitorPaneLoader.getController();

			this.lineRoot.getChildren().add(lineMonitorPane);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>> createCellFactory() {
		return new Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>>() {
			@Override
			public TableCell<TableProperty, String> call(
					TableColumn<TableProperty, String> param) {
				TableCell<TableProperty, String> cell = new TableCell<>();
				Text text = new Text();
				cell.setGraphic(text);
				cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
				text.wrappingWidthProperty().bind(cell.widthProperty());
				text.textProperty().bind(cell.itemProperty());
				return cell;
			}
		};
	}


	public void setTrainMap(ConcurrentHashMap<String, AbstractTrainSimulator> trainMap) {
		this.trainMap = trainMap;
	}

	public void setTrainNumbers(ObservableList<String> numbers) {
		this.trainNumbers.setItems(numbers);
	}

	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.tripMonitorPaneController.setInfrastructureServiceUtility(infraServiceUtility);

		this.infrastructureServiceUtility = infraServiceUtility;

		for (Line line : this.infrastructureServiceUtility.getNetworkService().allLines()) {
			lineMap.put(line.getName(), line);
		} // Kai

		ObservableList<String> lineList = FXCollections.observableArrayList();

		for (Line line : lineMap.values()) {
			lineList.add(line.getName());
		}
		lineListView.setItems(lineList);

		lineListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (oldValue == null || !oldValue.equals(newValue)) {
					stationListView.getItems().clear();
					//linePane.getChildren().clear();
				}
				lineRoot.setVisible(true);
				tripRoot.setVisible(false);

				updateLineUI();
			}
		});
	}

	public void setUIInfraSetting(UIInfrastructureSetting uiInfraSetting) {
		this.tripMonitorPaneController.setUIInfraSetting(uiInfraSetting);
	}

	private void updateLineUI() {
		String lineString = lineListView.getSelectionModel().getSelectedItem().toString();
		Line line = lineMap.get(lineString);
		Collection<Station> stations = this.infrastructureServiceUtility.getLineService().findStationsByLine(line);

		LineData lineData = lineMonitorPaneController.getLineData(lineString);
		if (lineData == null) {
			lineData = new LineData(stations, new ArrayList<AbstractTrainSimulator>(trainMap.values()));
		}

		HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeMap =
				trainRunDataManager.getBlockingTimeStairwaysInLine(lineData, trainMap.values());
		HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistanceMap =
				trainRunDataManager.getTimeDistancesInLine(lineData, trainMap.values());

		ObservableList<String> stationNameList = FXCollections.observableArrayList();
		for (Station station : stations) {
			stationNameList.add(station.getName());
		}
		stationListView.setItems(stationNameList);

		lineMonitorPaneController.updateUI(lineString, lineData, blockingTimeMap, timeDistanceMap);
	}

	public static ObservableList<TableProperty> generateTrainInfo(
			AbstractTrainSimulator train, String trainNumber) {

		ObservableList<TableProperty> observableTrainInfoList = FXCollections.observableArrayList();
		observableTrainInfoList.add(new TableProperty("Train Number", trainNumber));
		observableTrainInfoList.add(new TableProperty("State",
			train.getTrain().getStatus() == SimpleTrain.ACTIVE ? "In operation ..."	: "Terminated"));
		List<TripElement> elements = train.getTripSection().getTripElements();
		observableTrainInfoList.add(new TableProperty("From",
			((InfrastructureObject) elements.get(0).
					getOperationalPoint()).getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("To",
			((InfrastructureObject) elements.get(elements.size() - 1).
					getOperationalPoint()).getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("Start time",
			elements.get(0).getArriveTime().toString()));

		Duration scheduledTime =
			elements.get(elements.size() - 1).getDepartureTime().getDifference(elements.get(0).getDepartureTime());
		observableTrainInfoList.add(new TableProperty("Scheduled conveyance time", scheduledTime.toString()));

		observableTrainInfoList.add(new TableProperty("Devations",
				train.getDelay().toString()));

		return observableTrainInfoList;
	}
}
