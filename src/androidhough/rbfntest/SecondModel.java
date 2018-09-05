package androidhough.rbfntest;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import rbfn.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static rbfn.RBFNInputUtils.*;
import static rbfn.RBFNSavedInstance.arrayToMat;
import static rbfn.RBFNSavedInstance.load;

public class SecondModel {

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

    public static final File imagesFolder = new File(System.getProperty("user.home"), "/Téléchargements/secondModelResources/");

    public static final String TRUE_POSITIVE_FOLDER = "truePositive";
    public static final String TRUE_NEGATIVE_FOLDER = "trueNegative";
    public static final String FALSE_POSITIVE_FOLDER = "falsePositive";
    public static final String FALSE_NEGATIVE_FOLDER = "falseNegative";

    public static final String MODEL_IMAGE_NAME = "model.png";

    public static String[] answersForFirstExercise;
    public static String[] answersForSecondExercise;

    public static void main(String... args) throws IOException, ClassNotFoundException {

        /*
            Récupérer les images
         */

        String[] images = new String[]{"u","y"};

        // <modèle , <fichierEleve, output>>
        Map<String,Map<String,Boolean>> labelledImages = Stream.of(images).reduce(
                new HashMap<>(),
                (Map<String,Map<String,Boolean>> map, String image) -> {
                    File labelledFolder = new File(imagesFolder, image);
                    return mapMerger.apply(
                            map,
                            readImagesFromFourSeparatedFolders(
                                    labelledFolder,
                                    new File(labelledFolder, MODEL_IMAGE_NAME).getAbsolutePath()
                            ));
                },
                mapMerger
                );

        String bracket = "\\{";
        System.out.println(labelledImages.toString().replaceAll(",",",\n").replaceAll(bracket,"\t{\n"));

        List<String> modelImages = Stream.of(images).map(imageLabel ->
                Paths.get(imagesFolder.getAbsolutePath(), imageLabel, MODEL_IMAGE_NAME)
                        .toFile()
                        .getAbsolutePath()).collect(Collectors.toList());

        String imageModel1 = modelImages.get(0);
        String imageModel2 = modelImages.get(1);

        answersForFirstExercise = labelledImages.get(imageModel1).keySet().toArray(new String[]{});
        answersForSecondExercise = labelledImages.get(imageModel2).keySet().toArray(new String[]{});

        /*
            Load first model and get outputs from exercises
        */

        RBFNSavedInstance firstModelInstance = restoreFirstModel();
        RBFN firstModel = new RBFN(firstModelInstance);
        Mat firstExercise = prepareInputsForFirstModel(firstModelInstance, imageModel1).get(0);
        Mat secondExercise = prepareInputsForFirstModel(firstModelInstance, imageModel2).get(0);
        List<Mat> firstExerciseAnswers = prepareInputsForFirstModel(firstModelInstance, answersForFirstExercise);
        List<Mat> secondExerciseAnswers = prepareInputsForFirstModel(firstModelInstance, answersForSecondExercise);

        Mat firstExerciseOutput = firstModel.evalAndGetWeights(firstExercise);
        Mat secondExerciseOutput = firstModel.evalAndGetWeights(secondExercise);

        Mat[] meanAndStdDev = getMeanAndStdDevToNormalizeParameters(firstExerciseOutput, secondExerciseOutput);
        Mat means = meanAndStdDev[0], stdDevs = meanAndStdDev[1];

        normalizeInputs(means, stdDevs, firstExerciseOutput, secondExerciseOutput);



        List<Mat> firstExerciseAnswersOutput = new ArrayList<>(firstExerciseAnswers.size());
        for(Mat input : firstExerciseAnswers) {
            firstExerciseAnswersOutput.add( firstModel.evalAndGetWeights(input) );
        }
        List<Mat> secondExerciseAnswersOutput = new ArrayList<>(secondExerciseAnswers.size());
        for(Mat input : secondExerciseAnswers) {
            secondExerciseAnswersOutput.add( firstModel.evalAndGetWeights(input) );
        }

        /*
            Prepare new inputs for new model supposed to say if exercises should be validated or not
         */

        Mat OUTPUT_VALIDATE = NP.zeros(2); OUTPUT_VALIDATE.put(0,0,1);
        Mat OUTPUT_INVALIDATE = NP.zeros(2); OUTPUT_INVALIDATE.put(1,0,1);

        // List<Pattern> = List< <id, input, output> >
        List<Pattern> firstExercisePatterns = new ArrayList<>();
        int i = 0;
        Map<String,Boolean> stringBooleanMap = labelledImages.get(imageModel1);
        for(String answer : answersForFirstExercise) {
            Mat input = firstExerciseAnswersOutput.get(i);
            normalizeInputs(means, stdDevs, input);
            Core.subtract(firstExerciseOutput, input, input);
            firstExercisePatterns.add(
                new Pattern(i,
                        input,
                        stringBooleanMap.get(answer) ? OUTPUT_VALIDATE : OUTPUT_INVALIDATE )
            );
            i++;
        }

        List<Pattern> secondExercisePatterns = new ArrayList<>();
        int offset = i;
        stringBooleanMap = labelledImages.get(imageModel2);
        for(String answer : answersForSecondExercise) {
            Mat input = secondExerciseAnswersOutput.get(i-offset);
            normalizeInputs(means, stdDevs, input);
            Core.subtract(secondExerciseOutput, input, input);
            secondExercisePatterns.add(
                    new Pattern(i,
                            input,
                            stringBooleanMap.get(answer) ? OUTPUT_VALIDATE : OUTPUT_INVALIDATE )
            );
            i++;
        }

        int no_of_input = firstModelInstance.no_of_output;
        int no_of_output = 2; // 10 if validate, else 01
        int no_of_hidden_node = 30; // arbitrary
        RBFN secondModel = new RBFN(no_of_input, no_of_hidden_node, no_of_output);

        for(int nTrain = 0; nTrain < 10000; nTrain++) {
            System.out.println("epoch " + nTrain);
            for (Pattern pattern : firstExercisePatterns) {
                secondModel.train(pattern);
            }
        }

        List<Pattern> patterns = new ArrayList<Pattern>(firstExercisePatterns);// patterns.addAll(secondExercisePatterns);
        List<Character> labels = Arrays.asList('Y','N');
        Data data = new Data(patterns, labels);
        double accuracy = secondModel.get_accuracy_for_training(data);
        System.out.println("Accuracy for training data : " + accuracy + ".");

    }

