package mnist;

import androidhough.App;
import androidhough.Bitmap;
import androidhough.BitmapFactory;
import hough.HoughCircle;
import hough.HoughLine;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static hough.HoughCircle.Mat2BufferedImage;

public class EmnistToLineCSV {

    static int i = 0;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    static File srcMnistDir = Paths.get(System.getProperty("user.home"),"dl4j_Emnist","mnist_png","training").toFile();
    static File dstCsvFile = new File("hough_data.csv");
    static OutputStreamWriter csvOutput;
    static {
        try {
            csvOutput = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(dstCsvFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws IOException {
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
                emnistToCircleCSV.run(fileUri);
                System.out.println("\t" + fileUri);
                if(k++<=0) break;
            }
        }
        csvOutput.close();

    }

    public void run(String inputFileSrc) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeFile(inputFileSrc);
        List<HoughLine> lines = App.applyDetection(bitmap);
        for (HoughLine houghLine : lines) {
            String output = Stream.of(houghLine.getScore(), houghLine.getR(), houghLine.getTheta())
                    .reduce("",(s1,s2) -> s1 + "," + s2,(s1,s2) -> s1 + "," + s2);
            csvOutput.write(output.substring(1));
        }
        csvOutput.write('\n');
        csvOutput.flush();
    }

    static void printMat(Mat mat) {
        for(int i=0; i< mat.rows(); i++) {
            for(int j=0; j< mat.cols(); j++) {
                System.out.print(mat.get(i,j)[0] + ", ");
            }
            System.out.println();
        }
    }
}
