package androidhough;


import hough.HoughLine;

import java.util.ArrayList;
import java.util.Collections;

public class Hough {
    // The size of the neighbourhood in which to search for other local maxima
    final int neighbourhoodSize = 4;
    // How many discrete values of theta shall we check?
    final int maxTheta = 180;
    // Using maxTheta, work out the step
    final double thetaStep = Math.PI / maxTheta;
    int houghHeight, houghWidth;
    int doubleH;
    int[][] houghArray;
    float centerX, centerY;
    // the number of points that have been added
    int numPoints;
    int width, height;
    // cache of values of sin and cos for different theta values. Has a significant performance improvement.
    private double[] sinCache;
    private double[] cosCache;

    public Hough() {

    }


    void initialiseHough(int w, int h) {

        System.out.println(" initialisation hough ...");
        width = w;
        height = h;
        houghHeight = (int) (Math.sqrt(2) * Math.max(h, w)) / 2;
        doubleH = 2 * houghHeight;
        houghArray = new int[maxTheta][doubleH];
        centerX = w / 2;
        centerY = h / 2;
        numPoints = 0;

        sinCache = new double[maxTheta];
        cosCache = sinCache.clone();
        for (int t = 0; t < maxTheta; t++) {
            double realTheta = t * thetaStep;
            sinCache[t] = Math.sin(realTheta);
            cosCache[t] = Math.cos(realTheta);
        }
    }

    void addPoints(ImageTable imageTable) {
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

    public ArrayList<HoughLine> getLines(int n, int threshold) {
        // Initialise the vector of lines that we'll return
        ArrayList<HoughLine> lines = new ArrayList<>(n);
        // Only proceed if the hough array is not empty
        if (numPoints == 0) return lines;
        // Search for local peaks above threshold to draw
        for (int t = 0; t < maxTheta; t++) {
            loop:
            for (int r = neighbourhoodSize; r < doubleH - neighbourhoodSize; r++) {
                // Only consider points above threshold
                if (houghArray[t][r] > threshold) {

                    int peak = houghArray[t][r];

                    // Check that this peak is indeed the local maxima
                    for (int dx = -neighbourhoodSize; dx <= neighbourhoodSize; dx++) {
                        for (int dy = -neighbourhoodSize; dy <= neighbourhoodSize; dy++) {
                            int dt = t + dx;
                            int dr = r + dy;
                            if (dt < 0) dt = dt + maxTheta;
                            else if (dt >= maxTheta) dt = dt - maxTheta;
                            if (houghArray[dt][dr] > peak) {
                                // found a bigger point nearby, skip
                                continue loop;
                            }
                        }
                    }

                    // calculate the true value of theta
                    double theta = t * thetaStep;
                    // add the line to the vector
                    lines.add(new HoughLine(theta, r, width, height, houghArray[t][r]));
                }
            }
        }
        Collections.sort(lines, Collections.reverseOrder());

        int i=0;
        while (i<lines.size()){
            if (i >= n || lines.get(i).getScore() < 50){
                lines.remove(i);
                i--;
            }
            i++;
        }
        System.out.println("SIZE "+lines.size());

        return lines;
    }
}

