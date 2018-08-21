package androidhough.rbfntest;

import androidhough.App;
import androidhough.Bitmap;
import androidhough.BitmapFactory;
import hough.HoughLine;
import mnist.EmnistToLineCSV;
import org.opencv.core.Core;
import rbfn.RBFN;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstTestWithRBFN {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static File srcMnistDir = new File(System.getProperty("user.home"),"/Téléchargements/letters_capicokids_png/");

    public static void main(String... args) {
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
            for(String fileUri : entry.getValue()) {
                // TODO
                Bitmap bitmap = BitmapFactory.decodeFile(fileUri);
                List<HoughLine> houghLines = App.applyDetection(bitmap);

                System.out.println("\t" + fileUri);
            }
        }
    }
}
