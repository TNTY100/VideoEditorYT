package cal.a24.model;

import lombok.Data;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.util.LinkedList;
import java.util.List;

@Data
public class Montage {

    private List<Segment> segmentList;

    public Montage() {
        segmentList = new LinkedList<>();
    }

    public void addSegment(Segment segment) {
        segmentList.add(segment);
    }
}
