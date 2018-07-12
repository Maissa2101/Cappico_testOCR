import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.opencv.core.CvType.CV_8UC1;

public class App extends JFrame {

    List<Point> listCenter = new ArrayList<>();
    List<Integer> listRadius = new ArrayList<>();

    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    JFrame frame = new JFrame("App");
    Hough transformHough;
    ImagePanel imagePanel;
    Vector<HoughLine> lines;
    double ecart;
    static final int precision = 20;

    public static void main(String[] args) {
        /** p r o g r a m m e **/
        App app1 = new App();
        App app2 = new App();
        ValidationChar vc = new ValidationChar();
        List<HoughLine> lineEleve = app1.applyDetectionLine(1, "p2");
        System.out.println();
        System.out.println();
        List<HoughLine> lineRef = app2.applyDetectionLine(2, "p");
        double tauxReussite = app1.compareLetter(lineEleve, lineRef);
        System.out.println(ConsoleColor.RED + "Success rate for lines " + tauxReussite + ConsoleColor.RESET);
        app1.applyDetectionCircle(1, "p");
        app2.applyDetectionCircle(2, "p2");

        double tauxReussite2 = compareCircles(app1, app2);
        System.out.println(ConsoleColor.RED + "Success rate for circles " + tauxReussite2 + ConsoleColor.RESET);
        app1.applyAll(1, lineRef);
        app2.applyAll(2, lineEleve);
        System.out.println(ConsoleColor.CYAN + "Global success rate : " + (tauxReussite + tauxReussite2) / 2 + ConsoleColor.RESET);

    }

    BufferedImage skeleton(int index) {
        Mat src = loadImageMat("redecoupage" + index);
        Imgproc.threshold(src, src, 127, 255, Imgproc.THRESH_BINARY);
        Mat skel = new Mat(src.size(), CV_8UC1, Scalar.all(0));
        Mat temp, eroded;
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(3, 3));

