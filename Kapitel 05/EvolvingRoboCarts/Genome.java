import java.util.ArrayList;

/**
 * Genome object simply keeps 2 important info: fitness and weights. 
 * The fitness is the distance that how long the car could go. The weights are
 * a list of Sigmoid values which is from -1 to 1.
 * 
 * @author Hai Nguyen Ho
 * @version 18 Nov 2016
 * 
 * https://github.com/nguyen124/AutoDrivingCarUsingNeuronNetwork
 */
public class Genome {
    public int ID;
    public double fitness;
    public ArrayList<Double> weights;
}