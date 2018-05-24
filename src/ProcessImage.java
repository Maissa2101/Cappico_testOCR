import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ProcessImage {

    int[][] matErosion = {{0, 1, 0}, {1, 1, 1}, {0, 1, 0}};

    BufferedImage formatageIm(BufferedImage im) {
        // on recupere le format ( delimite par une boite englobante ) on la rescale centre et voila
        List<Integer> imtmp = new ArrayList<>();

        return im;
    }

    BufferedImage erosion(BufferedImage im) {

        // parcours une fois tous les pixels si on trouve un pixels qui a un pixels voisin vide
        System.out.println("erosion");
        for (int i = 0; i < im.getHeight() - 1; i=i+2) {
            for (int j = 0; j < im.getWidth() - 1; j=j+2) {
                if (i != 0 || j != 0) {
                    if (im.getRGB(j, i) == Color.white.getRGB()) {
                        if (im.getRGB(j - 1, i - 1) == Color.black.getRGB() || im.getRGB(j - 1, i) == Color.black.getRGB() || im.getRGB(j - 1, i + 1) == Color.black.getRGB() || im.getRGB(j, i - 1) == Color.black.getRGB() || im.getRGB(j, i + 1) == Color.black.getRGB() || im.getRGB(j + 1, i - 1) == Color.black.getRGB() || im.getRGB(j + 1, i) == Color.black.getRGB() || im.getRGB(j + 1, i+ 1) == Color.black.getRGB()) {
                            im.setRGB(j, i, Color.black.getRGB());
                        }
                    }
                }
            }
//                   if (im.getRGB(i, j) == Color.white.getRGB()) {
//                       if (im.getRGB(i - 1, j - 1) == Color.black.getRGB() || im.getRGB(i - 1, j) == Color.black.getRGB() || im.getRGB(i - 1, j + 1) == Color.black.getRGB() || im.getRGB(i, j - 1) == Color.black.getRGB() || im.getRGB(i, j + 1) == Color.black.getRGB() || im.getRGB(i + 1, j - 1) == Color.black.getRGB() || im.getRGB(i + 1, j) == Color.black.getRGB() || im.getRGB(i + 1, j + 1) == Color.black.getRGB()) {
//                           im.setRGB(i, j, Color.black.getRGB());
//                       }
//                   }
        }
        return im;
    }

    BufferedImage bAndW(BufferedImage im) {
        System.out.println(" seuillage ...");

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
