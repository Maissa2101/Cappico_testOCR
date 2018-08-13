package mnist;

import hough.HoughCircle;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.*;

import java.awt.image.BufferedImage;
import java.util.List;

import static hough.HoughCircle.Mat2BufferedImage;

public class EmnistToCircleCSV {

    static int i = 0;

    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    static File srcMnistDir = new File(System.getProperty("user.home"),"/ocr/");

    public static void main(String... args) {
        EmnistToCircleCSV emnistToCircleCSV = new EmnistToCircleCSV();
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

        List<Point> listCenter= new ArrayList<>();
        List<Integer> listRadius= new ArrayList<>();

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
            ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i) +"_Agray.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int row=0; row<gray.rows(); row++){
            for(int col=0; col<gray.cols(); col++){
                double pixelValue = gray.get(row,col)[0];
                if(pixelValue <0 || pixelValue > 255) System.exit(1);
                if(pixelValue > 50) gray.put(row,col, 255.0);
            }
        }

        try {
            im = Mat2BufferedImage(gray);
            ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i) +"_Bblack.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Imgproc.medianBlur(src, gray, 5);
        //Size size = new Size(100, 100);
        //Imgproc.resize(gray, gray, size, 0, 0, Imgproc.INTER_AREA);


        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                1, // change this value to detect circles with different distances to each other
                100.0, 10.0, 2, 10); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);
            listCenter.add(center);
            listRadius.add(radius);
        }
        reductionCircleSimilar(precision, listCenter, listRadius);
        System.out.print(listCenter.size() + "\t" + listRadius.size());

        //if(listCenter.size() > 0) {
            for (int x = 0; x < listRadius.size(); x++) {
                // circle center
                Imgproc.circle(gray, listCenter.get(x), 1, new Scalar(0), 1, 8, 0);
                // circle outline
                Imgproc.circle(gray, listCenter.get(x), listRadius.get(x), new Scalar(0), 1, 8, 0);
            }
            try {
                im = Mat2BufferedImage(gray);
                ImageIO.write(im, "png", new File(System.getProperty("user.home") + "/trash/output_"+(char)( 'A' + i++) +"C.png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        //}
    }

    void reductionCircleSimilar(int precision, List<Point> listCenter, List<Integer> listRadius) {
        int i=0,j;
        while (i < listCenter.size()) {
            j=0;
            while (j < listCenter.size()) {
                if (i != j) {
                    double dist = HoughCircle.distancePoints(listCenter.get(i),listCenter.get(j));
                    double diff = Math.abs(listRadius.get(i)-listRadius.get(j));
                    if(dist <=precision){
                        listCenter.remove(j);
                        listRadius.remove(j);
                        j--;
                    }
                }
                j++;
            }
            i++;
        }
    }
}
