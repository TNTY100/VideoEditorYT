package cal.a24.frontend;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

@Getter
public class TimelineCursor extends Rectangle {

    private final SimpleDoubleProperty maxTranslateX;

    public TimelineCursor() {
        maxTranslateX = new SimpleDoubleProperty(0);
        setWidth(5);
        setFill(Color.color(0,0,0, 0.8));
    }

    /**
     * Place le curseur sur la timeline
     * @param position : Valeurs entre 0 et 1
     */
    public TimelineCursor setRelativePosition(double position) {
        if (position < 0 || position > 1) {
            return this;
        }

        setTranslateX(position * maxTranslateX.get());
        return this;
    }
}
