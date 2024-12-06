
package cal.a24.frontend;

import cal.a24.model.Montage;
import cal.a24.model.Segment;
import cal.a24.model.Video;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Setter;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class VideoTimeline extends VBox {

    private final ListeLecture listeLecture;
    private final HBox timelineVideo;
    private SegmentBlock selectedSegmentBlock;
    private TimelinePlayer videoViewer;


    public VideoTimeline(ListeLecture listeLecture, TimelinePlayer videoViewer, TimelineCursorContainer timelineCursorContainer) {
        this.videoViewer = videoViewer;
        this.listeLecture = listeLecture;

        setFillWidth(true);
        ScrollPane scrollPane = new ScrollPane();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);


        // Time line avec le curseur
        timelineVideo = new HBox();
        timelineVideo.setFillHeight(false);
        timelineCursorContainer.getChildren().addFirst(timelineVideo);

        // Boutons
        HBox bouttons = new HBox();
        Button addVideoToTimeline = new Button("Ajouter la vidéo");
        Button moveSegmentRight = new Button("Bouger segment à droite");
        Button moveSegmentLeft = new Button("Bouger segment à gauche");
        Button deleteSegment = new Button("Supprimer segment");
        Button couper = new Button("Couper segment");

        bouttons.getChildren().addAll(
                addVideoToTimeline,
                moveSegmentLeft,
                moveSegmentRight,
                deleteSegment,
                couper
        );

        // Déclaration des actions
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
        couper.setOnAction(_ -> couperSegment());

        scrollPane.setContent(timelineCursorContainer);
        this.getChildren().addAll(bouttons, scrollPane);
    }

    private void couperSegment() {
        Montage montage = getMontage().cutSegmentAtTimestamp(videoViewer.getCurrentTimestamp());
        List<SegmentBlock> segmentBlocks = montage.getSegments().stream().map(
                segment -> new SegmentBlock(segment, this)
        ).toList();

        timelineVideo.getChildren().clear();
        timelineVideo.getChildren().addAll(segmentBlocks);
        onChange();
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
        onChange();
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
        onChange();
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
        onChange();
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
        onChange();
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

    @Setter
    Consumer<Montage> onChange;

    private void onChange() {
        onChange(null);
    }

    private void onChange(Montage montage) {
        if (montage == null){
            onChange.accept(
                    getMontage()
            );
        }
        else
            onChange.accept(montage);
    }

    public Montage getMontage() {
        return new Montage(
                timelineVideo.getChildren().stream()
                        .filter(n -> n instanceof SegmentBlock)
                        .map(sb -> ((SegmentBlock) sb).getSegment())
                        .toList()
        );
    }

    @Setter
    Runnable onChangeTime;

    public void onChangeTime() {
        onChangeTime.run();
    }
}
