package androidhough.rbfntest;

import androidhough.App;
import androidhough.Bitmap;
import androidhough.BitmapFactory;
import hough.HoughCircle;
import hough.HoughLine;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import rbfn.Data;
import rbfn.NP;
import rbfn.Pattern;
import rbfn.RBFN;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTestWithRBFN {
    static {
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    final static String LABELS = "bgm";

    public static void main(String... args) {
        int nbCirclesToDetect = 3;

        int nOutput = LABELS.length();
        int nInput = androidhough.App.nbLinesToDetect * 2 + nbCirclesToDetect * 3;

        Map<Character,Mat> outputs = new HashMap<>();
        for(int i=0; i<nOutput; i++){
            Mat output = NP.zeros(nOutput);
            output.put(i,0,1);
            outputs.put(LABELS.charAt(i), output);
        }

        String bUri = "/home/radureau/Téléchargements/letters_capicokids_png/b/b_capicokids_1.png";
        String gUri = "/home/radureau/Téléchargements/letters_capicokids_png/g/g_capicokids_1.png";
        String mUri = "/home/radureau/Téléchargements/letters_capicokids_png/m/M_capicokids_1.png";

        Bitmap bBitmap = BitmapFactory.decodeFile(bUri);
        Bitmap gBitmap = BitmapFactory.decodeFile(gUri);
        Bitmap mBitmap = BitmapFactory.decodeFile(mUri);
        List<HoughLine> bLines = App.applyDetection(bBitmap);
        List<HoughLine> gLines = App.applyDetection(gBitmap);
        List<HoughLine> mLines = App.applyDetection(mBitmap);

        Mat gInputs = NP.zeros(nInput);
        for(int i=0; i<gLines.size(); i++){
            gInputs.put(i*2,0,gLines.get(i).getTheta());
            gInputs.put(i*2+1,0,gLines.get(i).getR()/200);
        }
        Mat mInputs = NP.zeros(nInput);
        for(int i=0; i<mLines.size(); i++){
            mInputs.put(i*2,0,mLines.get(i).getTheta());
            mInputs.put(i*2+1,0,mLines.get(i).getR()/200);
        }
        Mat bInputs = NP.zeros(nInput);
        for(int i=0; i<bLines.size(); i++){
            bInputs.put(i*2,0,bLines.get(i).getTheta());
            bInputs.put(i*2+1,0,bLines.get(i).getR()/200);
        }

        int offset =  androidhough.App.nbLinesToDetect * 2;

        HoughCircle houghCircle = new HoughCircle();
        houghCircle.run(gUri, 20);
        for(int i=0; i<houghCircle.listRadius.size(); i++){
            gInputs.put(offset + i*3,0, houghCircle.listRadius.get(i)/300);
            gInputs.put(offset + i*3+1,0, houghCircle.listCenter.get(i).x/300);
            gInputs.put(offset + i*3+2,0, houghCircle.listCenter.get(i).y/300);
        }

        houghCircle.listCenter.clear();
        houghCircle.listRadius.clear();
        houghCircle.run(mUri, 20);
        for(int i=0; i<houghCircle.listRadius.size(); i++){
            mInputs.put(offset + i*3,0, houghCircle.listRadius.get(i)/300);
            mInputs.put(offset + i*3+1,0, houghCircle.listCenter.get(i).x/300);
            mInputs.put(offset + i*3+2,0, houghCircle.listCenter.get(i).y/300);
        }
        houghCircle.listCenter.clear();
        houghCircle.listRadius.clear();
        houghCircle.run(bUri, 20);
        for(int i=0; i<houghCircle.listRadius.size(); i++){
            bInputs.put(offset + i*3,0, houghCircle.listRadius.get(i)/300);
            bInputs.put(offset + i*3+1,0, houghCircle.listCenter.get(i).x/300);
            bInputs.put(offset + i*3+2,0, houghCircle.listCenter.get(i).y/300);
        }


        printMat(bInputs);
        System.out.println("_______");
        printMat(gInputs);
        printMat(mInputs);
        //System.exit(0);

        Pattern patternB = new Pattern(1, gInputs, outputs.get('b'));
        Pattern patternG = new Pattern(2, gInputs, outputs.get('g'));
        Pattern patternM = new Pattern(3, mInputs, outputs.get('m'));

        List<Pattern> patterns = Arrays.asList(patternB, patternG, patternM);
        List<Character> classLabels = Arrays.asList('b','g', 'm');
        Data data = new Data(patterns, classLabels);
        RBFN rbfn = new RBFN(nInput, 100, nOutput, data);
        double mse = rbfn.train(5000);
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
