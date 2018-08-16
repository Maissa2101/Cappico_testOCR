package androidhough;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BitmapFactory {

  /*https://stackoverflow.com/questions/10391778/create-a-bufferedimage-from-file-and-make-it-type-int-argb*/
  public static Bitmap decodeFile(String fileUri) {
      BufferedImage in = null;
      try {
          in = ImageIO.read(new File(fileUri));
      } catch (IOException e) {
          e.printStackTrace();
      }
      BufferedImage newImage = new BufferedImage(
              in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = newImage.createGraphics();
      g.drawImage(in, 0, 0, null);
      g.dispose();
      return new Bitmap(newImage);
  }
}
