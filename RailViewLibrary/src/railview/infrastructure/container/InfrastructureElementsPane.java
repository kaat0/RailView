package railview.infrastructure.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.RelativePosition;
import railapp.infrastructure.element.dto.Track;
import railapp.infrastructure.element.dto.Turnout;
import railapp.infrastructure.signalling.dto.AbstractSignal;
import railapp.units.Coordinate;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class InfrastructureElementsPane extends PannablePane {
	private Collection<InfrastructureElement> elements;
	private Collection<AbstractSignal> signals;
	private CoordinateMapper mapper;
	private Color elementColor;
	private Color signalColor;
	private List<Coordinate> path = null;
	private Coordinate eventPoint = null;

	private double elementWidth = 0.1;

	public InfrastructureElementsPane() {
		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}

	public void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}

	public void setHighlightedPath(List<Coordinate> path) {
		this.path = path;
	}

	public void setEventPoint(Coordinate coordinate) {
		this.eventPoint = coordinate;
	}

	public void setAndDrawElements(Collection<InfrastructureElement> elements,
			Color elementColor) {
		this.elements = elements;
		this.elementColor = elementColor;
		this.draw();
	}

	public void setSignals(Collection<AbstractSignal> signals, Color signalColor) {
		// TODO Auto-generated method stub
		this.signals = signals;
		this.signalColor = signalColor;
	}

	public ObservableList<Node> getChrildren() {
		return this.getChrildren();
	}

	public void setElements(Collection<InfrastructureElement> elements) {
		this.elements = elements;
	}

	public void draw() {
		if (this.elements == null)
			return;

		this.getChildren().clear();

		for (InfrastructureElement element : this.elements) {
			this.drawInfrastructureElement(element);
		}

		if (this.signals != null) { // for some panes, it is not necessary to
									// draw signals.
			for (AbstractSignal signal : this.signals) {
				this.drawSignal(signal);
			}
		}

		this.drawPath();
		this.drawEventPoint();
	}

	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			List<Coordinate> coordinates = ((Track) element)
					.getCoordinateAtLink12();
			this.drawLink(coordinates, this.elementColor, elementWidth);
		} else {
			if (element instanceof Turnout) {
				List<Coordinate> coordinates = element.findLink(1, 2)
						.getCoordinates();
				Coordinate middlePoint = Coordinate.fromXY(0.5 * (coordinates
						.get(0).getX() + coordinates.get(1).getX()),
						0.5 * (coordinates.get(0).getY() + coordinates.get(1)
								.getY()));
				this.drawLink(coordinates, this.elementColor, elementWidth);
				coordinates = element.findLink(1, 3).getCoordinates();
				coordinates.add(1, middlePoint);
				this.drawLink(coordinates, this.elementColor, elementWidth);
			} else {
				List<Coordinate> coordinates = element.findLink(1, 3)
						.getCoordinates();
				this.drawLink(coordinates, this.elementColor, elementWidth);
				coordinates = element.findLink(2, 4).getCoordinates();
				this.drawLink(coordinates, this.elementColor, elementWidth);
			}
		}
	}

	private void drawLink(List<Coordinate> coordinates, Color color,
			double width) {
		for (int i = 0; i < coordinates.size() - 1; i++) {
			Line line = new Line();

			line.setStartX(mapper.mapToPaneX(coordinates.get(i).getX(), this));
			line.setStartY(mapper.mapToPaneY(coordinates.get(i).getY(), this));
			line.setEndX(mapper.mapToPaneX(coordinates.get(i + 1).getX(), this));
			line.setEndY(mapper.mapToPaneY(coordinates.get(i + 1).getY(), this));

			line.setStroke(color);
			line.setStrokeWidth(width);

			this.getChildren().add(line);
		}
	}

	private void drawSignal(AbstractSignal signal) {
		RelativePosition position = signal.getPositions().get(0);
		double percentage = position.getDistance().getMeter()
				/ position.getLink().getGeometry().getLength().getMeter();

		List<Coordinate> coordinates = position.getLink().getCoordinates();
		List<Double> coorDists = new ArrayList<Double>();
		double totalCoorDist = 0;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			double coorDist = Coordinate.getDistance(coordinates.get(i),
					coordinates.get(i + 1));
			totalCoorDist += coorDist;
			coorDists.add(totalCoorDist);
		}

		InfrastructureObjectCoordinate signalCoor = null;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			if (coorDists.get(i) / totalCoorDist >= percentage) {
				double percentageInSegment = (coorDists.get(i) / totalCoorDist - percentage)
						* totalCoorDist
						/ (i == 0 ? coorDists.get(i)
								: (coorDists.get(i) - coorDists.get(i - 1)));
				signalCoor = new InfrastructureObjectCoordinate(
						coordinates.get(i), coordinates.get(i + 1),
						percentageInSegment);
				break;
			}
		}
		if (signalCoor == null) {
			signalCoor = new InfrastructureObjectCoordinate(
					coordinates.get(coordinates.size() - 2),
					coordinates.get(coordinates.size() - 1), 1);
		}

		double xStart = signalCoor.getStart().getX();
		double yStart = signalCoor.getStart().getY();
		double xEnd = signalCoor.getEnd().getX();
		double yEnd = signalCoor.getEnd().getY();

		double signalStartX = xStart + (percentage * (xEnd - xStart));
		double signalStartY = yStart + (percentage * (yEnd - yStart));

		// random static number a and b
		double a = 450.0;
		double b = 150.0;

		double xCoordinateP0;
		double yCoordinateP0;
		double xCoordinateP1;
		double yCoordinateP1;
		double xCoordinateP2;
		double yCoordinateP2;
		double xCoordinateP3;
		double yCoordinateP3;

		double length = Math.sqrt(Math.pow((yEnd - yStart), 2)+ Math.pow((xEnd - xStart), 2));

		xCoordinateP0 = signalStartX + (a * ((yEnd - yStart)/ length));
		yCoordinateP0 = signalStartY + (a * ((xStart - xEnd)/ length));

		xCoordinateP1 = signalStartX + ((a-(b/2.0)) * ((yEnd - yStart)/ length));
		yCoordinateP1 = signalStartY + ((a-(b/2.0)) * ((xStart - xEnd)/ length));

		xCoordinateP2 = signalStartX + ((a+(b/2.0)) * ((yEnd - yStart)/ length));
		yCoordinateP2 = signalStartY + ((a+(b/2.0)) * ((xStart - xEnd)/ length));

		xCoordinateP3 = xCoordinateP0 + ((xEnd-xStart) * a / length);
		yCoordinateP3 = yCoordinateP0 + ((yEnd-yStart)* a / length);

		Line line1 = new Line();
		line1.setStartX(mapper.mapToPaneX(xCoordinateP0, this));
		line1.setStartY(mapper.mapToPaneY(yCoordinateP0, this));
		line1.setEndX(mapper.mapToPaneX(xCoordinateP3, this));
		line1.setEndY(mapper.mapToPaneY(yCoordinateP3, this));
		line1.setStroke(Color.RED);
		line1.setStrokeWidth(0.1);

		Line line2 = new Line();
		line2.setStartX(mapper.mapToPaneX(xCoordinateP2, this));
		line2.setStartY(mapper.mapToPaneY(yCoordinateP2, this));
		line2.setEndX(mapper.mapToPaneX(xCoordinateP1, this));
		line2.setEndY(mapper.mapToPaneY(yCoordinateP1, this));
		line2.setStroke(Color.RED);
		line2.setStrokeWidth(0.1);


		Circle circle = new Circle();
		circle.setRadius(0.20);
		circle.setCenterX(mapper.mapToPaneX(xCoordinateP3, this));
		circle.setCenterY(mapper.mapToPaneY(yCoordinateP3, this));
		circle.setStrokeWidth(0.1);
		circle.setStroke(Color.RED);

		this.getChildren().addAll(line1, line2, circle);

