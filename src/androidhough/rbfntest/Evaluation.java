package androidhough.rbfntest;

import com.excilys.android.children.handwriting.validation.rbfn.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static com.excilys.android.children.handwriting.validation.rbfn.RBFNInputUtils.*;
import static com.excilys.android.children.handwriting.validation.rbfn.RBFNSavedInstance.*;

public class Evaluation {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static final File imagesFolder = new File(System.getProperty("user.home"), "/Téléchargements/letters_capicokids_png/");
    public static final String imageModelName = "m/lettre42.png";
    public static final String imageAnswerName = "m/lettre12.png";

    // file generated with FirstTestWithRBFN class
    public static final String savedRbfnModel = "savedModel.rbfn";

    public static void main(String... args) throws IOException, ClassNotFoundException {
        String imageModel = new File(imagesFolder, imageModelName).getAbsolutePath();
        String imageAnswer = new File(imagesFolder, imageAnswerName).getAbsolutePath();

        long start = System.currentTimeMillis();
        RBFNSavedInstance rbfnSavedInstance = load(new File(savedRbfnModel));
        long loadTime = System.currentTimeMillis() - start;
        System.out.println(loadTime);

        Mat means = arrayToMat(rbfnSavedInstance.means);
        Mat stdDevs = arrayToMat(rbfnSavedInstance.stdDevs);
        Mat modelInput = prepareInput(imageModel);
        Mat answerInput = prepareInput(imageAnswer);
        normalizeInputs(means, stdDevs, modelInput, answerInput);

        RBFN rbfn = new RBFN(rbfnSavedInstance);
        Character[] labels = rbfnSavedInstance.labels;

        printMat(rbfn.evalAndGetWeights(modelInput));
        System.out.println(
                labels[ rbfn.evalAndGetOutputIndex(modelInput) ]
        );

        System.out.println("\n" + Stream.iterate("-",v->v).limit(15).reduce(String::concat).get() + "\n");

        printMat(rbfn.evalAndGetWeights(answerInput));
        System.out.println(
                labels[ rbfn.evalAndGetOutputIndex(answerInput) ]
        );

    }
}