package mnist;

import hough.HoughCircle;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hough.HoughCircle.Mat2BufferedImage;

public class EmnistToLineCSV {

    static int i = 0;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    static File srcMnistDir = new File(System.getProperty("user.home"),"/ocr/");

    public static void main(String... args) {
        EmnistToLineCSV emnistToCircleCSV = new EmnistToLineCSV();
        Map<Character,List<String>> labelledFilesUri = new HashMap<>();
        for(File labelledDirectory : srcMnistDir.listFiles()) {
            char label = labelledDirectory.getName().charAt(0);
            labelledFilesUri.put(label, new ArrayList<>());
            for(File file : labelledDirectory.listFiles()) {
                labelledFilesUri.get(label).add(file.getAbsolutePath());
            }
        }
        for(Map.Entry<Character,List<String>> entry : labelledFilesUri.entrySet()) {
            char label = entry.getKey();
            System.out.println("Label: '" + label + "'");
            int k = 0;
            for(String fileUri : entry.getValue()) {
                emnistToCircleCSV.run(fileUri, 5);
                System.out.println("\t" + fileUri);
                if(++k > 0) break;
            }
        }
    }

    public void run(String file, int precision) {
        BufferedImage im = null;

        List<Double> listR= new ArrayList<>();
        List<Double> listTheta= new ArrayList<>();

        Mat src = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

        if( src.empty() ) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default "
                    + file +"] \n");
            System.exit(-1);
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        try {
            im = Mat2BufferedImage(gray);
            ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i) +"_Aaoriginal.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*for(int row=0; row<gray.rows(); row++){
            for(int col=0; col<gray.cols(); col++){
                double pixelValue = gray.get(row,col)[0];
                if(pixelValue > 100) gray.put(row,col, 255.0);
                else gray.put(row,col, 0.0);
            }
        }

        try {
            im = Mat2BufferedImage(gray);
            ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i) +"_Agray.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        //Imgproc.medianBlur(src, gray, 5);
        //Size size = new Size(100, 100);
        //Imgproc.resize(gray, gray, size, 0, 0, Imgproc.INTER_AREA);
        // Edge detection

        //Mat canny = new Mat();
        Imgproc.Canny(gray, gray, 50, 150, 3, false);
        /*Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.dilate(gray, gray, element);*/
        try {
            im = Mat2BufferedImage(gray);
            ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i) +"_BCanny.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Standard Hough Line Transform
        Mat lines = new Mat(); // will hold the results of the detection
        Imgproc.HoughLines(gray, lines, 1, Math.PI/180, 15); // runs the actual detection
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < lines.rows(); x++) {

            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
            listR.add(rho);
            listTheta.add(theta);
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a*rho, y0 = b*rho;
            Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
            Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
            Imgproc.line(gray, pt1, pt2, new Scalar(255), 1, Imgproc.LINE_AA, 0);
        }

        try {
            im = Mat2BufferedImage(gray);
            ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i++) +"C.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.print(listR.size() + "\t" + listTheta.size());
    }
}
