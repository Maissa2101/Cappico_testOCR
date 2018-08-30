package rbfn;

import androidhough.App;
import androidhough.Bitmap;
import androidhough.BitmapFactory;
import hough.HoughCircle;
import hough.HoughLine;
import hough.Lines;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Point;

import java.util.List;

public class RBFNInputUtils {

    public static int lastPatternId = 0;

    public static final int SCALED_WIDTH_IN_PX = 300;
    public static final int SCALED_HEIGHT_IN_PX = 300;

    public final static int placeToSpecifyHowManyWereDetected = 1;
    public final static int nbCirclesToDetect = 3;
    public final static int placeForLines = App.nbLinesToDetect * 2 + placeToSpecifyHowManyWereDetected;
    public final static int nbIntersectionsToKeep = App.nbLinesToDetect;
    public final static int placeForIntersections = nbIntersectionsToKeep * 2 + placeToSpecifyHowManyWereDetected;
    public final static int placeForCircles = nbCirclesToDetect * 3 +  placeToSpecifyHowManyWereDetected;
    public final static int nInput = placeForLines + placeForCircles + nbIntersectionsToKeep;

    public final static int nHiddenLayer = nInput * 5; // arbitrary

    public static void normalizeInputs(Mat mean, Mat stdDev, Mat... inputs) {
        for(Mat input : inputs) {
            Core.subtract(input, mean, input);
            Core.divide(input, stdDev, input);
        }
    }

    public static Mat[] getMeanAndStdDevToNormalizeParameters(Mat... inputs) {
        Mat merge = new Mat();
        for(Mat input : inputs){
            merge.push_back(input.t());
        }
        MatOfDouble meanBuff = new MatOfDouble();
        MatOfDouble stddevBuff = new MatOfDouble();
        Mat mean = new Mat();
        Mat stddev = new Mat();
        for(int i=0; i<merge.cols(); i++) {
            Core.meanStdDev(merge.col(i),meanBuff,stddevBuff);
            mean.push_back(meanBuff);
            stddev.push_back(stddevBuff);
        }
        Mat[] meanAndStdmean = {mean, stddev};
        return meanAndStdmean;
    }

    public static Mat prepareInput(String fileUri) {
        Mat input = NP.minDoubleVector(nInput);

        Bitmap bitmap = BitmapFactory.decodeFile(fileUri);
        List<HoughLine> houghLines = App.applyDetection(bitmap);
        for(int i=0; i<houghLines.size(); i++){
            input.put(i*2,0,houghLines.get(i).getTheta());
            input.put(i*2+1,0,houghLines.get(i).getR());
        }
        input.put(placeForLines-1,0, houghLines.size());

        int offset =  placeForLines;

        HoughCircle houghCircle = new HoughCircle();
        houghCircle.run(fileUri, 20);
        for(int i=0; i<houghCircle.listRadius.size(); i++){
            input.put(offset + i*3,0, houghCircle.listRadius.get(i));
            input.put(offset + i*3+1,0, houghCircle.listCenter.get(i).x);
            input.put(offset + i*3+2,0, houghCircle.listCenter.get(i).y);
        }
        input.put(offset + placeForCircles -1, 0, houghCircle.listRadius.size());

        offset = placeForLines + placeForCircles;

        List<Point> intersections = new Lines(houghLines, SCALED_WIDTH_IN_PX, SCALED_HEIGHT_IN_PX).intersections;
        for(int i=0; i<intersections.size(); i++){
            Point intersection = intersections.get(i);
            input.put(offset + i*2,0, intersection.x);
            input.put(offset + i*2+1,0, intersection.y);
        }
        input.put(offset + placeForIntersections -1, 0, intersections.size());
        return input;
    }
}
