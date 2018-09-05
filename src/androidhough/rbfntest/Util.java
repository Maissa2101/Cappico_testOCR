package androidhough.rbfntest;

import org.opencv.core.Mat;
import rbfn.RBFNSavedInstance;

import java.util.ArrayList;
import java.util.List;

import static rbfn.RBFNInputUtils.normalizeInputs;
import static rbfn.RBFNInputUtils.prepareInput;
import static rbfn.RBFNSavedInstance.arrayToMat;

public final class Util {
    private Util() {}

    public static List<Mat> prepareInputsForFirstModel(RBFNSavedInstance firstModelInstance, String... imagesAbsolutePath) {
        Mat means = arrayToMat(firstModelInstance.means);
        Mat stdDevs = arrayToMat(firstModelInstance.stdDevs);
        List<Mat> inputs = new ArrayList<>(imagesAbsolutePath.length);
        for(String imageAbsolutePath : imagesAbsolutePath) {
            inputs.add(
                    prepareInput(imageAbsolutePath)
            );
        }
        normalizeInputs(means, stdDevs, inputs.toArray(new Mat[]{}));
        return inputs;
    }
}
