import greenfoot.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A fully connected Neural Network (Multilayer Perceptron). Contains a method to produce a GreenfootImage visualization.
 * 
 * Supplementary material to the book: 
 * "Reinforcement Learning From Scratch: Understanding Current Approaches - with Examples in Java and Greenfoot" by Uwe Lorenz.
 * https://link.springer.com/book/10.1007/978-3-031-09030-1
 * 
 * Ausgabe auf Deutsch: https://link.springer.com/book/9783662683101
 * 
 * Licensing CC-BY-SA 4.0 
 * Attribution - Sharing under the same conditions
 * 
 * www.facebook.com/ReinforcementLearningJava
 * github.com/sn-code-inside/Reinforcement-Learning
 *
 * www.x-ai.eu
 * 
 * @author Uwe Lorenz
 * @version 1.2 (14.11.2023)
 */
public class NeuralNetwork {
    private int[] size_of_layer = null;
    private double[] inputs = null;
    private double[] outputs = null;
    
    private boolean lapsed = true;
    ArrayList <double[][]> network_weights = null;
    
    public static boolean ANN_SHOW_ACTIVATIONS_REALTIME = true;
    ArrayList <IntegerVec2D> neuronXY = new ArrayList <IntegerVec2D> (); // Positions of the neurons in the image, will be set, when an image is created 
                                                                         // and the flag ANN_SHOW_ACTIVATIONS is true.
                                                                         
    ArrayList <Integer> neuronDiameters = new ArrayList <Integer> ();    // Diameters of the neurons in the image, depending on the input activation 
                                                                         // calculated during output the calculation.
    
    private int d_min=10, d_max=20; // Min/max diameters of the neurons in the image. (Defaults, will be overwritten when an image is created.)
    
    public NeuralNetwork(int[] size_of_network_layers) {
       this.size_of_layer = size_of_network_layers;
       inputs = new double[size_of_layer[0]];
       outputs = new double[size_of_layer[size_of_network_layers.length-1]];
    }

    /** 
     * Set a new input vector to the neural network.
     * @param x inputs 
     */
    public void setInput( double[] x) {
        if (x.length != size_of_layer[0]) { 
            System.out.println("Size of the inputvector ("+x.length+") does not match with the size of the network input layer ("+size_of_layer[0]+") !"); 
            return;
        }
        inputs = x.clone();
        lapsed = true;
    }
    
    /**
     * Calcualtes and gets the current network output.
     * @return output vector of the network
     */
    public double[] getOutput(){
        if (lapsed) outputs=calculate();
        return outputs;
    }
    
    /**
     * Defines the used activation function.
     * @param x net input of the neuron (net_j)
     * @return activation
     */
    public double activation_function(double x){
        return ToolKit.Sigmoid(x, 1.0);
    }
    
   
    /**
     * Calculates the output of the network.
     * @return activations of the output neurons
     */
    public double[] calculate(){
        if (network_weights==null) {
            System.out.println("Network connections were not created! May have to created from genome.");
            return null;
        }
        if (ANN_SHOW_ACTIVATIONS_REALTIME) neuronDiameters.clear();
        double[] layer_inputs = addBiasToLayerInput(inputs);
        double[] layer_outputs=null;
        ArrayList <Integer> layerDiameters = new ArrayList <Integer> ();
        for (int layer=1;layer<size_of_layer.length;layer++){
            if (ANN_SHOW_ACTIVATIONS_REALTIME){
                layerDiameters.clear();
                for (double input : layer_inputs){
                    Integer d_raw  = visCalcDiameter(input);
                    layerDiameters.add(d_raw);
                }
                int id_max = getIListMaxAbs(layerDiameters);
                double ratio = (double)(d_max)/(double)(id_max);
                for (Integer id : layerDiameters)
                    neuronDiameters.add((int)(id*ratio+0.5));
            }
            // Multiply weight matrix with input vector for getting the outputs (with activation function).
            layer_outputs = new double[size_of_layer[layer]];
            double[][] weights = network_weights.get(layer-1);
            for (int m=0;m<layer_outputs.length;m++){
                double sum_input = 0.0;
                for (int n=0;n<layer_inputs.length;n++){
                    sum_input += weights[m][n]*layer_inputs[n];
                }
                layer_outputs[m]=activation_function(sum_input);
            }
            layer_inputs=addBiasToLayerInput(layer_outputs);
        }
        lapsed = false;
        if (ANN_SHOW_ACTIVATIONS_REALTIME){
            layerDiameters.clear();
            for (double input : layer_outputs){
                Integer d_raw  = visCalcDiameter(input);
                layerDiameters.add(d_raw);
            }
            int id_max = getIListMaxAbs(layerDiameters);
            double ratio = (double)(d_max)/(double)(id_max);
            for (Integer id : layerDiameters)
                neuronDiameters.add((int)(id*ratio+0.5));
        }
        outputs = layer_outputs;
        return layer_outputs;
    }
    
