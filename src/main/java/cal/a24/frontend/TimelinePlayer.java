package cal.a24.frontend;

import cal.a24.model.Montage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import lombok.Getter;

import java.time.Instant;


public class TimelinePlayer extends VBox {

    private Montage montage;

    private final ImageView imageView;

    private boolean appIsOn = true;
    @Getter
    private long currentTimestamp;
    private long tempTotalMontage;
    private int readMultiplyer = 0;
    private final TimelineCursor cursor;
    private final Image image = new Image("file:./src/main/resources/imageGenerique.png", true);

    public TimelinePlayer(GridPane inputGridPane, TimelineCursor cursor, Stage stage) {
        this.cursor = cursor;

        // Ajout du viewport de la vidéo
        imageView = new ImageView(image);
        imageView.smoothProperty().set(false);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(inputGridPane.widthProperty().divide(2));
        imageView.fitHeightProperty().bind(inputGridPane.heightProperty().multiply(0.57));

        // TODO : Ajout du timer

        // Ajout des contrôles
        Button buttonBack = new Button("Back");
        buttonBack.setOnAction((_) -> {
            readMultiplyer -= 1;
        });
        Button buttonPlayPause = new Button("Play/Pause");
        buttonPlayPause.setOnAction(_ -> {
            if (readMultiplyer == 0) {
                readMultiplyer = 1;
            } else {
                readMultiplyer = 0;
            }
        });
        Button buttonForward = new Button("Forward");
        buttonForward.setOnAction(_ -> {
            readMultiplyer += 1;
        });

        HBox buttons = new HBox();
        buttons.getChildren().addAll(
                buttonBack,
                // buttonBackFrame,
                buttonPlayPause,
                // buttonForwardFrame,
                buttonForward
        );

        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(imageView, buttons);
        setAlignment(Pos.CENTER);
        setSpacing(3);
        setBackground(Background.fill(Paint.valueOf("lightgrey")));

        new Thread(this::videoLoop).start();
        stage.onCloseRequestProperty().set(event -> appIsOn = false);
    }

    public void videoLoop() {
        long lastUpdate = Instant.now().toEpochMilli();
        while (appIsOn) {
            long currentUpdate = Instant.now().toEpochMilli();
            videoUpdate((currentUpdate - lastUpdate) * 1000);
            lastUpdate = currentUpdate;
        }
    }

    public void videoUpdate(long deltaTime) {
        currentTimestamp += deltaTime * readMultiplyer;
        if (readMultiplyer == 0) {
            return;
        }

        if (currentTimestamp > tempTotalMontage || currentTimestamp < 0 || tempTotalMontage == 0) {
            if (readMultiplyer > 0) {
                currentTimestamp = tempTotalMontage - 1;
            } else {
                currentTimestamp = 0;
            }
            readMultiplyer = 0;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        updateImage();
        updateCursor();
    }

    public void onMontageChange(Montage pMontage) {
        montage = pMontage;
        onMontageTimeChange();
    }

    public void onMontageTimeChange() {
        readMultiplyer = 0;
        tempTotalMontage = montage.getDureeTotale();
        if (tempTotalMontage == 0) {
            imageView.setImage(image);
        } else if (currentTimestamp > tempTotalMontage) {
            currentTimestamp = tempTotalMontage;
            updateImage();
            updateCursor();
        }
    }

    /**
     * @param value Valeurs entre 0 et 1
     */
    public void onChangeTime(Double value) {
        if (value < 0 || value > 1) {
            return;
        }
        currentTimestamp = (long) (tempTotalMontage * value);
        updateImage();
    }


    public void updateImage() {
        try {
            imageView.setImage(montage.getImageFXAtTimeStamp(currentTimestamp));
        } catch (RuntimeException e) {
            imageView.setImage(image);
        }
    }

    public void updateCursor() {
        cursor.setRelativePosition((double) currentTimestamp / tempTotalMontage);
    }
}
