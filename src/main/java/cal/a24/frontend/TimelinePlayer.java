package cal.a24.frontend;

import cal.a24.model.Montage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;

import java.time.Instant;


public class TimelinePlayer extends VBox {

    private Montage montage;

    private ImageView imageView;

    private long currentTimestamp;
    private long tempTotalMontage;
    private int readMultiplyer = 0;
    // private long deltaFrame = 10000; // 1000 for 30 FPS
    private final TimelineCursor cursor;

    public TimelinePlayer(GridPane inputGridPane, TimelineCursor cursor) {
        this.cursor = cursor;
        // Ajout du rectangle

        Image image = new Image("file:C:\\Users\\1ythibault\\Pictures\\Screenshots\\000000000000000000000000000000000000000000000000000.png", true);
        imageView = new ImageView(image);
        imageView.smoothProperty().set(false);
        imageView.setPreserveRatio(true);
        imageView.fitWidthProperty().bind(inputGridPane.widthProperty().divide(2));
        imageView.fitHeightProperty().bind(inputGridPane.heightProperty().multiply(0.57));

        // Ajout du timer
        Button buttonBack = new Button("Back");
        buttonBack.setOnAction((_) -> {
            readMultiplyer -= 1;
        });
        // Button buttonBackFrame = new Button("Back frame");
        Button buttonPlayPause = new Button("Play/Pause");
        buttonPlayPause.setOnAction(_ -> {
            if (readMultiplyer == 0) {
                readMultiplyer = 1;
            }
            else {
                readMultiplyer = 0;
            }
        });
        // Button buttonForwardFrame = new Button("Forward frame");
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
    }

    public void videoLoop() {
        long lastUpdate = Instant.now().toEpochMilli();
        while (true) {
            long currentUpdate = Instant.now().toEpochMilli();
            videoThread((currentUpdate - lastUpdate) * 1000);
            lastUpdate = currentUpdate;
        }
    }

    public void videoThread(long deltaTime) {
        currentTimestamp += deltaTime * readMultiplyer;
        if (tempTotalMontage == 0 || currentTimestamp > tempTotalMontage) {
            currentTimestamp -= deltaTime * readMultiplyer;
            readMultiplyer = 0;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        imageView.setImage(montage.getImageFXAtTimeStamp(currentTimestamp)); // TODO : Mettre try
        updateCursor();
    }

    public void onMontageChange(Montage pMontage) {
        montage = pMontage;
        onMontageTimeChange();
    }

    public void onMontageTimeChange() {
        readMultiplyer = 0;
        tempTotalMontage = montage.getDureeTotale();
        if (currentTimestamp > tempTotalMontage) {
            currentTimestamp = tempTotalMontage;
            updateCursor();
        }
    }

    public void updateCursor() {
        cursor.setRelativePosition((double) currentTimestamp / tempTotalMontage);
        imageView.setImage(montage.getImageFXAtTimeStamp(currentTimestamp)); // TODO : Mettre try
    }
}
