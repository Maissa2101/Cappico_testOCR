package androidhough;

import hough.HoughLine;

import java.util.List;

public class Main {

    static String modelImagePath = "/home/radureau/dl4j_Emnist/uppercase_png/training/A/train_41_00744.png";
    static  String userImagePath = modelImagePath;
    //static String modelImagePath = "/home/radureau/Téléchargements/letters_capicokids_png/m/M_capicokids_1.png";
    //static String userImagePath = "/home/radureau/Téléchargements/letters_capicokids_png/m/M_capicokids_1.png";
    //static String userImagePath = "/home/radureau/Téléchargements/letters_capicokids_png/g/g_capicokids_1.png";

    public static  Boolean areUserAnswersCorrect() {
        Bitmap studentDraw;
        Bitmap modelDraw;
        modelDraw = BitmapFactory.decodeFile(modelImagePath);
        studentDraw = BitmapFactory.decodeFile(userImagePath);
        List<HoughLine> modelLine = App.applyDetection(modelDraw);
        List<HoughLine> studentLine = App.applyDetection(studentDraw);
        Double result = App.validationMaj(modelLine, studentLine);

        // TODO recuperer la precision d'acceptation de la question
       System.out.println( result.toString() + "% " + Boolean.toString(result>=70));
       return result >= 70;
    }

    public static void main(String... args) {
        System.out.println(
                areUserAnswersCorrect() ? "Match!" : "No match :("
        );
    }
}
