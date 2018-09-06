package com.excilys.android.children.handwriting.validation.rbfn;

import org.opencv.core.Mat;

import java.io.*;

public class RBFNSavedInstance implements Serializable {

    public static final long serialVersionUID = 1L;

    public int no_of_input;
    int no_of_hidden;
    public int no_of_output;
    double[][] centroid;
    double[] sigma;
    double[][] hidden_to_output_weight;
    double[] output_bias;

    public Character[] labels;

    public double[] means;
    public double[] stdDevs;

    public static RBFNSavedInstance load(File inputfile) throws IOException, ClassNotFoundException {
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(inputfile));
        RBFNSavedInstance savedInstance = (RBFNSavedInstance) input.readObject();
        return savedInstance;
    }

    public static Mat arrayToMat(double[] array, Mat mat) {
        boolean isVerticalVector = mat.cols() == 1;
        for(int i = 0; i < array.length ; i++) {
            if(isVerticalVector) {
                mat.put(i,0, array[i]);
            } else {
                mat.put(0,i, array[i]);
            }
        }
        return mat;
    }
    public static Mat arrayToMat(double[] array) {
        return arrayToMat(array, NP.zeros(array.length));
    }

    public static Mat arrayToMat(double[][] array, Mat mat) {
        for(int i = 0; i<array.length; i++) {
            double[] row = array[i];
            for(int j = 0; j<row.length; j++) {
                mat.put(i,j,row[j]);
            }
        }
        return mat;
    }
    public static Mat arrayToMat(double[][] array) {
        return arrayToMat(array, NP.zeros(array.length, array[0].length));
    }

    public static double[][] matToArray(Mat mat) {
        double[][] array = new double[mat.rows()][];
        for(int i=0; i<mat.rows(); i++) {
            double[] row = new double[mat.cols()];
            for(int j=0; j<mat.cols(); j++) {
                row[j] = mat.get(i,j)[0];
            }
            array[i] = row;
        }
        return array;
    }

    public static double[] vectorToArray(Mat mat) {
        if(mat.rows() !=1 && mat.cols() != 1)
            throw new RuntimeException("Matrix parameter is required to be a vector but has a dimension of " + mat.rows() + "x" + mat.cols() + ".");
        double[] array = new double[Math.max(mat.rows(), mat.cols())];
        for(int i=0; i<mat.rows(); i++) {
            for(int j=0; j<mat.cols(); j++) {
                array[i+j] = mat.get(i,j)[0];
            }
        }
        return array;
    }

}