    public static RBFNSavedInstance restoreFirstModel() throws IOException, ClassNotFoundException {
        return load(new File(savedRbfnModel));
    }

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

    /*
     @return <["true"/"false"]["Positive"/"Negative"] , <fichierEleve, output>>
     */
    public static Map<String,Map<String,Boolean>> readImagesFromFourSeparatedFolders(File sourceFolder) {
        return Stream.of(
                TRUE_POSITIVE_FOLDER,
                TRUE_NEGATIVE_FOLDER,
                FALSE_POSITIVE_FOLDER,
                FALSE_NEGATIVE_FOLDER
        ).collect(
                Collectors.toMap(
                        folderType -> folderType,
                        (String folderType) -> {
                            boolean output = folderType.startsWith("true") == folderType.endsWith("Positive");
                            File folder = new File(sourceFolder, folderType);
                            return Stream.of(folder.list())
                                    .collect(Collectors.toMap(
                                            (String s) -> new File(folder, s).getAbsolutePath(),
                                            s -> output));
                        }
                )
        );
    }

    /*
     @return <modèle , <fichierEleve, output>>
     */
    public static Map<String,Map<String,Boolean>> readImagesFromFourSeparatedFolders(File sourceFolder, String imageModele) {
        return Stream.of(
                TRUE_POSITIVE_FOLDER,
                TRUE_NEGATIVE_FOLDER,
                FALSE_POSITIVE_FOLDER,
                FALSE_NEGATIVE_FOLDER
        ).collect(
                Collectors.toMap(
                        folderType -> imageModele,
                        (String folderType) -> {
                            boolean output = folderType.startsWith("true") == folderType.endsWith("Positive");
                            File folder = new File(sourceFolder, folderType);
                            return Stream.of(folder.list())
                                    .collect(Collectors.toMap(
                                            (String s) -> new File(folder, s).getAbsolutePath(),
                                            s -> output));
                        },
                        stringBooleanMapMerger,
                        TreeMap::new // Hashmap got us DuplicateKey exception because of Hash conflict
                )
        );
    }

    public final static BinaryOperator<Map<String,Boolean>> stringBooleanMapMerger =
            (Map<String,Boolean> map1b, Map<String,Boolean> map2b ) -> {
                map2b.forEach( (s,b) -> map1b.putIfAbsent(s,b) );
                return map1b;
            };

    public final static BinaryOperator<Map<String,Map<String,Boolean>>> mapMerger =
            (map1, map2) -> {
                map2.entrySet().stream().forEach(
                        (Map.Entry<String,Map<String,Boolean>> folder) -> {
                            map1.merge(
                                    folder.getKey(), folder.getValue(),
                                    stringBooleanMapMerger);
                        }
                );
                return map1;
            };
}
