package cal.a24.model;

import javafx.scene.image.Image;
import lombok.Data;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class Montage {

    public List<Segment> segments;

    public Montage(List<Segment> segments) {
        setSegments(segments);
    }

    public Image getImageFXAtTimeStamp(long timestamp) {
        if (timestamp < 0 || timestamp > getDureeTotale()) {
            throw new RuntimeException("Le timestamp est à l'extérieur des limites");
        }

        for (Segment segment: segments) {
            if (timestamp < segment.getDuree()) {
                return segment.getImageFXAtTimestampInContent(timestamp);
            }
            timestamp -= segment.getDuree();
        }
        return null;
    }

    public Montage cutSegmentAtTimestamp(long timestamp) {
        if (timestamp < 0 || timestamp > getDureeTotale()) {
            throw new RuntimeException("Le timestamp est à l'extérieur des limites");
        }
        List<Segment> newSegments = new ArrayList<>();


        for (Segment segment: segments) {
            if (timestamp < segment.getDuree() && timestamp > 0) {
                newSegments.addAll(segment.splitAtTimestamp(timestamp));
            }
            else {
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
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileName, width, height)) {
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mov,mp4,m4a,3gp,3g2,mj2");
            recorder.setFrameRate(frameRate);
            recorder.setVideoBitrate(videoBitrate);

            // Set audio settings if the input video has audio
            recorder.setAudioChannels(2);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // AAC is commonly used
            recorder.setSampleRate(sampleRate);
            recorder.setAudioBitrate(audioRate);

            recorder.start();
            System.out.println("Recording Started");

            for (Segment segment : segments) {
                System.out.println("Segment Started");
                FFmpegFrameGrabber grabber = segment.getGrabber();

                FFmpegFrameFilter filter = new FFmpegFrameFilter("fps=fps=" + frameRate,
                        "anull",
                        grabber.getImageWidth(),
                        grabber.getImageHeight(), grabber.getAudioChannels());
                filter.setSampleFormat(grabber.getSampleFormat()); // Vlaue is 1
                filter.setSampleRate(grabber.getSampleRate());
                filter.setPixelFormat(grabber.getPixelFormat());
                filter.setFrameRate(grabber.getFrameRate());
                filter.setSampleFormat(grabber.getSampleFormat());
                filter.start();

                segment.startGrab();
                Frame capturedFrame;
                Frame pullFrame;
                while (true) {
                    try {
                        capturedFrame = segment.grab();
                        if (capturedFrame == null) {
                            System.out.println("ERREUR");
                            break;
                        }

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
            }
            System.out.println("Recording ended");
            recorder.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
