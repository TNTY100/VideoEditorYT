package cal.a24.frontend;

import cal.a24.model.Segment;
import cal.a24.model.Video;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.IOException;

public class Timeline extends VBox {

    private final ListeLecture listeLecture;
    private final HBox timelineVideo;
    private SegmentBlock selectedSegmentBlock;

    public Timeline(ListeLecture listeLecture) {
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
        Button deleteSegment = new Button("Suprimer segment");

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
                moveSegmentRight,
                deleteSegment
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

        deleteSegment.setOnAction(_ -> {
            if (selectedSegmentBlock == null) {
                return;
            }
            // Safely remove and nullify first
            timelineVideo.getChildren().remove(selectedSegmentBlock);
            try {
                selectedSegmentBlock.getSegment().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            selectedSegmentBlock = null;
            // Encourage le garbage collector parce que beaucoup d'objet sont devenu orphelin. (Non déterministe)
            System.gc();
        });
        this.getChildren().addAll(bouttons, scrollPane);
    }

    private void getVideoFromListLecture() throws FFmpegFrameGrabber.Exception {
        Video selectedVideo = listeLecture.getSelectedVideo();
        if (selectedVideo == null) {
            return;
        }

        timelineVideo.getChildren().add(new SegmentBlock(new Segment(selectedVideo.getPATH()), this));
    }

    public void setSelectedSegment(SegmentBlock segmentBlock) {
        if (segmentBlock == selectedSegmentBlock) {
            selectedSegmentBlock.setNormalBorder();
            selectedSegmentBlock = null;
            return;
        }

        if (selectedSegmentBlock != null)
            selectedSegmentBlock.setNormalBorder();
        selectedSegmentBlock = segmentBlock;
        selectedSegmentBlock.setSelectedBorder();
    }
}
