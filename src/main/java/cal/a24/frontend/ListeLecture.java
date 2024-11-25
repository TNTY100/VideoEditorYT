package cal.a24.frontend;

import cal.a24.model.Video;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.File;
import java.io.IOException;


public class ListeLecture extends VBox {

    public ListeLecture(Stage stage) {

        final FileChooser fileChooser = new FileChooser();

        final Button openButton = new Button("Ajouter une vidéo à la liste...");

        openButton.setOnAction(
                e -> {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        addFileToList(file);
                    }
                });

        this.getChildren().add(openButton);
    }

    private void addFileToList(File file) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
             JavaFXFrameConverter converter = new JavaFXFrameConverter()) {
            grabber.start();
            grabber.setFrameNumber(0);
            Frame firstFrame = grabber.grabImage();
            Image image = converter.convert(firstFrame);

            VideoTile videoTile = new VideoTile(new Video(file.getPath(), image, grabber.getLengthInTime()));
            getChildren().add(videoTile);

            grabber.stop();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("Video à ajouter à la liste :");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP4", "*.mp4"),
                new FileChooser.ExtensionFilter("MOV", "*.mov")
        );
    }
}
