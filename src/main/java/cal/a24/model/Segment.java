package cal.a24.model;

import javafx.scene.image.Image;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.bytedeco.javacv.*;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class Segment implements Closeable {
    public static int MAX_TRY_IMAGE_GET = 10;

    public static FrameConverter<Image> converter = new JavaFXFrameConverter();

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

    private void findImageDebut() throws FFmpegFrameGrabber.Exception {
        grabber.setTimestamp(timestampDebut);
        Image imageDebutOg = imageDebut;
        Frame frameStart;
        int tries = 0;
        while (imageDebutOg == imageDebut && tries++ < MAX_TRY_IMAGE_GET) {
            try {
                grabber.setFrameNumber(grabber.getFrameNumber() + 1);
                frameStart = grabber.grabImage();
                imageDebut = convert(frameStart);
            } catch (RuntimeException _) {
            }
        }
    }

    private void findImageFin() throws FFmpegFrameGrabber.Exception {
        grabber.setTimestamp(timestampFin);
        Image imageFinOg = imageFin;
        Frame frameFin;
        int tries = 0;
        while (imageFinOg == imageFin && tries++ < MAX_TRY_IMAGE_GET) {
            try {
                grabber.setFrameNumber(grabber.getFrameNumber() - 1);
                frameFin = grabber.grabImage();
                imageFin = convert(frameFin);
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
        // Évite que l'application (UI) puisse intéragir avec la création de la vidéo.
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
}
