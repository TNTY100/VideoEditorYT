package cal.a24.model;

import javafx.scene.image.Image;
import lombok.Data;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.bytedeco.ffmpeg.global.avutil.AV_SAMPLE_FMT_FLTP;

@Data
public class Montage {
    private ReentrantLock mutex = new ReentrantLock();

    public List<Segment> segments;

    public Montage(List<Segment> segments) {
        setSegments(segments);
    }

    public Image getImageFXAtTimeStamp(long timestamp) {
        System.out.println("getImageFXAtTimeStamp");
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
                // Set video and audio codec settings
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mov,mp4,m4a,3gp,3g2,mj2");
                recorder.setFrameRate(frameRate);
                recorder.setVideoBitrate(videoBitrate);
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);


                // Set audio settings
                recorder.setAudioChannels(2);
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // AAC codec
                recorder.setSampleRate(sampleRate);
                recorder.setAudioBitrate(audioRate);
                // recorder.setSampleFormat(AV_SAMPLE_FMT_FLTP);
                recorder.start();
                System.out.println("Recording Started");

                for (Segment segment : segments) {
                    segment.startGrab();
                    FFmpegFrameGrabber grabber = segment.getGrabber();
                    Frame frame;

                    FFmpegFrameFilter filter = new FFmpegFrameFilter("fps=fps=30",
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
                    Frame capturedFrame;
                    Frame pullFrame;
                    while ((capturedFrame = segment.grab()) != null) {
                        try {
                            if (capturedFrame.image != null || capturedFrame.samples != null) {
                                filter.push(capturedFrame);
                            }
                            if ( (pullFrame = filter.pull()) != null) {
                                if(pullFrame.image != null || pullFrame.samples != null){
                                    recorder.record(pullFrame);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    filter.stop();
                    filter.close();
                    segment.stopGrab();
                }
                System.out.println("Recording ended");
                recorder.stop(); // Stop the recorder after all segments are processed

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error during export: " + e.getMessage());
            }
        } finally {
            mutex.unlock();
        }
    }

    public static Montage copy(Montage montage) {
        List<Segment> list = montage.getSegments().stream().map(segment -> {
            try {
                return new Segment(segment.getVideoPath())
                        .setTimestampDebut(segment.getTimestampDebut())
                        .setTimestampFin(segment.getTimestampFin());
            } catch (FFmpegFrameGrabber.Exception e) {
                throw new RuntimeException(e);
            }
        }).toList();

        return new Montage(list);
    }
}
