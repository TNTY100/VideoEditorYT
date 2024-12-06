package cal.a24.model;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bytedeco.javacv.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class Segment implements Closeable {

    public static FrameConverter<Image> converter = new JavaFXFrameConverter();

    public static void setConverter(FrameConverter<Image> converter) { // Pour les tests s'il y en a dans le futur???
        Segment.converter = converter;
    }

    private static ReentrantLock mutexStatic = new ReentrantLock();

    public static Image convert(Frame frame) {
        try {
            mutexStatic.lock();

            return converter.convert(frame);
        }
        finally {
            mutexStatic.unlock();
        }

    }

    @Setter(AccessLevel.NONE)
    private FFmpegFrameGrabber grabber;

    private final String videoPath;

    @Setter(AccessLevel.NONE)
    private long timestampDebut;
    private final long timestampVideoDebut;
    @Setter(AccessLevel.NONE)
    private long timestampFin;
    private final long timestampVideoFin;

    private Image imageDebut;
    private Image imageFin;

    private ReentrantLock mutex = new ReentrantLock();


    public Segment(String pathVideo) throws FFmpegFrameGrabber.Exception {
        this.videoPath = pathVideo;
        grabber = new FFmpegFrameGrabber(this.videoPath);
        grabber.start();
        timestampVideoDebut = 0;
        timestampDebut = timestampVideoDebut;
        timestampVideoFin = grabber.getLengthInTime();
        timestampFin = timestampVideoFin;
        setupImages();
    }

    private void setupImages() throws FFmpegFrameGrabber.Exception {
        findImageDebut();
        findImageFin();
    }

    private void findImageFin() throws FFmpegFrameGrabber.Exception {
        grabber.setTimestamp(timestampFin);
        Image imageFinOg = imageFin;
        Frame frameFin;
        while (imageFinOg == imageFin) {
            try {
                grabber.setFrameNumber(grabber.getFrameNumber() - 1);
                frameFin = grabber.grabImage();
                imageFin = convert(frameFin);
            } catch (RuntimeException _) {
            }
        }
    }

    private void findImageDebut() throws FFmpegFrameGrabber.Exception {
        grabber.setTimestamp(timestampDebut);
        Image imageDebutOg = imageDebut;
        Frame frameStart;
        while (imageDebutOg == imageDebut) {
            try {
                grabber.setFrameNumber(grabber.getFrameNumber() + 1);
                frameStart = grabber.grabImage();
                imageDebut = convert(frameStart);
            } catch (RuntimeException _) {
            }
        }
    }

    public long getDuree() {
        return timestampFin - timestampDebut;
    }

    @Override
    public void close() throws IOException {
        try {
            mutex.lock();
            grabber.stop();
            grabber.close();
        } finally {
            mutex.unlock();
        }
    }

    public Segment setTimestampDebut(long timestampDebut) {
        if (timestampDebut < timestampVideoDebut) {
            throw new RuntimeException("Le timestamp du début ne peux pas être plus petit que 0");
        }
        if (timestampDebut >= timestampFin) {
            throw new RuntimeException("Le timestamp du début ne peux pas venir après celui de la fin du segment.");
        }
        this.timestampDebut = timestampDebut;

        try {
            findImageDebut();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
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

        try {
            findImageFin();
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Image getImageFXAtTimestampInContent(long timestamp) {
        try {
            mutex.lock();
            timestamp = timestampDebut + timestamp;
            if (timestamp < 0 || timestamp > timestampFin) {
                throw new RuntimeException("Le timestamp ne fait pas parti de l'intervalle voulue");
            }

            grabber.setVideoTimestamp(timestamp);

            return converter.convert(grabber.grabImage());
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException("Le grabber était déjà fermé");
        } finally {
            mutex.unlock();
        }
    }

    public void startGrab() throws FrameGrabber.Exception {
        mutex.lock();
        grabber.setAudioTimestamp(timestampDebut);
        grabber.setVideoTimestamp(timestampDebut);
    }

    public Frame grab() throws FrameGrabber.Exception {
        if (grabber.getTimestamp() > timestampFin) {
            return null;
        }
        return grabber.grabFrame();
    }
    public void stopGrab() {
        mutex.unlock();
    }

    @Override
    public String toString() {
        return "Segment{" +
                "timestampDebut=" + timestampDebut +
                ", timestampFin=" + timestampFin +
                '}';
    }

    public List<Segment> splitAtTimestamp(long timestamp) {
        try {
            timestamp += timestampDebut;
            Segment e2 = new Segment(videoPath)
                    .setTimestampFin(timestampFin)
                    .setTimestampDebut(timestamp + 1);
            this.setTimestampFin(timestamp);
            return List.of(
                    this,
                    e2
            );
        } catch (FFmpegFrameGrabber.Exception e) {
            throw new RuntimeException(e);
        }
    }
}
