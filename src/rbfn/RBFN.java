package rbfn;

/*
Manually translated from Python code
https://github.com/mrthetkhine/RBFNeuralNetwork
*/

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.*;
import java.util.*;

public class RBFN {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary("opencv_java342");
    }

    private final int no_of_input;
    private final int no_of_hidden;
    private final int no_of_output;
    private Mat input;
    private final Mat centroid;
    private final Mat sigma;
    private Mat hidden_output;
    private final Mat hidden_to_output_weight;
    private Mat output;
    private final Mat output_bias;
    private Mat actual_target_values;
    private final double learningRate;
    private double mean_error;
    private Mat error_of_output_layer;
    private double total;

    public RBFN(int no_of_input, int no_of_hidden, int no_of_output) {
        this.no_of_input = no_of_input;
        this.no_of_hidden = no_of_hidden;
        this.no_of_output = no_of_output;
        this.input = NP.zeros(this.no_of_input);
        this.centroid = NP.zeros(this.no_of_hidden, this.no_of_input);
        this.sigma = NP.zeros(this.no_of_hidden);
        this.hidden_output = NP.zeros(this.no_of_hidden);
        this.hidden_to_output_weight = NP.zeros(this.no_of_hidden, this.no_of_output);
        this.output = NP.zeros(this.no_of_output);
        this.output_bias = NP.zeros(this.no_of_output);
        this.actual_target_values = NP.zeros(this.no_of_output);
        this.learningRate = 0.0262;
        this.setup_center();
        this.setup_sigma_spread_radius();
        this.set_up_hidden_to_output_weight();
        this.set_up_output_bias();
    }

    public RBFN(RBFNSavedInstance rbfnSavedInstance) {
        this(rbfnSavedInstance.no_of_input, rbfnSavedInstance.no_of_hidden, rbfnSavedInstance.no_of_output);
        RBFNSavedInstance.arrayToMat(rbfnSavedInstance.centroid, this.centroid);
        RBFNSavedInstance.arrayToMat(rbfnSavedInstance.sigma, this.sigma);
        RBFNSavedInstance.arrayToMat(rbfnSavedInstance.hidden_to_output_weight, this.hidden_to_output_weight);
        RBFNSavedInstance.arrayToMat(rbfnSavedInstance.output_bias, this.output_bias);
    }

    public void save(File outputfile, Mat means, Mat stdDevs, List<Character> labels) throws IOException {
        RBFNSavedInstance savedInstance = new RBFNSavedInstance();
        savedInstance.no_of_input = no_of_input;
        savedInstance.no_of_hidden = no_of_hidden;
        savedInstance.no_of_output = no_of_output;
        savedInstance.centroid = RBFNSavedInstance.matToArray(centroid);
        savedInstance.sigma = RBFNSavedInstance.vectorToArray(sigma);
        savedInstance.hidden_to_output_weight = RBFNSavedInstance.matToArray(hidden_to_output_weight);
        savedInstance.output_bias = RBFNSavedInstance.vectorToArray(output_bias);
        savedInstance.means = RBFNSavedInstance.vectorToArray(means);
        savedInstance.stdDevs = RBFNSavedInstance.vectorToArray(stdDevs);
        savedInstance.labels = labels.toArray(new Character[]{});
        ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(outputfile));
        output.writeObject(savedInstance);
        output.close();
    }

    /**
     * Setup center using clustering ,for now just randomize between 0 and 1
     */
    private void setup_center() {
        Core.randu(this.centroid,0.0,1.0);
    }

    private void setup_sigma_spread_radius() {
        for(int i=0; i<this.no_of_hidden; i++) {
            Mat center = this.centroid.row(i);
            this.sigma.put(i,0, this.set_up_sigma_for_center(center));
        }
    }

    private double set_up_sigma_for_center(Mat center) {
        int p = this.no_of_hidden / 3;
        double sigma = 0.0;
        ArrayList<Double> distances = new ArrayList<>(this.no_of_hidden);
        for (int i = 0; i < this.no_of_hidden; i++) {
            distances.add(RBFN.eclidean_distance(center, this.centroid.row(i)));
        }

        double sum = 0.0;
        for (int i = 0; i < p; i++) {
            int nearest = RBFN.get_smallest_index(distances);
            distances.set(nearest, Double.POSITIVE_INFINITY);

            Mat neightbour_centroid = this.centroid.row(nearest);
            for(int j=0; j<no_of_input; j++) {
                sum += Math.pow(center.get(0,j)[0] - neightbour_centroid.get(0,j)[0], 2);
            }
        }

        sigma = Math.sqrt(sum / p);
        return sigma;
    }

    private static Double eclidean_distance(Mat x, Mat y) {
        return Core.norm(x,y);
    }

    private static int get_smallest_index(ArrayList<Double> distances) {
        int min_index = 0;
        for(int i=1; i<distances.size(); i++){
            if(distances.get(min_index) > distances.get(i)){
                min_index = i;
            }
        }
        return min_index;
    }

    private void set_up_hidden_to_output_weight() {
        Core.randu(this.hidden_to_output_weight,0.0,1.0);
    }

    private void set_up_output_bias() {
        Core.randu(this.output_bias,0.0,1.0);
    }

    public double train(Pattern pattern) {
        Mat input = pattern.getInput();
        this.actual_target_values = pattern.getOutput();
        this.pass_input_to_network(input);
        double error = this.get_error_for_pattern();
        //System.out.println("pattern " + pattern.getPattern_id() + " Accuracy " + (1-error));
        this.gradient_descent();
        return error;
    }

    public double pass_one_epoch(Data data) {
        System.out.println("pass one epoch");
        double all_error = 0.0;
        ArrayList<Integer> all_index = new ArrayList<>();
        for(int i=0; i<data.getPatterns().size(); i++) {
            all_index.add(i);
        }
        for(int i=0; i<data.getPatterns().size(); i++) {
            int random_index = (int)(NP.randomUniform() * all_index.size());
            //Get a random pattern to train
            Pattern pattern = data.getPatterns().get(all_index.get(random_index));
            all_index.remove(random_index);
            System.out.println(pattern.getPattern_id());
            Mat input = pattern.getInput();
            this.actual_target_values = pattern.getOutput();
            this.pass_input_to_network(input);

            double error = this.get_error_for_pattern();
            all_error += error;
            this.gradient_descent();
        }
        return all_error / data.getPatterns().size();
    }

    private void pass_input_to_network(Mat input) {
        this.input = input;
        this.pass_to_hidden_node();
        this.pass_to_output_node();
    }

    private void pass_to_hidden_node() {
        this.hidden_output = NP.zeros(this.no_of_hidden);
        for(int i=0; i<this.no_of_hidden; i++) {
            double euclid_distance = Math.pow(RBFN.eclidean_distance(this.input.t(), this.centroid.row(i)), 2);
            this.hidden_output.put(i,0,Math.exp(- (euclid_distance / (2 * Math.pow(this.sigma.get(i,0)[0], 2)))));
        }
    }

    private void pass_to_output_node() {
        this.output = NP.zeros(no_of_output);
        double total = 0.0;
        for(int i=0; i<this.no_of_output; i++) {
            double output_value = 0;
            for(int j=0; j<this.no_of_hidden; j++) {
                output_value += this.hidden_to_output_weight.get(j,i)[0] * this.hidden_output.get(j,0)[0];
            }
            output_value += this.output_bias.get(i,0)[0];//added bias
            this.output.put(i,0, output_value);
        }
        //Normalize
        for(int i=0; i<this.no_of_output; i++) {
            total += this.output.get(i,0)[0];
        }
        for(int i=0; i<this.no_of_output; i++) {
            total += this.output.put(i,0, this.output.get(i,0)[0] / total);
        }
        this.total = total;
    }

    /**
     * Compute error for the pattern
     */
    private double get_error_for_pattern() {
        double error = 0.0;
        for(int i=0; i<this.output.rows(); i++) {
            error += Math.pow(this.actual_target_values.get(i,0)[0] - this.output.get(i,0)[0], 2);
        }
        return error;
    }

    /**
     * Weight update by gradient descent algorithm
     */
    private double gradient_descent() {
        // compute the error of output layer
        this.mean_error = 0.0;
        this.error_of_output_layer = NP.zeros(this.no_of_output);
        for(int i=0; i<this.no_of_output; i++) {
            this.error_of_output_layer.put(i,0, this.actual_target_values.get(i,0)[0] - this.output.get(i,0)[0]);
            this.mean_error += Math.pow(this.actual_target_values.get(i,0)[0] - this.output.get(i,0)[0], 2) / 2;
        }

        // Adjust hidden to output weight
        for(int o=0; o<this.no_of_output; o++){
            for(int h=0; h<this.no_of_hidden; h++){
                double delta_weight = this.learningRate * this.error_of_output_layer.get(o,0)[0] * this.hidden_output.get(h,0)[0];
                this.hidden_to_output_weight.put(h,o, this.hidden_to_output_weight.get(h,o)[0] + delta_weight);
            }
        }

        // For bias
        for(int o=0; o<this.no_of_output; o++){
            double delta_bias = this.learningRate * this.error_of_output_layer.get(o,0)[0];
            this.output_bias.put(o,0, this.output_bias.get(o,0)[0] + delta_bias);
        }

        // Adjust center / input to hidden weight
        for(int i=0; i<this.no_of_input; i++) {
            for(int j=0; j<this.no_of_hidden; j++) {
                double summ = 0.0;
                for(int p=0; p<this.no_of_output; p++) {
                    summ += this.hidden_to_output_weight.get(j,p)[0] * (this.actual_target_values.get(p,0)[0] - this.output.get(p,0)[0]);
                }

                double second_part = (this.input.get(i,0)[0] - this.centroid.get(j,i)[0]) / Math.pow(this.sigma.get(j,0)[0], 2);
                double delta_weight = (this.learningRate * this.hidden_output.get(j,0)[0] * second_part * summ);
                this.centroid.put(j,i, this.centroid.get(j,i)[0] + delta_weight);
            }
        }

        // Adjust sigma and spread radius
        for(int i=0; i<this.no_of_input; i++){
            for(int j=0; j<this.no_of_hidden; j++){
                double summ = 0;
                for(int p=0; p<this.no_of_output; p++)
                    summ += this.hidden_to_output_weight.get(j,p)[0] * (this.actual_target_values.get(p,0)[0] - this.output.get(p,0)[0]);

                double second_part = Math.pow(this.input.get(i,0)[0] - this.centroid.get(j,i)[0], 2) / Math.pow(this.sigma.get(j,0)[0], 3);
                double delta_weight = 0.1 * this.learningRate * this.hidden_output.get(j,0)[0] * second_part * summ;
                this.sigma.put(j,0, this.sigma.get(j,0)[0] + delta_weight);
            }
        }
        return this.mean_error;
    }

    public double get_accuracy_for_training(Data data) {
        double correct = 0.0;
        for(int i=0; i<data.getPatterns().size(); i++){
            Pattern pattern = data.getPatterns().get(i);
            this.pass_input_to_network(pattern.getInput());
            Mat n_output = this.output;
            Mat act_output = pattern.getOutput();
            int n_neuron = this.get_fired_neuron(n_output);
            int a_neuron = this.get_fired_neuron(act_output);
            System.out.println("for id " + pattern.getPattern_id() + " predicted " + n_neuron + "(" + data.getLabels().get(n_neuron) + ") and was " + a_neuron + "(" + data.getLabels().get(a_neuron) + ").");
            System.out.println(matchLabelToPredictionOutput(this.output, data.getLabels()));
            if(n_neuron == a_neuron)
                correct += 1.0;
        }
        double accuracy = correct / data.getPatterns().size() * 100.0;
        return accuracy;
    }

    private int get_fired_neuron(Mat output) {
        int max = 0;
        for(int i=0; i<output.rows(); i++)
            if(output.get(i,0)[0] > output.get(max,0)[0])
                max = i;
        return max;
    }

    public Mat evalAndGetWeights(Mat input) {
        this.pass_input_to_network(input);
        return this.output.clone();
    }

    public int evalAndGetOutputIndex(Mat input) {
        this.pass_input_to_network(input);
        return this.get_fired_neuron(this.output);
    }

    public static SortedSet<Map.Entry<Character,Double>> matchLabelToPredictionOutput(Mat output, List<Character> orderedLabels) {
        Map<Character,Double> map = new HashMap<>();
        int index = 0;
        for(Character character : orderedLabels) {
            map.put(character, output.get(index++,0)[0]);
        }
        SortedSet<Map.Entry<Character,Double>> sortedEntries = new TreeSet<Map.Entry<Character,Double>>(
                new Comparator<Map.Entry<Character,Double>>() {
                    @Override public int compare(Map.Entry<Character,Double> e1, Map.Entry<Character,Double> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public static void main(String... args) {
        Mat v00 = new Mat(2,1,CvType.CV_64F), v01 = new Mat(2,1,CvType.CV_64F),
        v10 = new Mat(2,1,CvType.CV_64F), v11= new Mat(2,1,CvType.CV_64F);
        v00.put(0,0,0); v00.put(1,0,0);
        v01.put(0,0,0); v01.put(1,0,1);
        v10.put(0,0,1); v10.put(1,0,0);
        v11.put(0,0,1); v11.put(1,0,1);
        Pattern p1 = new Pattern(1, v00, v10);
        Pattern p2 = new Pattern(2, v01, v01);
        Pattern p3 = new Pattern(3, v10, v01);
        Pattern p4 = new Pattern(4, v11, v10);

        List<Pattern> patterns = Arrays.asList(p1, p2, p3, p4);
        List<Character> classLabels = Arrays.asList('0', '1');
        Data data = new Data(patterns, classLabels);
        RBFN rbfn = new RBFN(2, 10, 2);
        double mse = 100.0;
        for(int i=0; i<1500; i++) {
            mse = rbfn.train(data.getPatterns().get(i%4));
        }
        double accuracy = rbfn.get_accuracy_for_training(data);
        System.out.println("Total accuracy is " + accuracy);
        System.out.println("Last MSE " + mse);
    }
}
