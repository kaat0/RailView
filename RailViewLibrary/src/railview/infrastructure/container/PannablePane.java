package railview.infrastructure.container;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;

/**
 * The canvas which holds all of the nodes of the application.
 */
public class PannablePane extends Pane {

    Scale scaleTransform;

    public PannablePane() {

       // setStyle("-fx-background-color: lightgrey; -fx-border-color: blue; -fx-opacity: 0.5");

        // add scale transform
        scaleTransform = new Scale( 1.0, 1.0);
        getTransforms().add( scaleTransform);

        // logging
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> { 
            System.out.println( 
                    "canvas event: " + ( ((event.getSceneX() - getBoundsInParent().getMinX()) / getScale()) + ", scale: " + getScale())
                    );
            System.out.println( "canvas bounds: " + getBoundsInParent());   
                });
    }


    public Scale getScaleTransform() {
        return scaleTransform;
    }

    public double getScale() {
        return scaleTransform.getY();
    }

    /**
     * Set x/y scale
     * @param scale
     */
    public void setScale( double scale) {
        scaleTransform.setX(scale);
        scaleTransform.setY(scale);
    }

    /**
     * Set x/y pivot points
     * @param x
     * @param y
     */
    public void setPivot( double x, double y) {
        scaleTransform.setPivotX(x);
        scaleTransform.setPivotY(y);
    }
}

