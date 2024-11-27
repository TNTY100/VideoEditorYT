package cal.a24.frontend;

import cal.a24.model.Video;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.File;
import java.io.IOException;


public class ListeLecture extends VBox {
    final private VBox videoList;

    private VideoTile selectedVideoTile;

    public ListeLecture(Stage stage) {
        // Boutons de gestion de la liste
        final FileChooser fileChooser = new FileChooser();
        final Button openButton = new Button("Ajouter une vidéo à la liste...");

        final Button removeButton = new Button("Retirer sélectionnée");

        HBox boutonGestionContainer = new HBox();
        boutonGestionContainer.getChildren().addAll(openButton, removeButton);
        // Enlève

        videoList = new VBox();
        videoList.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(videoList);
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Permet de grandir
        scrollPane.setFitToWidth(true);

        videoList.prefHeightProperty().bind(this.prefHeightProperty());

        // Setup des actions des boutons.
        openButton.setOnAction(
                e -> {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        addFileToList(file);
                    }
                });
        removeButton.setOnAction(
                _ -> {
                    if (selectedVideoTile == null) {
                        return;
                    }
                    videoList.getChildren().remove(selectedVideoTile);
                }
        );
        this.getChildren().addAll(boutonGestionContainer, scrollPane);
    }

    private void addFileToList(File file) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
             JavaFXFrameConverter converter = new JavaFXFrameConverter()) {
            grabber.start();
            grabber.setFrameNumber(0);
            Frame firstFrame = grabber.grabImage();
            Image image = converter.convert(firstFrame);

            VideoTile videoTile = createVideoTile(file, image, grabber);
            videoList.getChildren().add(videoTile);

            grabber.stop();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private VideoTile createVideoTile(File file, Image image, FFmpegFrameGrabber grabber) {
        VideoTile videoTile = new VideoTile(new Video(file.getPath(), image, grabber.getLengthInTime()));
        videoTile.setOnMousePressed(_ -> {
            if (selectedVideoTile != null){
                selectedVideoTile.setStyle("");
            }
            if (selectedVideoTile != videoTile) {
                selectedVideoTile = videoTile;
                selectedVideoTile.setStyle("-fx-border-color: red");
            }
            else {
                selectedVideoTile = null;
            }
        });
        return videoTile;
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("Video à ajouter à la liste :");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP4", "*.mp4"),
                new FileChooser.ExtensionFilter("MOV", "*.mov")
        );
    }
}
