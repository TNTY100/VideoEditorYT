package cal.a24.frontend;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public class AppliMontage extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws FFmpegFrameGrabber.Exception {
        final GridPane inputGridPane = new GridPane();

        // Création des composants de l'application
        ListeLecture listeLecture = new ListeLecture(stage);

        TimelineCursor cursor = new TimelineCursor();
        TimelineCursorContainer timelineCursorContainer = new TimelineCursorContainer(cursor);

        TimelinePlayer videoViewer = new TimelinePlayer(inputGridPane, cursor);

        VideoTimeline timeline = new VideoTimeline(listeLecture, videoViewer, timelineCursorContainer);

        ExportComponent exporter = new ExportComponent(stage, timeline);

        // Gestion des abonnements :
        timeline.setOnChange(videoViewer::onMontageChange);
        timeline.setOnChangeTime(videoViewer::onMontageTimeChange);
        timelineCursorContainer.setOnFrameChange(videoViewer::onChangeTime);
        listeLecture.setOnAddVideo(exporter::updateFields);


        // Gestion du style
        videoViewer.fillWidthProperty().set(true);

        // Contraintes du grid principal :
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

        // Placement des composants.
        GridPane.setConstraints(listeLecture, 0, 0);
        GridPane.setConstraints(videoViewer, 1, 0, 1, 1);
        GridPane.setConstraints(exporter, 2, 0);
        GridPane.setConstraints(timeline, 0, 1, 3, 1);

        // Ajout des composants
        inputGridPane.getChildren().addAll(listeLecture, timeline, videoViewer, exporter);

        // Création de la scène
        Scene scene = new Scene(inputGridPane);
        // Permet au Grid de s'agrandir avec la scène
        inputGridPane.prefHeightProperty().bind(scene.heightProperty());
        inputGridPane.prefWidthProperty().bind(scene.widthProperty());

        // Setup de la scène.
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("Video Editor YT");
        stage.show();
    }
}
