package cal.a24.model;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.javacv.*;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;

@Data
public class Segment implements Closeable {

    public static FrameConverter<Image> converter = new JavaFXFrameConverter();

    public static void setConverter(FrameConverter<Image> converter) { // Pour les tests s'il y en a dans le futur???
        Segment.converter = converter;
    }

    public static Image convert(Frame frame) {
        return converter.convert(frame);
    }
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private FFmpegFrameGrabber grabber;

    @Setter(AccessLevel.NONE)
    private long timestampDebut;
    private final long timestampVideoDebut;
    @Setter(AccessLevel.NONE)
    private long timestampFin;
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

        System.out.println("TD" + timestampVideoDebut);
        System.out.println("TF" + timestampVideoFin);
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

    @Override
    public void close() throws IOException {
        grabber.stop();
        grabber.close();
    }

    public Segment setTimestampDebut(long timestampDebut) {
        if (timestampDebut < timestampVideoDebut) {
            throw new RuntimeException("Le timestamp du début ne peux pas être plus petit que 0");
        }
        if (timestampDebut >= timestampFin) {
            throw new RuntimeException("Le timestamp du début ne peux pas venir après celui de la fin du segment.");
        }
        this.timestampDebut = timestampDebut;
        return this;
    }

    public Segment setTimestampFin(long timestampFin) {
        if (timestampFin > timestampVideoFin) {
            throw new RuntimeException("Le timestamp de la fin ne peux pas être placé après le timestamp final de la vidéo.");
        }
        if (timestampFin <= timestampDebut) {
            throw new RuntimeException("Le timestamp de la fin ne peux pas venir avant celui du début du segment.");
        }
        this.timestampFin = timestampFin;
        return this;
    }

    public Image getImageFXAtTimestampInContent(long timestamp) throws FFmpegFrameGrabber.Exception {
        timestamp = timestampDebut + timestamp;
        if (timestamp < 0 || timestamp > timestampFin) {
            throw new RuntimeException("Le timestamp ne fait pas parti de l'intervalle voulue");
        }

        grabber.setVideoTimestamp(timestamp);

        return converter.convert(grabber.grabImage());
    }

    public void startGrab() throws FrameGrabber.Exception {
        grabber.setAudioTimestamp(timestampDebut);
        grabber.setVideoTimestamp(timestampDebut);
    }

    public Frame grab() throws FrameGrabber.Exception {
        if (grabber.getTimestamp() > timestampFin) {
            return null;
        }
        return grabber.grabFrame();
    }

    @Override
    public String toString() {
        return "Segment{" +
                "timestampDebut=" + timestampDebut +
                ", timestampFin=" + timestampFin +
                '}';
    }
}
