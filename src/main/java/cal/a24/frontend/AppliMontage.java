package cal.a24.frontend;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static jdk.jfr.consumer.EventStream.openFile;

public class AppliMontage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        var listeLecture = new ListeLecture(stage);

        final GridPane inputGridPane = new GridPane();
        inputGridPane.setGridLinesVisible(true);

        RowConstraints rowConstraints1 = new RowConstraints();
        rowConstraints1.setPercentHeight(50.0);
        RowConstraints rowConstraints2 = new RowConstraints();
        rowConstraints2.setPercentHeight(50.0);

        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setPercentWidth(25.0);
        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setPercentWidth(50.0);
        ColumnConstraints columnConstraints3 = new ColumnConstraints();
        columnConstraints3.setPercentWidth(25.0);

        inputGridPane.getRowConstraints().addAll(rowConstraints1, rowConstraints2);
        inputGridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints2, columnConstraints3);


        Rectangle videoViewer = new Rectangle();
        videoViewer.setStyle("-fx-background-color: black");
        Rectangle exporter = new Rectangle();
        exporter.setStyle("-fx-background-color: grey;");
        Rectangle timeline = new Rectangle();
        timeline.setStyle("-fx-background-color: orange;");

        GridPane.setConstraints(listeLecture, 0, 0);
        GridPane.setConstraints(videoViewer, 1, 0, 1, 1);
        GridPane.setConstraints(exporter, 3, 0);
        GridPane.setConstraints(timeline, 0, 2, 3, 1);

        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(listeLecture);
        inputGridPane.getChildren().addAll(videoViewer);
        inputGridPane.getChildren().addAll(exporter);
        inputGridPane.getChildren().addAll(timeline);

        Scene scene = new Scene(inputGridPane);
        inputGridPane.prefHeightProperty().bind(scene.heightProperty());
        inputGridPane.prefWidthProperty().bind(scene.widthProperty());

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Video Editor YT");
        stage.show();
    }
}
