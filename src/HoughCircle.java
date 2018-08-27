import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class HoughCircle {

    List<Point> listCenter= new ArrayList<>();
    List<Integer> listRadius= new ArrayList<>();

    public BufferedImage run(String name, int precision) {

        String file = "/home/excilys/capico-java/Cappico_testOCR/" + name + ".png";

        Mat src = Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);

        if( src.empty() ) {
            System.out.println("Error opening image!");
            System.out.println("Program Arguments: [image_name -- default "
                    + file +"] \n");
            System.exit(-1);
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(gray, gray, 5);
        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                (double)gray.rows()/16, // change this value to detect circles with different distances to each other
                100.0, 30.0, 1, 140); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            int radius = (int) Math.round(c[2]);
            listCenter.add(center);
            listRadius.add(radius);
        }

        reductionCircleSimilar(precision);

        System.out.println(ConsoleColor.PURPLE_BOLD+"Size Center "+ listCenter.size());
        System.out.println(ConsoleColor.PURPLE_BOLD+"Size Radius "+ listRadius.size()+ConsoleColor.RESET);
        for (int x = 0; x < listRadius.size(); x++) {
            // circle center
            Imgproc.circle(src, listCenter.get(x), 1, new Scalar(0,100,100), 3, 8, 0 );
            // circle outline
            Imgproc.circle(src, listCenter.get(x), listRadius.get(x), new Scalar(255,0,255), 3, 8, 0 );
        }
        BufferedImage im = null;
        try {
            im = Mat2BufferedImage(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return im;
    }


    BufferedImage Mat2BufferedImage(Mat matrix)throws Exception {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[]=mob.toArray();

        BufferedImage bi=ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }
    double distancePoints(Point p1,Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    void reductionCircleSimilar(int precision) {
        int i=0,j;
        while (i < listCenter.size()) {
            j=0;

            while (j < listCenter.size()) {
                if (i != j) {
                    double dist = distancePoints(listCenter.get(i),listCenter.get(j));
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