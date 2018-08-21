package androidhough.houghtest;

import androidhough.Bitmap;
import androidhough.BitmapFactory;
import androidhough.BitmapUtils;
import androidhough.ImageTable;
import org.opencv.core.Mat;

import java.awt.image.BufferedImage;

import static org.opencv.core.CvType.*;

public class AccumulationMatrixExtractor {
    // How many discrete values of theta shall we check?
    final int maxTheta = 9;
    // Using maxTheta, work out the step
    final double thetaStep = Math.PI / maxTheta;
    int houghHeight;
    int doubleH;
    int[][] houghArray;
    float centerX, centerY;
    // the number of points that have been added
    int numPoints;
    int width, height;
    // cache of values of sin and cos for different theta values. Has a significant performance improvement.
    private double[] sinCache;
    private double[] cosCache;
    private ImageTable imageTable;

    public AccumulationMatrixExtractor(ImageTable imageTable) {

        System.out.println(" initialisation hough ...");
        this.imageTable = imageTable;
        width = imageTable.getWidth();
        height = imageTable.getHeight();
        houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2;
        doubleH = 2 * houghHeight;
        houghArray = new int[maxTheta][doubleH];
        centerX = width / 2;
        centerY = height / 2;
        numPoints = 0;

        sinCache = new double[maxTheta];
        cosCache = sinCache.clone();
        for (int t = 0; t < maxTheta; t++) {
            double realTheta = t * thetaStep;
            sinCache[t] = Math.sin(realTheta);
            cosCache[t] = Math.cos(realTheta);
        }
        scan();
    }


    public static Mat getAccumulationMatrix(String filePath) {

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = BitmapUtils.resizeKeepRatio(bitmap,16,16);

        ImageTable imageTable = new ImageTable(bitmap.getWidth(),bitmap.getHeight());
        bitmap.getPixels(imageTable.getImage(),0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());

        AccumulationMatrixExtractor acc = new AccumulationMatrixExtractor(imageTable);
        Mat m = new Mat(acc.maxTheta, acc.doubleH, CV_32S);
        for(int i = 0; i < acc.houghArray.length; i++) {
            m.put(i, 0, acc.houghArray[i]);
        }
        return m;
    }

    /**
     * Parcours du l'image avec création de la matrice d'accumulation.
     */
    void scan() {
        System.out.println(" ajout des points  .... ");

        width = imageTable.getWidth();
        height = imageTable.getHeight();

        for (int x = 0; x < imageTable.getWidth(); x++) {
            for (int y = 0; y < imageTable.getHeight(); y++) {
                if ((imageTable.getImage()[x + y * imageTable.getWidth()] & 0x000000FF) != 0) {
                    addPoint(x, y);
                }
            }
        }
    }

    void addPoint(int x, int y) {
        for (int t = 0; t < maxTheta; t++) {

            int r = (int) (((x - centerX) * cosCache[t]) + ((y - centerY) * sinCache[t]));
            r += houghHeight;
            if (r < 0 || r >= doubleH) {
                continue;
            }
            houghArray[t][r]++;
        }
        numPoints++;
    }
}
