package cal.a24.model;

import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Video {
    private String PATH;
    private Image imageFX;
    private long longeur;
}
