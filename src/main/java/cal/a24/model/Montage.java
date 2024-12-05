package cal.a24.model;

import javafx.scene.image.Image;
import lombok.Data;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

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

    public long getDureeTotale() {
        return segments.stream().mapToLong(Segment::getDuree).sum();
    }

    public void export(String fileName, int width, int height, int videoBitrate, int sampleRate, int audioRate) {
        try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(fileName, width, height)) {
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mov,mp4,m4a,3gp,3g2,mj2");
            recorder.setFrameRate(30);
            recorder.setVideoBitrate(videoBitrate);

            // Set audio settings if the input video has audio
            recorder.setAudioChannels(2);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);  // AAC is commonly used
            recorder.setSampleRate(sampleRate);
            recorder.setAudioBitrate(audioRate);

            recorder.start();

            for (Segment segment : segments) {
                segment.startGrab();
                Frame frame;
                while ((frame = segment.grab()) != null) {
                    recorder.record(frame);
                }
            }

            recorder.stop();
        } catch (FrameRecorder.Exception e) {
            throw new RuntimeException(e);
        } catch (FrameGrabber.Exception e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
