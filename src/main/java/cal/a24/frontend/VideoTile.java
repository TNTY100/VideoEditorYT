package cal.a24.frontend;

import cal.a24.model.Video;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;

public class VideoTile extends Button {

    Video video;

    public VideoTile(Video video) {
        // Image
        this.video = video;
        ImageView imageView = new ImageView(this.video.getImageFX());
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(75);

        // Setup Boutton
        this.setText(video.getPATH());
        this.setGraphic(imageView);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);

        // Lock du bouton
        this.setMaxWidth(Double.MAX_VALUE);
    }
}
