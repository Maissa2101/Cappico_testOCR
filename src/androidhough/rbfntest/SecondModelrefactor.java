package androidhough.rbfntest;

import com.excilys.android.children.handwriting.validation.rbfn.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.excilys.android.children.handwriting.validation.rbfn.RBFNInputUtils.getMeanAndStdDevToNormalizeParameters;
import static com.excilys.android.children.handwriting.validation.rbfn.RBFNInputUtils.normalizeInputs;
import static com.excilys.android.children.handwriting.validation.rbfn.RBFNInputUtils.printMat;
import static com.excilys.android.children.handwriting.validation.rbfn.RBFNSavedInstance.load;

public class SecondModelrefactor {
    /* Format du dossier d'image supporté :

    /<imagesFolder/ -> root folder for images, e.g. : /home/modelResources

    /<imagesFolder/<SingleCharacterLabelName>/ -> e.g. : /home/modelResources/u

    /<imagesFolder/<SingleCharacterLabelName>/<MODEL_IMAGE_NAME> -> e.g. : /home/modelResources/u/model.png
    c'est le fichier modèle, celui donné par l'enseignant en tant qu'exercice

    /<imagesFolder/<SingleCharacterLabelName>/<TRUE_POSITIVE_FOLDER>/*.png
    /<imagesFolder/<SingleCharacterLabelName>/<TRUE_NEGATIVE_FOLDER>/*.png
    /<imagesFolder/<SingleCharacterLabelName>/<FALSE_POSITIVE_FOLDER>/*.png
    /<imagesFolder/<SingleCharacterLabelName>/<FALSE_NEGATIVE_FOLDER>/*.png
    dossiers contenant les images des réponses des élèves,
    classées en fonction des résultats de la transformée de hough simple

     */

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    // file generated with FirstTestWithRBFN class
    public static final String savedRbfnModel = "savedModel.rbfn";

    public static final String imagesFolderAbsolutePath = new File(System.getProperty("user.home"), "/Téléchargements/secondModelResources/").getAbsolutePath();

    public static final String TRUE_POSITIVE_FOLDER = "truePositive";
    public static final String TRUE_NEGATIVE_FOLDER = "trueNegative";
    public static final String FALSE_POSITIVE_FOLDER = "falsePositive";
    public static final String FALSE_NEGATIVE_FOLDER = "falseNegative";
    public static final Predicate<String> outputForFolderPredicate = folderName -> folderName.startsWith("true") == folderName.endsWith("Positive");

    public static final String MODEL_IMAGE_NAME = "model.png";

    public static void main(String... args) throws IOException, ClassNotFoundException {

        List<String> modelResources = Arrays.asList(new File(imagesFolderAbsolutePath).list());
        List<Exercise> exercises = modelResources.stream().filter(s -> !s.equals("u")).peek(System.out::println)
                .map( SecondModelrefactor::readImagesFromFourSeparatedFolders )
                .flatMap(List::stream)
                .collect(Collectors.toList());

        final RBFNSavedInstance firstModelInstance = load(new File(savedRbfnModel));
        final RBFN firstModel = new RBFN(firstModelInstance);
        Function<String,Mat> computeInputFromFirstModel = (String imageFileAbsolutePath) -> Util.prepareInputsForFirstModel(firstModelInstance, imageFileAbsolutePath).get(0);

        System.out.println(exercises.toString().replaceAll("Exercise","\n").replaceAll(",","\n"));

        Map<String,Mat> imagefileInputMap = new TreeMap<>();
        exercises.stream().forEach(exercise -> {
            imagefileInputMap.computeIfAbsent(exercise.modelFileAbsolutePath, computeInputFromFirstModel);
            imagefileInputMap.computeIfAbsent(exercise.answerFileAbsolutePath, computeInputFromFirstModel);
        });

        imagefileInputMap.replaceAll((imageFilePath, input) -> firstModel.evalAndGetWeights(input));

        Mat[] buffer = imagefileInputMap.values().toArray(new Mat[]{});
        Mat[] meanAndStdDev = getMeanAndStdDevToNormalizeParameters(buffer);
        Mat means = meanAndStdDev[0], stdDevs = meanAndStdDev[1];
        normalizeInputs(means, stdDevs, buffer);

        int no_of_input = firstModelInstance.no_of_output;
        int no_of_output = 2; // 10 if validate, else 01
        int no_of_hidden_node = 300; // arbitrary
        RBFN secondModel = new RBFN(no_of_input, no_of_hidden_node, no_of_output);

        Mat OUTPUT_VALIDATE = NP.zeros(2); OUTPUT_VALIDATE.put(0,0,1);
        Mat OUTPUT_INVALIDATE = NP.zeros(2); OUTPUT_INVALIDATE.put(1,0,1);

        AtomicInteger patternCounter = new AtomicInteger(0);
        List<Pattern> patterns = exercises.parallelStream().map( exercise -> {
            Mat diff = NP.zeros(no_of_input);
            Mat modelWeights = imagefileInputMap.get(exercise.modelFileAbsolutePath);
            Mat anwserWeights = imagefileInputMap.get(exercise.answerFileAbsolutePath);
            Core.subtract(modelWeights, anwserWeights, diff);
            return new Pattern(
                    patternCounter.getAndIncrement(),
                    diff,
                    exercise.isValid() ? OUTPUT_VALIDATE : OUTPUT_INVALIDATE
            );
        }).collect(Collectors.toList());

        for(int nTrain = 0; nTrain < 1000; nTrain++) {
            System.out.println("epoch " + nTrain);
            for (Pattern pattern : patterns) {
                secondModel.train(pattern);
            }
        }

        List<Character> labels = Arrays.asList('Y','N');
        Data data = new Data(patterns, labels);
        double accuracy = secondModel.get_accuracy_for_training(data);
        System.out.println("Accuracy for training data : " + accuracy + ".");

        File savedModel = new File("savedSecondModel.rbfn");
        secondModel.save(savedModel, means, stdDevs, labels);

    }

    public static List<Exercise> readImagesFromFourSeparatedFolders(String characterLabel) {
        Path labelledFolder = Paths.get(imagesFolderAbsolutePath, characterLabel);
        String modelFileAbsolutePath = labelledFolder.resolve(MODEL_IMAGE_NAME).toFile().getAbsolutePath();
        return Stream.of(
                TRUE_POSITIVE_FOLDER,
                TRUE_NEGATIVE_FOLDER,
                FALSE_POSITIVE_FOLDER,
                FALSE_NEGATIVE_FOLDER)
                .flatMap(folderType -> {
                    boolean output = outputForFolderPredicate.test(folderType);
                    File folder = new File(labelledFolder.toFile(), folderType);
                    return folder.list() != null ?
                            Stream.of(folder.list())
                            .map( imageFileName -> new Exercise(
                                    modelFileAbsolutePath,
                                    new File(folder, imageFileName).getAbsolutePath(),
                                    output
                                    )
                            )
                            .collect(Collectors.toList()).stream()
                            : Stream.empty();
                }).collect(Collectors.toList());
    }
}
