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

        Button addVideoToTimeline = new Button("Ajouter la vidéo");
        Button moveSegmentRight = new Button("Bouger segment à droite");
        Button moveSegmentLeft = new Button("Bouger segment à gauche");
        Button deleteSegment = new Button("Supprimer segment");

        addVideoToTimeline.setStyle("-fx-start-margin: 100px");
        HBox bouttons = new HBox();

        bouttons.getChildren().addAll(
                addVideoToTimeline,
                moveSegmentLeft,
                moveSegmentRight,
                deleteSegment
        );

        timelineVideo = new HBox();
        scrollPane.setContent(timelineVideo);

        addVideoToTimeline.setOnAction(_ -> {
            try {
                addVideoFromListeLecture();
            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        });
        deleteSegment.setOnAction(_ -> removeSelectedSegment());
        moveSegmentRight.setOnAction(_ -> moveSelectedSegmentRight());
        moveSegmentLeft.setOnAction(_ -> moveSelectedSegmentLeft());

        this.getChildren().addAll(bouttons, scrollPane);
    }

    private void moveSelectedSegmentLeft() {
        if (selectedSegmentBlock == null) {
            return;
        }
        int index = timelineVideo.getChildren().indexOf(selectedSegmentBlock);
        if (index - 1 < 0) {
            return;
        }

        timelineVideo.getChildren().remove(selectedSegmentBlock);
        timelineVideo.getChildren().add(index - 1, selectedSegmentBlock);
    }

    private void moveSelectedSegmentRight() {
        if (selectedSegmentBlock == null) {
            return;
        }
        int index = timelineVideo.getChildren().indexOf(selectedSegmentBlock);
        if (index + 1 >= timelineVideo.getChildren().size()) { // ou faire le size - 1... même chose
            return;
        }

        timelineVideo.getChildren().remove(selectedSegmentBlock);
        timelineVideo.getChildren().add(index + 1, selectedSegmentBlock);
    }

    private void removeSelectedSegment() {
        if (selectedSegmentBlock == null) {
            return;
        }
        timelineVideo.getChildren().remove(selectedSegmentBlock);
        try {
            selectedSegmentBlock.getSegment().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        selectedSegmentBlock = null;
        // Encourage le garbage collector parce que beaucoup d'objet sont devenu orphelin. (Non déterministe)
        System.gc();
    }

    private void addVideoFromListeLecture() throws FFmpegFrameGrabber.Exception {
        Video selectedVideo = listeLecture.getSelectedVideo();
        if (selectedVideo == null) {
            return;
        }

        Segment segment = new Segment(selectedVideo.getPATH());
        SegmentBlock segmentBlock = new SegmentBlock(segment, this);

        timelineVideo.getChildren().add(segmentBlock);
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
