package cal.a24.model;

import javafx.scene.image.Image;
import lombok.Data;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Data
public class Montage {
    private ReentrantLock mutex = new ReentrantLock();

    public List<Segment> segments;

    public Montage(List<Segment> segments) {
        setSegments(segments);
    }

    public Image getImageFXAtTimeStamp(long timestamp) {
        try {
            mutex.lock();
            if (timestamp < 0 || timestamp > getDureeTotale()) {
                throw new RuntimeException("Le timestamp est à l'extérieur des limites");
            }

            for (Segment segment : segments) {
                if (timestamp < segment.getDuree()) {
                    return segment.getImageFXAtTimestampInContent(timestamp);
                }
                timestamp -= segment.getDuree();
            }
            return null;
        } finally {
            mutex.unlock();
        }

    }

    public Montage cutSegmentAtTimestamp(long timestamp) {
        if (timestamp < 0 || timestamp > getDureeTotale()) {
            throw new RuntimeException("Le timestamp est à l'extérieur des limites");
        }
        List<Segment> newSegments = new ArrayList<>();


        for (Segment segment : segments) {
            if (timestamp < segment.getDuree() && timestamp > 0) {
                newSegments.addAll(segment.splitAtTimestamp(timestamp));
            } else {
                newSegments.add(segment);
            }
            timestamp -= segment.getDuree();
        }
        return new Montage(newSegments);
    }

    public long getDureeTotale() {
        return segments.stream().mapToLong(Segment::getDuree).sum();
    }

    public void export(String fileName, int width, int height, int frameRate, int videoBitrate, int sampleRate, int audioRate) {
        try {
            mutex.lock();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileName, width, height)) {
                // setup du recorder (vidéo)
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mov,mp4,m4a,3gp,3g2,mj2");
                recorder.setFrameRate(frameRate);
                recorder.setVideoBitrate(videoBitrate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);


                // setup du recorder (audio)
                recorder.setAudioChannels(2);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // AAC codec
                recorder.setSampleRate(sampleRate);
                recorder.setAudioBitrate(audioRate);
                recorder.start();

                for (Segment segment : segments) {
                    // Démarrage de la prise d'information.
                    segment.startGrab();
                    // Création du filtre
                    FFmpegFrameFilter filter = createFrameFilter(frameRate, segment);

                    // Capture du segment
                    Frame capturedFrame;
                    Frame pullFrame;
                    while ((capturedFrame = segment.grab()) != null) {
                        try {
                            if (capturedFrame.image != null || capturedFrame.samples != null) {
                                filter.push(capturedFrame);
                            }
                            if ((pullFrame = filter.pull()) != null) {
                                if (pullFrame.image != null || pullFrame.samples != null) {
                                    recorder.record(pullFrame);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // Arrêt de la capture du segment
                    filter.stop();
                    filter.close();
                    segment.stopGrab();
                }
                // Arrêt de la capture.
                recorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error during export: " + e.getMessage());
            }
        } finally {
            mutex.unlock();
        }
    }

    private static FFmpegFrameFilter createFrameFilter(int frameRate, Segment segment) throws FFmpegFrameFilter.Exception {
        // Le filtre cherche à harmoniser les FPS entre le grabber et le recorder
        FFmpegFrameGrabber grabber = segment.getGrabber();
        FFmpegFrameFilter filter = new FFmpegFrameFilter("fps=fps=" + frameRate,
                "anull",
                grabber.getImageWidth(),
                grabber.getImageHeight(), grabber.getAudioChannels());
        filter.setSampleFormat(grabber.getSampleFormat());
        filter.setSampleRate(grabber.getSampleRate());
        filter.setPixelFormat(grabber.getPixelFormat());
        filter.setFrameRate(grabber.getFrameRate());
        filter.setSampleRate(grabber.getSampleRate());
        filter.setSampleFormat(grabber.getSampleFormat());
        filter.start();
        return filter;
    }
}
