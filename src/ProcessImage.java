import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ProcessImage {

    int[][] matErosion = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
    int maxX, maxY, minY, minX;

    BufferedImage formatageIm(BufferedImage im) {
        // on recupere le format ( delimite par une boite englobante ) on la rescale centre et voila
        BufferedImage imchar ;
        boiteEnglobante(im);
       imchar = decoupage(im);
        return imchar;
    }

    BufferedImage decoupage(BufferedImage im){
        BufferedImage imchar = new  BufferedImage(maxX-minX,maxY-minY,im.getType());
        for(int i=minX;i<maxX;i++){
            for(int j=minY;j<maxY;j++){
                if(im.getRGB(i,j)==Color.white.getRGB()){
//                    int color = im.getRGB(i,j);
//                    int alpha = (color >>> 24) & 0xff;
//                    int r = (color >> 16) & 0xff;
//                    int g = (color >> 8) & 0xff ;
//                    int b = color & 0xff;
//
                    imchar.setRGB(i,j,im.getRGB(i,j));
                }
            }
        }
        return imchar;
    }

    void boiteEnglobante(BufferedImage im) {
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
    }

    BufferedImage erosion(BufferedImage im) {

        // parcours une fois tous les pixels si on trouve un pixels qui a un pixels voisin vide
       // System.out.println("erosion ...");
        Kernel kernelErosion = new Kernel(2,2, new float[]{1,1,1,1});
        ConvolveOp convol= new ConvolveOp(kernelErosion);
        BufferedImage imEro= convol.filter(im,null);
        boolean test =false;
        return imEro;
    }

    public static int myColor(int r, int g, int b) {
        int argb = 0xFF << 24 | r << 16 | g << 8 | b;
        return argb;
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
