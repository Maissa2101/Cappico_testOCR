package androidhough;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public final class BitmapUtils {
    private BitmapUtils() {
    }

    public static Bitmap resizeKeepRatio(Bitmap formatedImage, int w, int h) {
        BufferedImage imchar = formatedImage.getBufferedImage();
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        double wScale = (double) w/imchar.getWidth();
        double hScale = (double) h/imchar.getHeight();
        System.out.println();
        at.scale(wScale, hScale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(imchar, after);
        return new Bitmap(after);
    }
}
