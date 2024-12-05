package cal.a24.frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;

public class AppliMontage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws FFmpegFrameGrabber.Exception {
        final GridPane inputGridPane = new GridPane();
        inputGridPane.setGridLinesVisible(true);


        ListeLecture listeLecture = new ListeLecture(stage);

        TimelineCursor cursor = new TimelineCursor();

        TimelineCursorContainer timelineCursorContainer = new TimelineCursorContainer(cursor);

        TimelinePlayer videoViewer = new TimelinePlayer(inputGridPane, cursor);

        VideoTimeline timeline = new VideoTimeline(listeLecture, timelineCursorContainer);

        // Souscription à la time line du video player
        timeline.setOnChange(videoViewer::onMontageChange);
        timeline.setOnChangeTime(videoViewer::onMontageTimeChange);

        videoViewer.fillWidthProperty().set(true);


        RowConstraints rowConstraints1 = new RowConstraints();
        rowConstraints1.setPercentHeight(60.0);
        RowConstraints rowConstraints2 = new RowConstraints();
        rowConstraints2.setPercentHeight(40.0);

        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setPercentWidth(25.0);
        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setPercentWidth(50.0);
        ColumnConstraints columnConstraints3 = new ColumnConstraints();
        columnConstraints3.setPercentWidth(25.0);

        inputGridPane.getRowConstraints().addAll(rowConstraints1, rowConstraints2);
        inputGridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints2, columnConstraints3);



        Rectangle exporter = new Rectangle();
        exporter.setStyle("-fx-background-color: grey;");


        GridPane.setConstraints(listeLecture, 0, 0);
        GridPane.setConstraints(videoViewer, 1, 0, 1, 1);
        GridPane.setConstraints(exporter, 2, 0);
        GridPane.setConstraints(timeline, 0, 1, 3, 1);

        inputGridPane.getChildren().addAll(listeLecture, timeline, videoViewer, exporter);

        Scene scene = new Scene(inputGridPane);
        inputGridPane.prefHeightProperty().bind(scene.heightProperty());
        inputGridPane.prefWidthProperty().bind(scene.widthProperty());

        // Autres ajouts
        listeLecture.addFileToList(new File("C:\\Users\\1ythibault\\Videos\\1-FonctionnementAPI.mp4"));


        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Video Editor YT");
        stage.show();
    }
}
