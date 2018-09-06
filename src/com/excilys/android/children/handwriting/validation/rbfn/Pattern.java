package com.excilys.android.children.handwriting.validation.rbfn;

import org.opencv.core.Mat;

public class Pattern {
    private final long pattern_id;
    private final Mat output;
    private final Mat input;

    public Mat getInput() {
        return input;
    }

    public long getPattern_id() {
        return pattern_id;
    }


    public Mat getOutput() {
        return output;
    }

    public Pattern(long pattern_id, Mat input, Mat output) {
        this.pattern_id = pattern_id;
        this.input = input;
        this.output = output;
    }
}
