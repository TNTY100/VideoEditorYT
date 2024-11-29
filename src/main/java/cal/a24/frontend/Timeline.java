package cal.a24.frontend;

import cal.a24.model.Montage;
import cal.a24.model.Segment;
import cal.a24.model.Video;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public class Timeline extends VBox {

    private final Montage montage;
    private final ListeLecture listeLecture;
    private HBox timelineVideo;

    public Timeline(Stage stage, Montage montage, ListeLecture listeLecture) {
        this.montage = montage;
        this.listeLecture = listeLecture;

        setFillWidth(true);
        ScrollPane scrollPane = new ScrollPane();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        Button buttonBack = new Button("Back");
        // Button buttonBackFrame = new Button("Back frame");
        Button buttonPlayPause = new Button("Play");
        // Button buttonForwardFrame = new Button("Forward frame");
        Button buttonForward = new Button("Forward");
        Button addVideoToTimeline = new Button("Ajouter la vidéo");
        Button moveSegmentRight = new Button("Bouger segment à droite");
        Button moveSegmentLeft = new Button("Bouger segment à gauche");

        addVideoToTimeline.setStyle("-fx-start-margin: 100px");
        HBox bouttons = new HBox();

        bouttons.getChildren().addAll(
                buttonBack,
                // buttonBackFrame,
                buttonPlayPause,
                // buttonForwardFrame,
                buttonForward,
                addVideoToTimeline,
                moveSegmentLeft,
                moveSegmentRight
        );

        timelineVideo = new HBox();
        scrollPane.setContent(timelineVideo);

        addVideoToTimeline.setOnAction(_ -> {
            try {
                getVideoFromListLecture();
            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.getChildren().addAll(bouttons, scrollPane);
    }

    public void getVideoFromListLecture() throws FFmpegFrameGrabber.Exception {
        Video selectedVideo = listeLecture.getSelectedVideo();
        if (selectedVideo == null) {
            return;
        }

        montage.addSegment(new Segment(selectedVideo.getPATH()));
        timelineVideo.getChildren().clear();
        timelineVideo.getChildren().addAll(
                montage.getSegmentList().stream()
                        .map(SegmentBlock::new)
                        .toList());
        timelineVideo.getChildren().forEach((node -> {
            if (node instanceof SegmentBlock) {
                System.out.println(((SegmentBlock) node).getSegment().getDuree());
            }
        }));
    }

}
