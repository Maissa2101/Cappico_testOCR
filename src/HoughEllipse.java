import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class HoughEllipse {

    public BufferedImage run(String name) {

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
                100.0, 30.0, 1, 110); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = null;
            int radius = 0;
            for(int j = 0 ; j < c.length ; j++) {
                 radius = (int) Math.round(c[2]);
            }
            center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(src, center, 1, new Scalar(0,100,100), 1, 8, 0 );
            // circle outline
            Imgproc.circle(src, center, radius, new Scalar(255,0,255), 2, 8, 0 );
        }
        HighGui.imshow("detected circles " + name, src);

        BufferedImage im = null;
        try {
            im = Mat2BufferedImage(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HighGui.imshow(" cercle "+name,src);
        return im;
    }

    BufferedImage Mat2BufferedImage(Mat matrix)throws Exception {
        MatOfByte mob=new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte ba[]=mob.toArray();

        BufferedImage bi=ImageIO.read(new ByteArrayInputStream(ba));
        return bi;
    }

    public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpng", byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
    }
}

