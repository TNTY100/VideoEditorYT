package cal.a24.frontend;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import lombok.Setter;

import java.util.function.Consumer;

public class TimelineCursorContainer extends StackPane {
    private final TimelineCursor cursor;

    public TimelineCursorContainer(TimelineCursor cursor) {
        this.cursor = cursor;
        this.cursor.heightProperty().bind(heightProperty());
        this.cursor.getMaxTranslateX().bind(widthProperty());
        getChildren().addLast(this.cursor);
        setAlignment(Pos.CENTER_LEFT);

        setOnMouseClicked((event) -> {
            if (!event.isControlDown()) {
                return;
            }
            double x = event.getX();
            onChangeTime(cursor.setRelativePosition(x / getWidth()).getTranslateX());
        });
    }

    @Setter
    Consumer<Double> onFrameChange;

    public void onChangeTime(Double value) {
        if (onFrameChange != null)
            onFrameChange.accept(value / getWidth());
    }
}
