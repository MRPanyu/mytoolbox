package mrpanyu.mytoolbox.tools.image;

import java.awt.image.BufferedImage;

public interface OcrProvider {
	public String callOcr(BufferedImage img) throws Exception;
}
