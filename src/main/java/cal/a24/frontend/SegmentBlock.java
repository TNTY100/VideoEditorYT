package cal.a24.frontend;

import cal.a24.model.Segment;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentBlock extends StackPane {
    private static final double BORDER_THICKNESS = 5;
    private static long multiplicateur = 40_000;
    private static final long IMAGE_MAX_WIDTH = 200;

    enum SideToResize {
        RIGHT,
        LEFT,
        NONE
    }

    private final VideoTimeline videoTimeline;

    private Segment segment;
    private ImageView imageViewDebut;
    private ImageView imageViewFin;

    private double mouseX, mouseY;
    private boolean mouseClicked;
    private SideToResize sideToResize;
    private long widthAtResizeStart;
    private long widthLong;

    private Rectangle highlightedBorder;
    private Rectangle bgRectangle;

    public SegmentBlock(Segment segment, VideoTimeline videoTimeline) {
        this.videoTimeline = videoTimeline;
        this.segment = segment;

        widthLong = segment.getDuree();
        setHeight(150);

        bgRectangle = new Rectangle();
        bgRectangle.setFill(Paint.valueOf("grey"));
        bgRectangle.setHeight(getHeight());
        bgRectangle.setWidth(getWidth());


        imageViewDebut = new ImageView(segment.getImageDebut());
        imageViewDebut.setPreserveRatio(true);
        imageViewDebut.setFitWidth(IMAGE_MAX_WIDTH);

        imageViewFin = new ImageView(segment.getImageFin());
        imageViewFin.setPreserveRatio(true);
        imageViewFin.setFitWidth(IMAGE_MAX_WIDTH);

        StackPane.setAlignment(imageViewDebut, Pos.TOP_LEFT);
        StackPane.setAlignment(imageViewFin, Pos.TOP_RIGHT);
        getChildren().addAll(bgRectangle, imageViewDebut, imageViewFin);

        highlightedBorder = new Rectangle(BORDER_THICKNESS, getHeight());
        highlightedBorder.setFill(Paint.valueOf("red"));

        setBackground(Background.fill(Paint.valueOf("lightgrey")));
        setNormalBorder();
        addResizeListeners();
        changeWidth(widthLong);
    }

    private void addResizeListeners() {

        setOnMouseMoved(event -> {
            if (mouseClicked)
                return;

            setSideToResize(getResizableBorder(event));

            if (sideToResize == SideToResize.NONE) {
                setCursor(Cursor.DEFAULT);
            } else {
                setCursor(Cursor.H_RESIZE);
            }
        });

        setOnMousePressed(event -> {
            if (event.isControlDown()) {
                sideToResize = SideToResize.NONE;
                return;
            }
            if (sideToResize == SideToResize.NONE) {
                videoTimeline.setSelectedSegment(this);
                return;
            }

            mouseX = event.getScreenX();
            mouseY = event.getScreenY();
            widthAtResizeStart = widthLong;
            mouseClicked = true;
        });



        setOnMouseDragged(event -> {
            long dx = (long) ((event.getScreenX() - mouseX) * multiplicateur);

            if (sideToResize != SideToResize.NONE) {
                long newWidth;
                if (sideToResize == SideToResize.RIGHT){
                    newWidth = calculateMaxWidthFromRight(dx);
                }
                else {
                    newWidth = calculateMaxWidthFromLeft(dx);
                }
                if (newWidth > 0) {
                    changeWidth(newWidth);
                }

                imageViewFin.setFitWidth(Math.min(IMAGE_MAX_WIDTH, bgRectangle.getWidth()));
                imageViewDebut.setFitWidth(Math.min(IMAGE_MAX_WIDTH, bgRectangle.getWidth()));
            }
        });

        setOnMouseExited(_ -> {
            if (sideToResize == SideToResize.NONE || mouseClicked){
                return;
            }
            setCursor(Cursor.DEFAULT);
            setSideToResize(SideToResize.NONE);
        });

        setOnMouseReleased(_ -> {
            if (sideToResize == SideToResize.NONE){
                return;
            }

            if (sideToResize == SideToResize.RIGHT) {
                long differance = widthLong - widthAtResizeStart;

                long nouveauTimestampFin = segment.getTimestampFin() +  differance;

                try {
                    segment.setTimestampFin(nouveauTimestampFin);
                    imageViewFin.setImage(segment.getImageFin());
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                    // S'il y a une erreur on doit rollback.
                    changeWidth(widthAtResizeStart);
                }
            }
            else {
                long differance = widthLong - widthAtResizeStart;

                long nouveauTimestampDebut = segment.getTimestampDebut() -  differance;

                try {
                    segment.setTimestampDebut(nouveauTimestampDebut);
                    imageViewDebut.setImage(segment.getImageDebut());
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                    // S'il y a une erreur on doit rollback.
                    changeWidth(widthAtResizeStart);
                }
            }
            System.out.println(segment);
            mouseClicked = false;
            videoTimeline.onChangeTime();
            setSideToResize(SideToResize.NONE);
        });
    }

    private void checkImageOverFlow() {
        if (bgRectangle.getWidth() < IMAGE_MAX_WIDTH){
            imageViewDebut.setFitWidth(bgRectangle.getWidth());
            imageViewFin.setFitWidth(bgRectangle.getWidth());
        }
    }

    private void setSideToResize(SideToResize sideToResize) {
        if (this.sideToResize == sideToResize)
            return;

        this.sideToResize = sideToResize;
        getChildren().remove(highlightedBorder);

        if (sideToResize == SideToResize.RIGHT) {
            StackPane.setAlignment(highlightedBorder, Pos.TOP_RIGHT);
            getChildren().add(highlightedBorder);
        }
        else if (sideToResize == SideToResize.LEFT) {
            StackPane.setAlignment(highlightedBorder, Pos.TOP_LEFT);
            getChildren().add(highlightedBorder);
        }
    }

    private SideToResize getResizableBorder(MouseEvent event) {
        if (event.getX() >= getWidth() - BORDER_THICKNESS) {
            return SideToResize.RIGHT;
        }
        else if (event.getX() <= BORDER_THICKNESS) {
            return SideToResize.LEFT;
        }
        else {
            return SideToResize.NONE;
        }
    }

    public void changeWidth(long width) {
        widthLong = width;
        setWidth((double) width/multiplicateur);
        bgRectangle.setWidth((double) width/multiplicateur);
        checkImageOverFlow();
    }

    public void setNormalBorder() {
        setBorder(Border.stroke(Paint.valueOf("black")));
    }

    public void setSelectedBorder() {
        setBorder(Border.stroke(Paint.valueOf("red")));
    }

    private long calculateMaxWidthFromLeft(long dx) {
        return Math.min(segment.getTimestampFin(), widthAtResizeStart - dx);
    }

    public long calculateMaxWidthFromRight(long dx) {
        return Math.min(segment.getTimestampVideoFin() - segment.getTimestampDebut(), widthAtResizeStart + dx);
    }
}
