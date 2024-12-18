package cal.a24.frontend;

import cal.a24.model.Montage;
import cal.a24.model.Video;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;
import java.util.function.Predicate;

public class ExportComponent extends GridPane {

    private final VideoTimeline timeline;
    private boolean hadFirstValueUpdate = false;
    private final TextField fileNameField;
    private final TextField frameRateField;
    private final TextField widthField;
    private final TextField heightField;
    private final TextField videoBitrateField;
    private final TextField sampleRateField;
    private final TextField audioBitrateField;

    public ExportComponent(Stage stage, VideoTimeline timeline) {
        this.timeline = timeline;
        setPadding(new Insets(10));
        setHgap(10);
        setVgap(10);
        setAlignment(Pos.CENTER);

        fileNameField = new TextField();
        fileNameField.setPromptText("Select file location");
        fileNameField.setEditable(false);

        Button browseButton = new Button("Chercher");

        // Configure the FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select or Save File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers MP4", "*.mp4"),
                new FileChooser.ExtensionFilter("Fichiers MOV", "*.mov"),
                new FileChooser.ExtensionFilter("Tout fichiers", "*.*")
        );

        browseButton.setOnAction(e -> {
            fileChooser.setInitialFileName(fileNameField.getText());
            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile != null) {
                fileNameField.setText(selectedFile.getAbsolutePath());
            }
        });

        add(browseButton, 0, 0);
        add(fileNameField, 1, 0);

        Label frameRateLabel = new Label("Image par secondes :");
        frameRateField = new TextField();
        frameRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                frameRateField.setText(oldValue);
            }
        });
        add(frameRateLabel, 0, 1);
        add(frameRateField, 1, 1);

        Label widthLabel = new Label("Largeur (px) : ");
        widthField = new TextField();
        widthField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                widthField.setText(oldValue);
            }
        });
        add(widthLabel, 0, 2);
        add(widthField, 1, 2);

        Label heightLabel = new Label("Hauteur (px) :");
        heightField = new TextField();
        heightField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                heightField.setText(oldValue);
            }
        });
        add(heightLabel, 0, 3);
        add(heightField, 1, 3);

        Label videoBitrateLabel = new Label("Bitrate vidéo :");
        videoBitrateField = new TextField();
        videoBitrateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                videoBitrateField.setText(oldValue);
            }
        });
        add(videoBitrateLabel, 0, 4);
        add(videoBitrateField, 1, 4);

        Label audioBitrateLabel = new Label("Bitrate audio :");
        audioBitrateField = new TextField();
        audioBitrateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                audioBitrateField.setText(oldValue);
            }
        });
        add(audioBitrateLabel, 0, 5);
        add(audioBitrateField, 1, 5);

        Label sampleRateLabel = new Label("Taux d'échantillonnage (sample rate) :");
        sampleRateField = new TextField();
        sampleRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                sampleRateField.setText(oldValue);
            }
        });
        add(sampleRateLabel, 0, 6);
        add(sampleRateField, 1, 6);

        Button submitButton = new Button("Exporter");
        add(submitButton, 0, 7, 2, 1);
        submitButton.setOnAction(e -> {
            exporterMontage();
        });
    }

    private void exporterMontage() {
        boolean isValid = true;

        Predicate<TextField> testEmpty = (TextField textField) -> textField.getText().trim().isEmpty();

        // Validation des données avec du feedback
        isValid &= validateField(fileNameField, (field) -> field.getText().matches("(\\.mp4|\\.mov)$"));
        isValid &= validateField(fileNameField, testEmpty);
        isValid &= validateField(frameRateField, testEmpty);
        isValid &= validateField(widthField, testEmpty);
        isValid &= validateField(heightField, testEmpty);
        isValid &= validateField(videoBitrateField, testEmpty);
        isValid &= validateField(audioBitrateField, testEmpty);
        isValid &= validateField(sampleRateField, testEmpty);


        if (!isValid) {
            return;
        }
        // Validation des FP
        Montage montage = timeline.getMontage();
        int frameRate = Integer.parseInt(frameRateField.getText());
        isValid = validateField(frameRateField,
                (_) -> frameRate > montage.getSegments().stream()
                        .mapToInt(value -> (int) value.getGrabber().getFrameRate())
                        .min().orElse(0));

        if (!isValid) {
            return;
        }

        // Conversion des données
        String fileName = fileNameField.getText();
        int width = Integer.parseInt(widthField.getText());
        int height = Integer.parseInt(heightField.getText());
        int videoBitrate = Integer.parseInt(videoBitrateField.getText());
        int audioBitrate = Integer.parseInt(audioBitrateField.getText());
        int sampleRate = Integer.parseInt(sampleRateField.getText());

        // Préparation
        Stage dialogBlock = new Stage();
        dialogBlock.setTitle("Vidéo en traitement");

        StackPane popupContent = new StackPane();
        Text popupText = new Text("La vidéo est en traitement");
        popupContent.getChildren().add(popupText);

        Scene popupScene = new Scene(popupContent, 200, 100);
        dialogBlock.setOnCloseRequest(Event::consume);
        dialogBlock.setScene(popupScene);
        dialogBlock.setAlwaysOnTop(true);
        dialogBlock.initModality(Modality.APPLICATION_MODAL);
        dialogBlock.show();
        new Thread(() -> {
            try {
                montage.export(fileName, width, height, frameRate, videoBitrate, sampleRate, audioBitrate);
            } finally {
                javafx.application.Platform.runLater(dialogBlock::close);
            }
        }).start();

    }

    private <T extends Node> boolean validateField(T textfield, Predicate<T> predicate) {
        if (predicate.test(textfield)) {
            textfield.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
            return false;
        } else {
            textfield.setStyle("");
            return true;
        }
    }

    public void updateFields(Video video) {
        try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(video.getPATH())) {
            frameGrabber.start();

            if (!hadFirstValueUpdate) {
                frameRateField.setText((int) frameGrabber.getFrameRate() + "");
                widthField.setText(frameGrabber.getImageWidth() + "");
                heightField.setText(frameGrabber.getImageHeight() + "");
                videoBitrateField.setText(frameGrabber.getVideoBitrate() + "");
                audioBitrateField.setText(frameGrabber.getAudioBitrate() + "");
                sampleRateField.setText(frameGrabber.getSampleRate() + "");
            }

            hadFirstValueUpdate = true;
            frameGrabber.stop();
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }

    }
}
