package cal.a24.frontend;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class Timeline extends HBox {



    public Timeline(Stage stage) {
        setFillHeight(true);
        ScrollPane scrollPane = new ScrollPane();
        HBox.setHgrow(scrollPane, Priority.ALWAYS);


        this.getChildren().addAll(scrollPane);
    }
}
