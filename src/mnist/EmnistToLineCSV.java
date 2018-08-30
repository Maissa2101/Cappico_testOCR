package mnist;

import androidhough.App;
import androidhough.Bitmap;
import androidhough.BitmapFactory;
import androidhough.houghtest.AccumulationMatrixExtractor;
import hough.HoughCircle;
import hough.HoughLine;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import rbfn.NP;

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
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_64F;

public class EmnistToLineCSV {

    static int i = 0;
    //static int min = Integer.MAX_VALUE;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
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
                emnistToCircleCSV.run(label, fileUri);
                System.out.println("\t" + fileUri);
                if(++k>=1) break;
            }
        }
        csvOutput.close();
        //System.out.println("Min: " + min);
    }

    public void run(char label, String inputFileSrc) throws IOException {
        Mat matrix = AccumulationMatrixExtractor.getAccumulationMatrix(inputFileSrc);
        int taille = (matrix.rows() * matrix.cols());
        int nbZeros = taille - Core.countNonZero(matrix);
        //if(min > nbZeros) min = nbZeros;
        MatOfDouble normalized = toNormalizedVector(matrix);
        printMat(normalized);
        writeMat(label, normalized);
        //csvOutput.write(toCsvString(label, nbZeros));
        csvOutput.flush();
    }

    private static void writeMat(char label, MatOfDouble mat) throws IOException {
        csvOutput.write(label+",");
        for(int i=0; i< mat.rows(); i++) {
            for(int j=0; j< mat.cols(); j++) {
                csvOutput.write(String.valueOf(mat.get(i,j)[0]));
                if(j < mat.cols()-1) csvOutput.write(",");
            }
            csvOutput.write("\n");
        }
    }

    static String toCsvString(Object... args) {
        return Stream.of(args).reduce("",(s1,s2) -> s1 + "," + s2,(s1,s2) -> s1 + "," + s2).substring(1) + "\n";
    }

    public static MatOfDouble toNormalizedVector(Mat matrix) {
        Mat reshaped = matrix.reshape(0, 1);
        MatOfDouble meanMatrix = new MatOfDouble(), stdMeanMatrix = new MatOfDouble();
        Core.meanStdDev(reshaped, meanMatrix, stdMeanMatrix);
        double mean = meanMatrix.get(0,0)[0], stdMean = stdMeanMatrix.get(0,0)[0];
        MatOfDouble res = new MatOfDouble(NP.zeros(1, reshaped.cols()));
        for(int i=0; i<reshaped.cols(); i++) {
            double value = reshaped.get(0,i)[0];
            if(value != 0.0) {
                res.put(0,i, (value - mean) / stdMean);

            }
        }
        System.out.println(mean + " - " + stdMean);
        printMat(reshaped);
        return res;
    }

    static void printMat(Mat mat) {
        //System.out.println(mat.rows() + "  ---  " + mat.cols());
        for(int i=0; i< mat.rows(); i++) {
            for(int j=0; j< mat.cols(); j++) {
                System.out.print(mat.get(i,j)[0] + ", ");
            }
            System.out.println();
        }
    }
}
