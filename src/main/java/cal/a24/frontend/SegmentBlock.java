package cal.a24.frontend;

import cal.a24.model.Segment;
import javafx.scene.Cursor;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import lombok.Data;

@Data
public class SegmentBlock extends Pane {
    private static final double BORDER_THICKNESS = 5;
    private static long multiplicateur = 40_000;

    private Segment segment;
    private ImageView imageViewDebut;
    private ImageView imageViewFin;
    private double mouseX, mouseY;

    public SegmentBlock(Segment segment) {
        this.segment = segment;
        setWidth((double) segment.getDuree() / multiplicateur);
        setHeight(150);

        imageViewDebut = new ImageView(segment.getImageDebut());
        imageViewDebut.setPreserveRatio(true);
        imageViewDebut.setFitHeight(100);

        imageViewFin = new ImageView(segment.getImageFin());
        imageViewFin.setPreserveRatio(true);
        imageViewFin.setFitHeight(100);

        imageViewFin.setX(getWidth() - imageViewFin.getFitWidth());

        getChildren().addAll(imageViewDebut, imageViewFin);

        setBorder(Border.stroke(Paint.valueOf("black")));
        setBackground(Background.fill(Paint.valueOf("grey")));
        // addResizeListeners();
    }
}
