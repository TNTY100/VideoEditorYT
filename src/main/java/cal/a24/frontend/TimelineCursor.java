package cal.a24.frontend;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

public class TimelineCursor extends Rectangle {

    @Getter
    private SimpleDoubleProperty maxTranslateX;

    public TimelineCursor() {
        maxTranslateX = new SimpleDoubleProperty(0);
        setWidth(5);
        setFill(Color.color(0,0,0, 0.8));
    }

    /**
     *
     * @param position : Value between 0 and 1
     */
    public void setRelativePosition(double position) {
        setTranslateX(position * maxTranslateX.get());
    }
}
