package cal.a24.frontend;

import cal.a24.model.Segment;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SegmentBlock extends StackPane {
    private static final double BORDER_THICKNESS = 5;
    private static long multiplicateur = 40_000;

    enum SideToResize {
        RIGHT,
        LEFT,
        NONE
    }

    private Segment segment;
    private ImageView imageViewDebut;
    private ImageView imageViewFin;

    private double mouseX, mouseY;
    private SideToResize sideToResize;
    private double widthAtResizeStart;
    private boolean mouseClicked;

    private Rectangle highlightedBorder;
    private Rectangle bgRectangle;

    public SegmentBlock(Segment segment) {
        this.segment = segment;
        setWidth((double) segment.getDuree() / multiplicateur);
        setHeight(150);
        bgRectangle = new Rectangle();
        bgRectangle.setFill(Paint.valueOf("grey"));
        bgRectangle.setHeight(getHeight());
        bgRectangle.setWidth(getWidth());


        imageViewDebut = new ImageView(segment.getImageDebut());
        imageViewDebut.setPreserveRatio(true);
        imageViewDebut.setFitHeight(100);

        imageViewFin = new ImageView(segment.getImageFin());
        imageViewFin.setPreserveRatio(true);
        imageViewFin.setFitHeight(100);

        StackPane.setAlignment(imageViewDebut, Pos.TOP_LEFT);
        StackPane.setAlignment(imageViewFin, Pos.TOP_RIGHT);
        getChildren().addAll(bgRectangle, imageViewDebut, imageViewFin);

        highlightedBorder = new Rectangle(BORDER_THICKNESS, getHeight());
        highlightedBorder.setFill(Paint.valueOf("red"));

        setBorder(Border.stroke(Paint.valueOf("black")));
        setBackground(Background.fill(Paint.valueOf("lightgrey")));
        addResizeListeners();
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
            mouseX = event.getScreenX();
            mouseY = event.getScreenY();
            widthAtResizeStart = getWidth();
            mouseClicked = true;
        });



        setOnMouseDragged(event -> {
            double dx = event.getScreenX() - mouseX;

            if (sideToResize != SideToResize.NONE) {
                double newWidth;
                if (sideToResize == SideToResize.RIGHT){
                    newWidth = Math.min((double) segment.getTimestampVideoFin() / multiplicateur, widthAtResizeStart + dx);
                }
                else
                    newWidth = widthAtResizeStart - dx;

                if (newWidth > 0) {
                    changeWidth(newWidth);
                }
            }
        });

        setOnMouseExited(_ -> {
            if (sideToResize == SideToResize.NONE || mouseClicked){
                return;
            }
            setCursor(Cursor.DEFAULT);
            setSideToResize(SideToResize.NONE);
        });

        setOnMouseReleased(e -> {

            mouseClicked = false;
            setSideToResize(SideToResize.NONE);
        });
    }

    private void setSideToResize(SideToResize sideToResize) {
        if (this.sideToResize == sideToResize)
            return;

        this.sideToResize = sideToResize;

        if (sideToResize == SideToResize.RIGHT) {
            StackPane.setAlignment(highlightedBorder, Pos.TOP_RIGHT);
            getChildren().add(highlightedBorder);
        }
        else if (sideToResize == SideToResize.LEFT) {
            StackPane.setAlignment(highlightedBorder, Pos.TOP_LEFT);
            getChildren().add(highlightedBorder);
        } else {
            getChildren().remove(highlightedBorder);
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

    public void changeWidth(double width) {
        setWidth(width);
        bgRectangle.setWidth(width);
    }
}
