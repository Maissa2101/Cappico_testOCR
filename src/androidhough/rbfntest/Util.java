package androidhough.rbfntest;

import com.excilys.android.children.handwriting.validation.rbfn.RBFNSavedInstance;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import static com.excilys.android.children.handwriting.validation.rbfn.RBFNInputUtils.normalizeInputs;
import static com.excilys.android.children.handwriting.validation.rbfn.RBFNInputUtils.prepareInput;
import static com.excilys.android.children.handwriting.validation.rbfn.RBFNSavedInstance.arrayToMat;

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
