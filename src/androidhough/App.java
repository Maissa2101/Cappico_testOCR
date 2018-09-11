package androidhough;

import hough.HoughLine;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.List;

public class App {
    private App(){}

    private static final String CLASS_NAME = "App";
    public final static int nbLinesToDetect = 10;
    private static int threshold = 5;
    private static int precision = 20;

    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static List<HoughLine> applyDetection(Bitmap im) {

        int width = im.getWidth();
        int height = im.getHeight();
        ImageTable imageTable = new ImageTable(width,height);
        im.getPixels(imageTable.getImage(),0,width,0,0,width,height);

        imageTable = ProcessImage.bAndW(imageTable);
        imageTable = ProcessImage.formatageIm(imageTable);
        imageTable = ProcessImage.erosion(imageTable);

        save(Bitmap.createBitmap(imageTable.getImage(),imageTable.getWidth(),imageTable.getHeight(),
                Bitmap.Config.ARGB_8888), System.getProperty("java.io.tmpdir")+"/testErosionfinal.jpg");
        Mat source = Imgcodecs.imread(System.getProperty("java.io.tmpdir")+"/testErosionfinal.jpg");
        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3,3), new Point(1,1));
        Imgproc.erode(source,source,kernel);
        Imgcodecs.imwrite(System.getProperty("java.io.tmpdir")+"/aprestestErosion.jpg",source);



        Hough transformHough = new Hough();
        transformHough.initialiseHough(imageTable.getWidth(), imageTable.getHeight());
        transformHough.addPoints(imageTable);
        List<HoughLine> lines = new ArrayList<>(transformHough.getLines(nbLinesToDetect, threshold));
        similarLinesReduction(lines);

        save(Bitmap.createBitmap(imageTable.getImage(),imageTable.getWidth(),
                imageTable.getHeight(), Bitmap.Config.ARGB_8888),"resultat.png");

        System.out.println(CLASS_NAME + " nb= lines " + lines.size());
        System.out.println(CLASS_NAME + "num points :" + transformHough.numPoints);
        return lines;
    }

    // TODO : at the end : return boolean
    public static double validationMaj(List<HoughLine> linesP, List<HoughLine> linesM){
        double pourcentageValidate = compareLetters(linesP, linesM);
        System.out.println(CLASS_NAME + " resulats est " + pourcentageValidate );
        return pourcentageValidate;
//        return pourcentageValidate >= 100-precision;
    }


    static void save(Bitmap im, String name) {
        FileOutputStream outputfile = null;
        if (name != null) {
            try {
                outputfile = new FileOutputStream(name);
            } catch (FileNotFoundException e) {
                System.out.println("APP" + "file cannot be opened");
            }
        } else {
            try {
                outputfile = new FileOutputStream("image.png");
            } catch (FileNotFoundException e) {
                System.out.println("APP" + "file cannot be opened");
            }
        }
        if (outputfile != null) {
            im.compress(Bitmap.CompressFormat.PNG, 100, outputfile);
            System.out.println(CLASS_NAME + " save file ... ");
        }

    }


    private static void similarLinesReduction(List<HoughLine> lines) {
        int i = 0;
        while (i < lines.size()) {
            int j = i + 1;
            double m = (lines.get(i).y2 - lines.get(i).y1) / (lines.get(i).x2 - lines.get(i).x1);
            while (j < lines.size()) {
                double m2 = (lines.get(j).y2 - lines.get(j).y1) / (lines.get(j).x2 - lines.get(j).x1);

                if (lines.get(i) != lines.get(j)) {
                    // angle par rapport à l'horizontale
                    double angle = Math.toDegrees(Math.atan((m)));
                    double angle2 = Math.toDegrees(Math.atan((m2)));

                    if (compareAngle(angle, angle2) != -1.0 &&
                            compareB(lines.get(i), lines.get(j), m, m2, Math.max(Math.abs(angle), Math.abs(angle2)))) {
                        lines.remove(lines.get(j));
                        j--;
                    }
                }
                j++;
            }
            i++;
        }
    }


    private static boolean compareB(HoughLine line1, HoughLine line2, double m1, double m2, double angle) {
        if (Double.isInfinite(m1)) {
            m1 = 999;
        }
        double b1 = line1.y1 - m1 * line1.x1;
        double b2 = line2.y1 - m1 * line2.x1;
        double diff = Math.abs(b2 - b1);
        return (diff <= precision * angle);
    }


    private static double compareAngle(double angle, double angle2) {
        if (Math.abs(angle - angle2) < precision){
            return Math.abs(angle-angle2);
        }
        else{
            if (Math.abs(angle - angle2) > 90 && 180 - Math.abs(angle - angle2) < precision){
                return (180-Math.abs(angle-angle2));
            }
            else{
                return -1.0;
            }
        }
    }


    private static double compareLetters(List<HoughLine> linesP, List<HoughLine> linesM) {
        System.out.println(CLASS_NAME + " Validation ::");
        int i = 0;
        double linesValidation = 0;
        double validation;
        int nbLines = linesP.size() >= linesM.size() ? linesP.size() : linesM.size();

        List<HoughLine> linesRef = new ArrayList<>();
        List<HoughLine> linesTest = new ArrayList<>();

        if(linesP.size()<=linesM.size()) {
            linesRef.addAll(linesM);
            linesTest.addAll(linesP);
        }else{
            linesRef.addAll(linesP);
            linesTest.addAll(linesM);
        }

        while (i < linesRef.size()) {
            int j = 0;
            while (j < linesTest.size()) {
                validation = lineValidation(linesTest.get(j), linesRef.get(i));
                if (validation != -1.0){
                    linesRef.remove(linesRef.get(i));
                    linesTest.remove(linesTest.get(j));
                    i--;
                    linesValidation += validation;
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }
        return (linesValidation*100) / (nbLines);
    }


    private static double lineValidation (HoughLine lineTest, HoughLine lineRef){
        double m = (lineRef.y2 - lineRef.y1) / (lineRef.x2 - lineRef.x1);
        double m2 = (lineTest.y2 - lineTest.y1) / (lineTest.x2 - lineTest.x1);
        double angle = Math.toDegrees(Math.atan((m)));
        double angle2 = Math.toDegrees(Math.atan((m2)));
        double ecartAngles = compareAngle(angle, angle2);
        boolean isBSimilar = compareB(lineRef, lineTest, m, m2, Math.max(Math.abs(angle), Math.abs(angle2)));
        if ( ecartAngles != -1.0 && isBSimilar) {
            return (1-(ecartAngles/100));
        }
        return -1.0;
    }

}