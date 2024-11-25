package cal.a24.frontend;

import cal.a24.model.Video;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class VideoTile extends HBox {

    Video video;

    public VideoTile(Video video) {
        this.video = video;

        ImageView imageView = new ImageView(this.video.getImageFX());
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(100);
        this.getChildren().add(imageView);
        this.setStyle("--border-color: black;");
        System.out.println("OUT");
    }
}
