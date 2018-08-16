package rbfn;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.Random;

public final class NP {
    private NP() {}

    public static Mat zeros(int length) {
        return new Mat(length, 1, CvType.CV_64F, Scalar.all(0));
    }
    public static Mat zeros(int nrow, int ncol) {
        return new Mat(nrow, ncol, CvType.CV_64F, Scalar.all(0));
    }

    private final static Random randomGenerator = new Random();
    public static double randomUniform() {
        return randomGenerator.nextDouble();
    }
}
