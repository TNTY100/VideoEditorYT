package cal.a24.model;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameConverter;
import org.bytedeco.javacv.JavaFXFrameConverter;

@Data
public class Segment {

    public static FrameConverter<Image> converter = new JavaFXFrameConverter();

    public static void setConverter(FrameConverter<Image> converter) {
        Segment.converter = converter;
    }

    public static Image convert(Frame frame) {
        return converter.convert(frame);
    }
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private FFmpegFrameGrabber grabber;

    private long timestampDebut;
    @Setter(AccessLevel.NONE)
    private final long timestampVideoDebut;
    private long timestampFin;
    @Setter(AccessLevel.NONE)
    private final long timestampVideoFin;

    private Image imageDebut;
    private Image imageFin;


    public Segment(String pathVideo) throws FFmpegFrameGrabber.Exception {
        grabber = new FFmpegFrameGrabber(pathVideo);
        grabber.start();
        timestampVideoDebut = 0;
        timestampDebut = timestampVideoDebut;
        timestampVideoFin = grabber.getLengthInTime();
        timestampFin = timestampVideoFin;
        setupImages();
    }

    private void setupImages() throws FFmpegFrameGrabber.Exception {
        grabber.setTimestamp(timestampDebut);
        imageDebut = convert(grabber.grabImage());
        grabber.setTimestamp(timestampFin);
        imageFin = convert(grabber.grabImage());
    }

    public long getDuree() {
        return timestampFin - timestampDebut;
    }
}