        return null;
    }

    List<HoughLine> applyDetectionLine(int index, String namefile) {
        ProcessImage processImage = new ProcessImage();
        BufferedImage im = loadImage(namefile);
        System.out.println("Erosion ...");
        for (int i = 0; i < 10; i++) im = processImage.erosion(im);
        save(im, "testErosion" + index);
        im = processImage.bAndW(im);
        save(im, "testBW" + index);
        im = processImage.formatageIm(im);
        save(im, "redecoupage" + index);
        transformHough = new Hough();
        transformHough.initialiseHough(im.getWidth(), im.getHeight());
        transformHough.addPoints(im);
        List<HoughLine> lines = new ArrayList(transformHough.getLines(10, 32));
        lines = reductionLineSimilar(lines);
        return lines;
    }

    void applyAll(int index, List<HoughLine> lines) {
        Mat src = loadImageMat("redecoupage" + index);
        BufferedImage imLine = Mix(src, lines);
        save(imLine, "imageAll" + index);
    }

    BufferedImage Mix(Mat src, List<HoughLine> lines) {
        HoughCircle elli = new HoughCircle();
        for (int x = 0; x < lines.size(); x++) {
            Point p1 = new Point();
            Point p2 = new Point();
            p1.set(new double[]{lines.get(x).x1, lines.get(x).y1});
            p2.set(new double[]{lines.get(x).x2, lines.get(x).y2});
            Imgproc.line(src, p1, p2, new Scalar(0, 100, 100), 3, 8, 0);
        }
        for (int x = 0; x < listRadius.size(); x++) {
            // circle center
            Imgproc.circle(src, listCenter.get(x), 1, new Scalar(0, 100, 100), 3, 8, 0);
            // circle outline
            Imgproc.circle(src, listCenter.get(x), listRadius.get(x), new Scalar(255, 0, 255), 3, 8, 0);
        }
        BufferedImage im = null;
        try {
            im = elli.Mat2BufferedImage(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return im;
    }

    static double compareCircles(App pM, App pE) {
        int i = 0, j;
        double score = 0;
        double ecartCenter = 0;
        double ecartdiff = 0;

        List<Point> auxCenter = new ArrayList<>(pE.listCenter);
        List<Integer> auxRadius = new ArrayList<>(pE.listRadius);

        while (i < pM.listCenter.size()) {
            j = 0;
            while (j < auxCenter.size()) {
                double distCenter = new HoughCircle().distancePoints(pM.listCenter.get(i), auxCenter.get(j));
                double diffRadius = Math.abs(pM.listRadius.get(i) - auxRadius.get(j));
                if (distCenter <= precision && diffRadius <= precision) {
                    ecartCenter = precision - distCenter;
                    ecartdiff = precision - diffRadius;
                    score += ((1 - ((ecartCenter + ecartdiff) / 100)) * 100);
                    System.out.println("SCORE = " + ((1 - ((ecartCenter + ecartdiff) / 100)) * 100));
                    auxCenter.remove(j);
                    auxRadius.remove(j);
                    j--;
                }
                j++;
            }
            i++;
        }
        System.out.println(ConsoleColor.BLUE + "Ecart for centers \t" + ecartCenter);
        System.out.println("Ecart for radius \t" + ecartdiff + ConsoleColor.RESET);
        if (pM.listCenter.size() > pE.listCenter.size()) {
            double diff = pM.listCenter.size() - pE.listCenter.size();
            System.out.println("Diff supp " + diff + " score = "+ score);
            score = ((score + (20 * diff)) / pM.listCenter.size());
        }
        if (pM.listCenter.size() < pE.listCenter.size()) {
            double diff = pM.listCenter.size() - pE.listCenter.size();
            System.out.println("Diff inf " + diff);
            score = ((score - (20 * diff)) / pM.listCenter.size());
        }
        if (pM.listCenter.size() == pE.listCenter.size()) {
            score = (score / pM.listCenter.size());
        }

        return score;
    }

    void applyDetectionCircle(int index, String namefile) {
        ProcessImage processImage = new ProcessImage();
        BufferedImage im = loadImage(namefile);
        System.out.println("erosion ...");
        for (int i = 0; i < 8; i++) im = processImage.erosion(im);
        save(im, "testErosion" + index);
        im = processImage.formatageImCircle(im);
        save(im, "redecoupage" + index);
        HoughCircle circleHough = new HoughCircle();
        BufferedImage imCircle = circleHough.run("redecoupage" + index, precision);
        this.listRadius = circleHough.listRadius;
        this.listCenter = circleHough.listCenter;

        save(imCircle, "testCircle" + index);
    }

    BufferedImage loadImage(String nameFile) {

        try {
            BufferedImage im;
            if (nameFile != null) {
                System.out.println("Load " + nameFile + "");
                im = ImageIO.read(new File("/home/excilys/capico-java/Cappico_testOCR/picture/" + nameFile + ".png"));
            } else {
                im = ImageIO.read(new File("/home/excilys/capico-java/Cappico_testOCR/picture/.png"));
            }
            return im;
        } catch (IOException e) {
            System.out.println("No file found");
            return null;
        }
    }

    void save(BufferedImage im, String name) {
        File outputfile;
        if (name != null) {
            outputfile = new File(name + ".png");
        } else {
            outputfile = new File("image.png");
        }

        try {
            ImageIO.write(im, "png", outputfile);
        } catch (IOException e) {
            System.out.println("Error in saving the image");
        } finally {
            System.out.println("Saving file ... ");
        }
    }

    @Override
    public void paintComponents(Graphics graphics) {
        super.paintComponents(graphics);
    }

    List<HoughLine> reductionLineSimilar(List<HoughLine> lines) {
        int i = 0, j;
        while (i < lines.size()) {
            j = i + 1;
            double m = (lines.get(i).y2 - lines.get(i).y1) / (lines.get(i).x2 - lines.get(i).x1);
            while (j < lines.size()) {
                double m2 = (lines.get(j).y2 - lines.get(j).y1) / (lines.get(j).x2 - lines.get(j).x1);
                // droite sont horizontale

                if (lines.get(i) != lines.get(j)) {

                    // angle par rapport à l'horizontale
                    double angle = Math.toDegrees(Math.atan((m)));
                    double angle2 = Math.toDegrees(Math.atan((m2)));

                    if (compareAngle(angle, angle2) && compareB(lines.get(i), lines.get(j), m, m2, Math.max(Math.abs(angle), Math.abs(angle2)))) {
                        lines.remove(lines.get(j));
                        j -= 1;
                    }
                }

                j++;

            }
            i++;
        }

        return lines;
    }

    boolean compareB(HoughLine line1, HoughLine line2, double m1, double m2, double angle) {
        if (Double.isInfinite(m1)) {
            m1 = 999;
        }
        if (Double.isInfinite(m2)) {
            m2 = 999;
        }
        double b1 = line1.y1 - m1 * line1.x1;
        double b2 = line2.y1 - m1 * line2.x1;
        double diff = b2 - b1;
        diff = Math.abs(diff);
        if (diff <= precision * (angle)) {
            return true;
        } else {
            return false;
        }
    }

    Mat loadImageMat(String name) {
        String file = "/home/excilys/capico-java/Cappico_testOCR/" + name + ".png";

        Mat src = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

        if (src.empty()) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default "
                    + file + "] \n");
            System.exit(-1);
        }

        return src;
    }

    boolean compareAngle(double angle, double angle2) {
        if (Math.abs(angle - angle2) < precision) {
            ecart = (Math.abs(angle - angle2));
            return true;
        } else {
            if (Math.abs(angle - angle2) > 90 && 180 - Math.abs(angle - angle2) < precision) {
                ecart = (180 - Math.abs(angle - angle2));
                return true;
            } else {
                return false;
            }
        }
    }

    double compareLetter(List<HoughLine> linesP, List<HoughLine> linesE) {
        int i = 0, j;
        double nbValidateLine = 0;

        List<HoughLine> linesRef = new ArrayList<>();
        List<HoughLine> linesTest = new ArrayList<>();

        if (!(linesP.size() > linesE.size())) {
            linesRef.addAll(linesE);
            linesTest.addAll(linesP);
        } else {
            linesRef.addAll(linesP);
            linesTest.addAll(linesE);

        }

        while (i < linesRef.size()) {
            j = 0;
            double m = (linesRef.get(i).y2 - linesRef.get(i).y1) / (linesRef.get(i).x2 - linesRef.get(i).x1);
            while (j < linesTest.size()) {
                double m2 = (linesTest.get(j).y2 - linesTest.get(j).y1) / (linesTest.get(j).x2 - linesTest.get(j).x1);
                double angle = Math.toDegrees(Math.atan((m)));
                double angle2 = Math.toDegrees(Math.atan((m2)));
                if (compareAngle(angle, angle2) && compareB(linesRef.get(i), linesTest.get(j), m, m2, Math.max(Math.abs(angle), Math.abs(angle2)))) {
                    linesRef.remove(linesRef.get(i));
                    linesTest.remove(linesTest.get(j));
                    i--;
                    System.out.println(ConsoleColor.BLUE + "Ecart\t" + (1 - (ecart / 100)));
                    nbValidateLine = (nbValidateLine) + (1 - (ecart / 100));
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }
        double score = 0;
        if (linesP.size() > linesE.size()) {
            double diff = linesP.size() - linesE.size();
            score = (((nbValidateLine * 100) + (10 * diff)) / linesP.size());
        }
        if (linesP.size() < linesE.size()) {
            double diff = linesE.size() - linesP.size();
            score = (((nbValidateLine * 100) - (20 * diff)) / linesP.size());
        }

        if (linesP.size() == linesE.size()) {
            score = ((nbValidateLine * 100) / linesP.size());
        }
        return score;
    }


}