    /** 
     * Calculates the diameter of a neuron in the image from activation value.
     */
    private int visCalcDiameter(double activation){
        return (int)(10*Math.sqrt(activation)+d_min);
    }
    
    /**
     * The method is neccesary in this program, because we need an input[0] fixed with 1.0 (bias).
     * @param inputs array containing the input variables
     * @
     */
    private double[] addBiasToLayerInput(double[] inputs){
        double[] inputsWithBias=new double[inputs.length+1];
        inputsWithBias[0]=1.0; // bias "input" is always 1
        for (int i=0;i<inputs.length;i++) inputsWithBias[i+1]=inputs[i];
        return inputsWithBias;
    }

    /**
     * The neural network receives weights from the genome. That's why no learning rule like 
     * backpropagation is needed here.
     */
    public void createWeightsFromGenome(Genome genome) {
        if (genome == null) return;
        network_weights = new ArrayList<double[][]>(); // List of matices
        int counter=0;
        for (int layer=1; layer<size_of_layer.length; layer++){
            double[][] weights = new double[size_of_layer[layer]][size_of_layer[layer-1]+1]; // +1 because of BIAS
            for (int m=0;m<weights.length;m++){ // outputs "to neuron"
                for (int n=0;n<weights[0].length;n++){ // inputs "from neuron"
                    weights[m][n]=genome.weights.get(counter);
                    counter++;
                }
            }
            network_weights.add(weights.clone());
        }
    }
    
    public ArrayList <IntegerVec2D> getNeuronXY(){
        return neuronXY;
    }
    
    /** 
     * Gets the number of weights from the existing weight matrices.
     */
    public int getNumberOfWeights(){
        int sum=0;
        for (double[][]w : network_weights){
            sum+=w[0].length*w.length;
        }
        return sum;
    }
    
    /**
     * Calculates the number of weights from the given layer sizes (fully connected).
     * @param size_of_network_layers Array containing the size of the layers of th neural network. The first layer 0 is the input layer and 
     *        the last one istthe output layer.
     */
    public static int calcNumberOfWeights(int[] size_of_network_layers){
        int sum=0;
        for (int i=0;i<size_of_network_layers.length-1;i++){
            sum += (size_of_network_layers[i]+1)*size_of_network_layers[i+1]; // for each matrix: (input_size+1)*output_size
        }        
        return sum;
    }
    
    /**
     * Gets the nominal maximum value in a matrix (for normalization).
     */
    private double getAbsMatrixMax(double[][] w){
        double w_max= Double.NEGATIVE_INFINITY;
        for (int i=0;i<w.length;i++)
            for (int j=0;j<w[0].length;j++)
                if (Math.abs(w[i][j])>w_max) w_max=Math.abs(w[i][j]);
        return w_max;
    }
    
    private int getIListMaxAbs(ArrayList <Integer> li){
        Integer i_max = Integer.MIN_VALUE;
        for (Integer i : li)
                if (iabs(i)>i_max) i_max=iabs(i);
        return i_max;
    }
    
    private int iabs(int i){
        return (i<0) ? -i : i;
    }
    
