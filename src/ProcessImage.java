import org.opencv.core.Mat;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import static javax.swing.text.StyleConstants.Size;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.getStructuringElement;

public class ProcessImage {

    int[][] matErosion = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
    int maxX, maxY, minY, minX;
    int maxXC, maxYC, minYC, minXC;
    public static int myColor(int r, int g, int b) {
        int argb = 0xFF << 24 | r << 16 | g << 8 | b;
        return argb;
    }

    BufferedImage formatageIm(BufferedImage im) {
        // on recupere le format ( delimite par une boite englobante ) on la rescale centre et voila
        BufferedImage imchar;
        boiteEnglobante(im);
        imchar = decoupage(im);

        int w = 300;
        int h = 300;
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        double wScale = (double) w/imchar.getWidth();
        double hScale = (double) h/imchar.getHeight();
        System.out.println();
        at.scale(wScale, hScale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(imchar, after);
        return after;
    }
    BufferedImage formatageImCircle(BufferedImage im) {
        // on recupere le format ( delimite par une boite englobante ) on la rescale centre et voila
        BufferedImage imchar;
        boiteEnglobanteCircle(im);
        imchar = decoupageCircle(im);

        int w = 300;
        int h = 300;
        BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = new AffineTransform();
        double wScale = (double) w/imchar.getWidth();
        double hScale = (double) h/imchar.getHeight();
        System.out.println();
        at.scale(wScale, hScale);
        AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(imchar, after);
        return after;
    }

    BufferedImage decoupage(BufferedImage im) {
        BufferedImage imchar = new BufferedImage(maxX - minX, maxY - minY, im.getType());
        for (int i = 0; i < imchar.getWidth(); i++) {
            for (int j = 0; j < imchar.getHeight(); j++) {
                imchar.setRGB(i, j, im.getRGB(i + minX, j + minY));

            }
        }
        return imchar;
    }
    BufferedImage decoupageCircle(BufferedImage im) {
        System.out.println("min XC"+minXC+ " minYC"+minYC);
        BufferedImage imchar = new BufferedImage(maxXC - minXC, maxYC - minYC, im.getType());
        for (int i = 0; i < imchar.getWidth(); i++) {
            for (int j = 0; j < imchar.getHeight(); j++) {
                imchar.setRGB(i, j, im.getRGB(i + minXC, j + minYC));

            }
        }
        return imchar;
    }
    void boiteEnglobante(BufferedImage im) {
        minX = im.getWidth();
        minY = im.getHeight();
        for (int i = 0; i < im.getWidth(); i++) {
            for (int j = 0; j < im.getHeight(); j++) {
                if (im.getRGB(i, j) == Color.white.getRGB()) {
                    if (maxX < i) {
                        maxX = i;
                    }
                    if (maxY < j) {
                        maxY = j;
                    }
                    if (minX > i) {
                        minX = i;
                    }
                    if (minY > j) {
                        minY = j;
                    }
                }
            }
        }
        System.out.println(minX + " " + minY + " " + maxX + " " + maxY);
    }
    void boiteEnglobanteCircle(BufferedImage im) {
        minXC = im.getWidth();
        minYC = im.getHeight();
        for (int i = 0; i < im.getWidth(); i++) {
            for (int j = 0; j < im.getHeight(); j++) {
                if (im.getRGB(i, j) == Color.black.getRGB()) {
                    if (maxXC < i) {
                        maxXC = i;
                    }
                    if (maxYC < j) {
                        maxYC = j;
                    }
                    if (minXC > i) {
                        minXC = i;
                    }
                    if (minYC > j) {
                        minYC = j;
                    }
                }
            }
        }
    }

    BufferedImage erosion(BufferedImage im) {
        // parcours une fois tous les pixels si on trouve un pixels qui a un pixels voisin vide
        Kernel kernelErosion = new Kernel(2, 2, new float[]{1, 1, 1, 1});
        ConvolveOp convol = new ConvolveOp(kernelErosion, ConvolveOp.EDGE_NO_OP, null);
        BufferedImage imEro = convol.filter(im, null);
        return imEro;
    }

    BufferedImage bAndW(BufferedImage im) {
        System.out.println("Thresholding ...");

        for (int i = 0; i < im.getWidth(); i++) {
            for (int j = 0; j < im.getHeight(); j++) {
                if (im.getRGB(i, j) < -1) {
                    im.setRGB(i, j, Color.white.getRGB());
                } else {
                    im.setRGB(i, j, Color.black.getRGB());
                }

            }
        }
        return im;
    }
}