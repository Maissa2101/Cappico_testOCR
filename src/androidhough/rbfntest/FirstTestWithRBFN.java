package androidhough.rbfntest;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import rbfn.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static rbfn.RBFNInputUtils.getMeanAndStdDevToNormalizeParameters;
import static rbfn.RBFNInputUtils.normalizeInputs;

public class FirstTestWithRBFN {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static File srcMnistDir = new File(System.getProperty("user.home"),"/Téléchargements/letters_capicokids_png/");

    public static void main(String... args) throws IOException, ClassNotFoundException {
        Map<Character,List<String>> labelledFilesUri = new TreeMap<>();
        for(File labelledDirectory : srcMnistDir.listFiles()) {
            char label = labelledDirectory.getName().charAt(0);
            labelledFilesUri.put(label, new ArrayList<>());
            for(File file : labelledDirectory.listFiles()) {
                labelledFilesUri.get(label).add(file.getAbsolutePath());
            }
        }

        int nOutput = labelledFilesUri.keySet().size();
        Iterator<Character> labelIterator = labelledFilesUri.keySet().iterator();
        Map<Character,Mat> outputs = new TreeMap<>();
        for(int i=0; i<nOutput; i++){
            Mat output = NP.zeros(nOutput);
            output.put(i,0,1);
            outputs.put(labelIterator.next(), output);
        }

        RBFN rbfn = new RBFN(RBFNInputUtils.nInput, RBFNInputUtils.nHiddenLayer, nOutput);

        Map<String,Pattern> mapFile2Pattern = new HashMap<>();
        for(Map.Entry<Character,List<String>> entry : labelledFilesUri.entrySet()) {
            char label = entry.getKey();
            System.out.println("Label: '" + label + "'");
            for(String fileUri : entry.getValue()) {

                Mat input = RBFNInputUtils.prepareInput(fileUri);
                Pattern pattern = new Pattern(RBFNInputUtils.lastPatternId++, input, outputs.get(label));
                mapFile2Pattern.put(fileUri, pattern);
                System.out.println("\t" + fileUri);
                break;
            }
        }

        Mat[] inputsToNormalize = new Mat[mapFile2Pattern.values().size()];
        mapFile2Pattern.values().stream().map(pattern -> pattern.getInput()).collect(Collectors.toList()).toArray(inputsToNormalize);
        Mat[] meanAndStdDev = getMeanAndStdDevToNormalizeParameters(inputsToNormalize);
        Mat means = meanAndStdDev[0], stdDevs = meanAndStdDev[1];
        normalizeInputs(means, stdDevs, inputsToNormalize);

        List<Pattern> patterns = new LinkedList<>(mapFile2Pattern.values());
        List<Character> labels = new ArrayList<>(outputs.keySet());
        Data data = new Data(patterns, labels);

        for(int nTrain = 0; nTrain < 10000; nTrain++) {
            System.out.println("epoch " + nTrain);
            for(Map.Entry<String,Pattern> entry : mapFile2Pattern.entrySet() ){
                rbfn.train(entry.getValue());
            }
        }

        double accuracy = rbfn.get_accuracy_for_training(data);
        System.out.println("Accuracy for training data : " + accuracy + ".");

        File savedModel = new File("savedModel.rbfn");
        rbfn.save(savedModel, means, stdDevs, labels);
        RBFNSavedInstance savedInstance = RBFNSavedInstance.load(savedModel);
        RBFN restoredRbfn = new RBFN(savedInstance);

        accuracy = restoredRbfn.get_accuracy_for_training(data);
        System.out.println("Accuracy for training data : " + accuracy + ".");
    }

}