    /**
     * Produces a Greenfoot image of the network.
     * @param width
     * @param height
     * @param disp_activations Set true to display neuron activations live (see GlassPane).
     */
    public GreenfootImage produceVisualizationImage(int width, int height, boolean disp_activations){
        final int THICKNESS_MAX = 4; 
        neuronXY.clear();
        GreenfootImage img = new GreenfootImage(width,height);
        int cnt_layer=0;
        int num_layers = network_weights.size()+1;
        int num_neurons_max = -1;
        int x_from_neuron_prev = 0;
        int[] y_from_neuron_prev = null;
        for (double[][]w : network_weights){
            if (w[0].length>num_neurons_max){ 
                 num_neurons_max=w[0].length;
            }
        }        
        d_max = (3*height)/(4*num_neurons_max);
        d_min = height/(4*num_neurons_max);
        int x = width/(2*num_layers);
        int dist_x = width/num_layers; 
        int num_output = 0;
        double[][] w_prev=null;
        for (double[][]w : network_weights){
            int y = height/(2*w[0].length);
            int dist_y = height/w[0].length;
            int x_from_neuron = x;
            int[] y_from_neuron = new int[w[0].length];
            // weights
            for (int i=0;i<w[0].length;i++){
                y_from_neuron[i]=y;
                if ((y_from_neuron_prev!=null)&&(i>0)){
                    for (int j=0;j<y_from_neuron_prev.length;j++){ 
                        w_prev = network_weights.get(cnt_layer-1);
                        double w_max = getAbsMatrixMax(w_prev);
                        double weight = w_prev[i-1][j];
                        if (weight<0){
                            img.setColor(Color.BLUE);
                        }else{
                            img.setColor(Color.RED);
                        }
                        drawThickLine(img,x_from_neuron_prev,y_from_neuron_prev[j],x,y,(int)(Math.abs(THICKNESS_MAX*weight)/w_max+0.5));
                    }   
                }
                y+= dist_y;
            }
            // draw neurons
            if (y_from_neuron_prev!=null){
                for (int j=0;j<y_from_neuron_prev.length;j++){  
                    if (j>0){
                        img.setColor(Color.ORANGE);
                    }else{
                        img.setColor(Color.RED);
                    }
                    if (disp_activations){
                        neuronXY.add(new IntegerVec2D(x_from_neuron_prev,y_from_neuron_prev[j]));
                        img.drawOval(x_from_neuron_prev-d_max/2,y_from_neuron_prev[j]-d_max/2, d_max, d_max);
                    }else{
                        img.fillOval(x_from_neuron_prev-d_max/2,y_from_neuron_prev[j]-d_max/2, d_max, d_max);
                    }
                }
            } 
            x_from_neuron_prev = x_from_neuron;
            y_from_neuron_prev = y_from_neuron;  
            num_output = w.length;
            x+= dist_x; 
            cnt_layer++;
        }
        
        // Outputlayer
        int y = height/(2*num_output);
        int dist_y = height/num_output;
        w_prev=network_weights.get(cnt_layer-1);
        for (int i=0;i<num_output;i++){
            if (y_from_neuron_prev!=null){
                for (int j=0;j<y_from_neuron_prev.length;j++){
                    double w_max = getAbsMatrixMax(w_prev);
                    double weight = w_prev[i][j];
                    if (weight<0){
                        img.setColor(Color.BLUE);
                    }else{
                        img.setColor(Color.RED);
                    }
                    drawThickLine(img,x_from_neuron_prev,y_from_neuron_prev[j],x,y,(int)(Math.abs(THICKNESS_MAX*weight)/w_max+0.5));
                    if (i==0){
                        if (j>0){
                            img.setColor(Color.ORANGE);
                        }else{
                            img.setColor(Color.RED);
                        }
                        if (disp_activations){
                            neuronXY.add(new IntegerVec2D(x_from_neuron_prev,y_from_neuron_prev[j]));
                            img.drawOval(x_from_neuron_prev-d_max/2,y_from_neuron_prev[j]-d_max/2, d_max, d_max);
                        }else{
                            img.fillOval(x_from_neuron_prev-d_max/2,y_from_neuron_prev[j]-d_max/2, d_max, d_max);
                        }
                    }
                }   
            }
            img.setColor(Color.ORANGE);
            if (disp_activations){
                neuronXY.add(new IntegerVec2D(x,y));
                img.drawOval(x-d_max/2,y-d_max/2, d_max, d_max);
            }else{
                img.fillOval(x-d_max/2,y-d_max/2, d_max, d_max);
            }
            y+= dist_y;
        } 
        System.out.println(this.toString());
        return img;
    }
    
    /**
     * @return list with the netinputs of the neurons.
     */
    public ArrayList <Integer> getNeuronDiameters(){
        return this.neuronDiameters;
    }
    
    /**
     * Draws a line into the greenfoot image with the given thickness.
     */
    private void drawThickLine(GreenfootImage img, int x0, int y0, int x1, int y1, int thickness){
        int xf=x0-thickness/2;
        int yf=y0-thickness/2;
        int xt=x1-thickness/2;
        int yt=y1-thickness/2;
        for(int i=0;i<thickness;i++){
            for(int j=0;j<thickness;j++){
                img.drawLine(xf+i,yf+j,xt+i,yt+j);
            }
        }
    }

    public String toString(){
        String ret="";
        int cnt_layer=0;
        for (double[][]w : network_weights){
            ret+="layer:"+cnt_layer+"\n";
            for(int i=0;i<w.length;i++){
                ret+="weights to neuron "+i+" in layer "+(cnt_layer+1)+"\n";
                ret+=Arrays.toString(w[i])+"\n";
            }
            cnt_layer++;
        }
        return ret;
    }
}
