package androidhough.rbfntest;

import androidhough.App;
import androidhough.Bitmap;
import androidhough.BitmapFactory;
import androidhough.houghtest.AccumulationMatrixExtractor;
import hough.HoughCircle;
import hough.HoughLine;
import mnist.EmnistToLineCSV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import rbfn.Data;
import rbfn.NP;
import rbfn.Pattern;
import rbfn.RBFN;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static androidhough.rbfntest.FirstTestWithRBFN.srcMnistDir;

public class NormalizedVectorWithRBFN {
    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    final static String LABELS = "0123456789";

    public static void main(String... args) {

        int nOutput = LABELS.length();

        Map<Character,Mat> outputs = new HashMap<>();
        for(int i=0; i<nOutput; i++){
            Mat output = NP.zeros(nOutput);
            output.put(i,0,1);
            outputs.put(LABELS.charAt(i), output);
        }

        List<Pattern> patterns = new ArrayList<>();
        int i = 0;
        for(File labelledDirectory : srcMnistDir.listFiles()) {
            char label = labelledDirectory.getName().charAt(0);
            for(File file : labelledDirectory.listFiles()) {
                Mat matrix = AccumulationMatrixExtractor.getAccumulationMatrix(file.getAbsolutePath());
                Mat features = EmnistToLineCSV.toNormalizedVector(matrix).t();
                patterns.add(new Pattern(i++, features, outputs.get(label)));
                break;
            }
        }

        List<Character> classLabels = LABELS.chars().mapToObj(c -> (char)c).collect(Collectors.toList());

        int nInput = patterns.get(0).getInput().rows();

        Data data = new Data(patterns, classLabels);
        RBFN rbfn = new RBFN(nInput, 2000, nOutput, data);
        double mse = rbfn.train(500);
        double accuracy = rbfn.get_accuracy_for_training();
        System.out.println("Total accuracy is " + accuracy);
        System.out.println("Last MSE " + mse);
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
