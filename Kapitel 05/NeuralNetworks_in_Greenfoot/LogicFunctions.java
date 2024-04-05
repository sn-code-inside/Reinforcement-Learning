import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.io.FileReader;
import java.util.List;
import java.util.Arrays;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.LearningRule;

/**
 * Environment for testing neural networks in Greenfoot. Initial example with a training of logic
 * functions. The Neuroph library (http://neuroph.sourceforge.net) is used for this.
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
public class LogicFunctions extends World implements LearningEventListener
{
    protected final static int DIM_input = 2;
    protected final static int DIM_output = 1;
    protected DataSet trainingSet = new  DataSet(DIM_input, DIM_output);
    
    protected double learningRate = 0.05;
    protected double maxError = 0.001;
    protected int maxIterations = 50000;
    protected int cnt_trainingEpoch = 0;
    
    public static int EVALUATION_INTERVAL = 1000; 
       
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
    
    public LogicFunctions() 
    {    
        super(4, 4, 16); 

        // generate training data
        trainingSet.add (new DataSetRow (new double[]{1, 0},  new double[]{1})); 
        trainingSet.add (new DataSetRow (new double[]{0, 1},  new double[]{1})); 
        trainingSet.add (new DataSetRow (new double[]{0, 0},  new double[]{0})); 
        trainingSet.add (new DataSetRow (new double[]{1, 1},  new double[]{0})); 
        
        // create neural network
        //NeuralNetwork neuralNetwork = new Perceptron(DIM_input,DIM_output);
        MultiLayerPerceptron neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, 2, 2, 1);
        
        // test untrained
        testNeuralNetwork(neuralNetwork);
        BackPropagation learningRule = null;
        if (neuralNetwork.getClass()==MultiLayerPerceptron.class){
            learningRule = (BackPropagation)neuralNetwork.getLearningRule();
            learningRule.addListener(this);
            learningRule.setLearningRate(learningRate);
            learningRule.setMaxError(maxError);
            learningRule.setMaxIterations(maxIterations);
        }
        initJfxLogger();
        
        System.out.println("start time:"+JfxChartLogger.getTimeStamp());
      
        System.out.println("Training phase... ");
        neuralNetwork.learn(trainingSet);

        String et = JfxChartLogger.getTimeStamp();
        System.out.println("end time: "+et);
        jfxLogger.append("end time;"+et);
        if (learningRule!=null){
             System.out.println("Iterations performed :"+learningRule.getCurrentIteration()+" Remaining error:"+learningRule.getTotalNetworkError());
        }
        // test
        testNeuralNetwork(neuralNetwork);
        
        // save trained net
        neuralNetwork.save("ANNs/logic_perceptron.nnet");
    }
    
    /**
     * This initializes the data logger.
     */
    private void initJfxLogger(){
        jfxLogger = new JfxChartLogger(logFilePath,"Learning progress of the neural network","remaining error","iterations","error");
        
        jfxLogger.appendln("attributes:");    
        jfxLogger.appendln("input dim;"+DIM_input);
        jfxLogger.appendln("output dim;"+DIM_output);
        jfxLogger.appendln("number of training data elements;"+trainingSet.size());
  
        jfxLogger.appendln("learning parameter:");  
        jfxLogger.appendln("Learning rate;"+learningRate);
        jfxLogger.appendln("max. errorr;"+maxError);
        
        jfxLogger.append("logger start time;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n"); 
    }

    @Override
    public void handleLearningEvent(LearningEvent event) {
        BackPropagation bp = (BackPropagation)event.getSource();
        if (event.getEventType() != LearningEvent.Type.LEARNING_STOPPED){
            int i = bp.getCurrentIteration();
            if (i%EVALUATION_INTERVAL==0) {
                System.out.println("iteration "+i+": "+ bp.getTotalNetworkError());
                jfxLogger.append(i,bp.getTotalNetworkError());
            }
        }
    }    
    
    public void testNeuralNetwork(String nnetFile) {
       NeuralNetwork neuralNet = NeuralNetwork.createFromFile(nnetFile);
       testNeuralNetwork(neuralNet);
    }
    
    public void testNeuralNetwork(NeuralNetwork neuralNet) {
        DataSet testSet = new  DataSet(2); 
        testSet.add (new DataSetRow (new double[]{0, 0})); 
        testSet.add (new DataSetRow (new double[]{1, 0})); 
        testSet.add (new DataSetRow (new double[]{0, 1})); 
        testSet.add (new DataSetRow (new double[]{1, 1})); 
        
        removeObjects(getObjects(Activation.class));
        int row = 0;
        for (DataSetRow testDataRow : testSet.getRows()) {
            double[] input = testDataRow.getInput();
            neuralNet.setInput(input);
            System.out.print("Input: " + Arrays.toString(input));
            this.addObject(new Activation(input[0]),0,row);
            this.addObject(new Activation(input[1]),1,row);
            
            neuralNet.calculate();
            this.addObject(new Arrow(),2,row);
            
            double[] networkOutput = neuralNet.getOutput();
            System.out.println(" Output: " + Arrays.toString(networkOutput));
            this.addObject(new Activation(networkOutput[0]),3,row);
            
            row++;
        }
    }
}