/**
		// angle between line and x-axis
//		double angle = (yEnd - yStart) / (xEnd - xStart);
		double alpha = Math.atan2(yEnd - yStart, xEnd - xStart);

		// parallel line
		Line line = new Line();
		// orthogonal line
		Line orthLine = new Line();
		Circle circle = new Circle();

		// line is horizontal
		if (yStart == yEnd) {
			line.setStartX(mapper.mapToPaneX(signalStartX, this));
			line.setStartY(mapper.mapToPaneY(signalStartY + a, this));
			line.setEndX(mapper.mapToPaneX(signalStartX + a, this));
			line.setEndY(mapper.mapToPaneY(signalStartY + a, this));
			line.setStroke(Color.RED);
			line.setStrokeWidth(0.1);

			orthLine.setStartX(mapper.mapToPaneX(signalStartX, this));
			orthLine.setStartY(mapper.mapToPaneY(signalStartY + a + (b), this));
			orthLine.setEndX(mapper.mapToPaneX(signalStartX, this));
			orthLine.setEndY(mapper.mapToPaneY(signalStartY + a - (b), this));
			orthLine.setStroke(Color.RED);
			orthLine.setStrokeWidth(0.1);
			circle.setRadius(0.000000000000000001);
			circle.setCenterX(mapper.mapToPaneX(signalStartX + a + circle.getRadius(), this));
			circle.setCenterY(mapper.mapToPaneY(signalStartY + a + circle.getRadius(), this));
			circle.setStroke(Color.RED);
		}

		// line is vertical
		else if (xStart == xEnd) {
			line.setStartX(mapper.mapToPaneX(signalStartX + a, this));
			line.setStartY(mapper.mapToPaneY(signalStartY, this));
			line.setEndX(mapper.mapToPaneX(signalStartX + a, this));
			line.setEndY(mapper.mapToPaneY(signalStartY + a, this));
			line.setStroke(Color.RED);
			line.setStrokeWidth(0.1);

			orthLine.setStartX(mapper.mapToPaneX(signalStartX + a + (b), this));
			orthLine.setStartY(mapper.mapToPaneY(signalStartY, this));
			orthLine.setEndX(mapper.mapToPaneX(signalStartX + a - (b), this));
			orthLine.setEndY(mapper.mapToPaneY(signalStartY, this));
			orthLine.setStroke(Color.RED);
			orthLine.setStrokeWidth(0.1);

			circle.setRadius(0.000000000000000001);
			circle.setCenterX(mapper.mapToPaneX(signalStartX + a + circle.getRadius(), this));
			circle.setCenterY(mapper.mapToPaneY(signalStartY + a + circle.getRadius(), this));
			circle.setStrokeWidth(0.2);
			circle.setStroke(Color.RED);
		}

		// 0 - 360�
		else if (xEnd > xStart && yEnd > yStart) {
			xCoordinateP0 = signalStartX - (a * (Math.sin(alpha)));
			yCoordinateP0 = signalStartY + (a * (Math.cos(alpha)));
			xCoordinateP3 = signalStartX - (a - b / 2) * (Math.sin(alpha));
			yCoordinateP3 = signalStartY + (a - b / 2) * (Math.cos(alpha));
			line.setStartX(mapper.mapToPaneX(xCoordinateP0, this));
			line.setStartY(mapper.mapToPaneY(yCoordinateP0, this));
			line.setEndX(mapper.mapToPaneX(
					xCoordinateP0 + (a * (Math.cos(alpha))), this));
			line.setEndY(mapper.mapToPaneY(
					yCoordinateP0 + (a * (Math.sin(alpha))), this));
			line.setStroke(Color.RED);
			circle.setStrokeWidth(0.1);
			line.setStrokeWidth(0.1);

			orthLine.setStartX(mapper.mapToPaneX(xCoordinateP3, this));
			orthLine.setStartY(mapper.mapToPaneY(yCoordinateP3, this));
			orthLine.setEndX(mapper.mapToPaneX(xCoordinateP0, this));
			orthLine.setEndY(mapper.mapToPaneY(yCoordinateP0, this));
			orthLine.setStroke(Color.RED);
			orthLine.setStrokeWidth(0.1);

		}

		else if (xEnd > xStart && yEnd < yStart) {
			xCoordinateP0 = signalStartX + (a * (Math.sin(alpha)));
			yCoordinateP0 = signalStartY + (a * (Math.cos(alpha)));
			xCoordinateP3 = signalStartX + (a - b / 2) * (Math.sin(alpha));
			yCoordinateP3 = signalStartY + (a - b / 2) * (Math.cos(alpha));
			line.setStartX(mapper.mapToPaneX(xCoordinateP0, this));
			line.setStartY(mapper.mapToPaneY(yCoordinateP0, this));
			line.setEndX(mapper.mapToPaneX(
					xCoordinateP0 + (a * (Math.cos(alpha))), this));
			line.setEndY(mapper.mapToPaneY(
					yCoordinateP0 - (a * (Math.sin(alpha))), this));
			line.setStroke(Color.RED);
			circle.setStrokeWidth(0.1);
			line.setStrokeWidth(0.1);

			orthLine.setStartX(mapper.mapToPaneX(xCoordinateP3, this));
			orthLine.setStartY(mapper.mapToPaneY(yCoordinateP3, this));
			orthLine.setEndX(mapper.mapToPaneX(xCoordinateP0, this));
			orthLine.setEndY(mapper.mapToPaneY(yCoordinateP0, this));
			orthLine.setStroke(Color.RED);
			orthLine.setStrokeWidth(0.1);
		}

		else if (xEnd < xStart && yEnd < yStart) {
			xCoordinateP0 = signalStartX + (a * (Math.sin(alpha)));
			yCoordinateP0 = signalStartY - (a * (Math.cos(alpha)));
			xCoordinateP3 = signalStartX + (a - b / 2) * (Math.sin(alpha));
			yCoordinateP3 = signalStartY - (a - b / 2) * (Math.cos(alpha));
			line.setStartX(mapper.mapToPaneX(xCoordinateP0, this));
			line.setStartY(mapper.mapToPaneY(yCoordinateP0, this));
			line.setEndX(mapper.mapToPaneX(
					xCoordinateP0 - (a * (Math.cos(alpha))), this));
			line.setEndY(mapper.mapToPaneY(
					yCoordinateP0 - (a * (Math.sin(alpha))), this));
			line.setStroke(Color.RED);
			circle.setStrokeWidth(0.1);
			line.setStrokeWidth(0.1);

			orthLine.setStartX(mapper.mapToPaneX(xCoordinateP3, this));
			orthLine.setStartY(mapper.mapToPaneY(yCoordinateP3, this));
			orthLine.setEndX(mapper.mapToPaneX(xCoordinateP0, this));
			orthLine.setEndY(mapper.mapToPaneY(yCoordinateP0, this));
			orthLine.setStroke(Color.RED);
			orthLine.setStrokeWidth(0.1);
		}

		else if (xEnd < xStart && yEnd > yStart) {
			xCoordinateP0 = signalStartX - (a * (Math.sin(alpha)));
			yCoordinateP0 = signalStartY - (a * (Math.cos(alpha)));
			xCoordinateP3 = signalStartX - (a - b / 2) * (Math.sin(alpha));
			yCoordinateP3 = signalStartY - (a - b / 2) * (Math.cos(alpha));
			xCoordinateP3 = signalStartX;
			yCoordinateP3 = signalStartY;
			line.setStartX(mapper.mapToPaneX(xCoordinateP0, this));
			line.setStartY(mapper.mapToPaneY(yCoordinateP0, this));
			line.setEndX(mapper.mapToPaneX(
					xCoordinateP0 - (a * (Math.cos(alpha))), this));
			line.setEndY(mapper.mapToPaneY(
					yCoordinateP0 + (a * (Math.sin(alpha))), this));
			line.setStroke(Color.RED);
			circle.setStrokeWidth(0.1);
			line.setStrokeWidth(0.1);

			orthLine.setStartX(mapper.mapToPaneX(xCoordinateP3, this));
			orthLine.setStartY(mapper.mapToPaneY(yCoordinateP3, this));
			orthLine.setEndX(mapper.mapToPaneX(xCoordinateP0, this));
			orthLine.setEndY(mapper.mapToPaneY(yCoordinateP0, this));
			orthLine.setStroke(Color.RED);
			orthLine.setStrokeWidth(0.1);
		}

		this.getChildren().addAll(line, orthLine, circle);
 *
 **/

		System.out.println(signalCoor);
	}

	private void drawPath() {
		if (path == null) {
			return;
		} else {
			drawLink(path, Color.CYAN, 0.4);
		}
	}

	private void drawEventPoint() {
		if (this.eventPoint != null) {
			Circle circle = new Circle();

			circle.setCenterX((mapper.mapToPaneX(eventPoint.getX(), this)));
			circle.setCenterY((mapper.mapToPaneY(eventPoint.getY(), this)));
			circle.setFill(Color.RED);
			circle.setRadius(0.6);

			this.getChildren().add(circle);
		}
	}

	class InfrastructureObjectCoordinate {
		private Coordinate start;
		private Coordinate end;
		private double percentage;

		public InfrastructureObjectCoordinate(railapp.units.Coordinate start,
				railapp.units.Coordinate end, double percentage) {
			super();
			this.start = start;
			this.end = end;
			this.percentage = percentage;
		}

		public Coordinate getStart() {
			return start;
		}

		public Coordinate getEnd() {
			return end;
		}

		public double percentage() {
			return percentage;
		}

		@Override
		public String toString() {
			return "InfrastructureObjectCoordinate [start=" + start + ", end="
					+ end + ", percentage=" + percentage + "]";
		}
	}
}
