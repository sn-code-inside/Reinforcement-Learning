import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import au.com.bytecode.opencsv.CSVReader; 
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
 * Environment for testing neural networks under Greenfoot. A multilayer perceptron 
 * is trained that approximates a financial market curve. 
 * The Neuroph library (http://neuroph.sourceforge.net) is used for this purpose.
 *
 * Supplementary material to the book: 
 * "Reinforcement Learning From Scratch: Understanding Current Approaches - 
 * with Examples in Java and Greenfoot" by Uwe Lorenz.
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
public class FunctionApproximation extends World implements LearningEventListener
{
    private static double max_input = 370;
    private static double min_input = 0;
    private static double max_output = 13000;
    private static double min_output = 10000;
    
    protected final static int DIM_input = 1;
    protected final static int DIM_output = 1;
    protected DataSet trainingSet = new  DataSet(DIM_input, DIM_output);
    private DataSet testSet = null;
    
    public static int EVALUATION_INTERVAL = 1000; 
    
    protected double learningRate = 0.01;
    protected double maxError = 0.000001;
    protected int maxIterations = 1000000;
    protected int cnt_trainingEpoch = 0;

    protected int[] layersMLP = {1,20,1};
    protected NeuralNetwork neuralNetwork = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, layersMLP); 
    
    protected Random random = new Random();
  
    private static boolean netIsTrained = false;
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
     
    public FunctionApproximation()
    {    
        super(28, 28, 16);
    }
    
    @Override
    public void started() {  
        System.out.println("greenfoot started ("+JfxChartLogger.getTimeStamp()+")");
        if (!netIsTrained){
            train("data/[2018-11-01_bis_2019-10-31]_DAX_Performance-Index.csv","data/[2018-11-01_bis_2019-10-31]_DAX_Performance-Index.csv");
        }
    }
    
    /**
     * This initializes the data logger.
     */
    private void initJfxLogger(){
        jfxLogger = new JfxChartLogger(logFilePath,"Regression with MLP "+Arrays.toString(layersMLP),"prediction","day","performance");
        
        jfxLogger.appendln("attributes:");    
        jfxLogger.appendln("input dim;"+DIM_input);
        jfxLogger.appendln("output dim;"+DIM_output);
        jfxLogger.appendln("number of training data elements;"+trainingSet.size());
  
        jfxLogger.appendln("learning parameter:");  
        jfxLogger.appendln("learning rate;"+learningRate);
        jfxLogger.appendln("max. errorr;"+maxError);
        
        jfxLogger.append("logger start time;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n"); 
    }
    
    /**
     * Trains the neural network.
     * @param trainingDataFile file with the training data.
     * @param validationDataFile file with the test data.
     */
    public void train(String trainingDataFile, String validationDataFile){
        neuralNetwork.randomizeWeights();
        System.out.println("Load data ... ");
        trainingSet = FunctionApproximation.parseCSVLineByLine(trainingDataFile);
        testSet = FunctionApproximation.parseCSVLineByLine(validationDataFile);
        
        // Einstellen der Lernparameter
        BackPropagation learningRule = (BackPropagation)neuralNetwork.getLearningRule();
        learningRule.addListener(this);
        learningRule.setLearningRate(learningRate);
        learningRule.setMaxError(maxError);
        learningRule.setMaxIterations(maxIterations);
        
        System.out.println("MLP layers: "+ Arrays.toString(layersMLP));
        System.out.println("learning rate ="+learningRate+"  max error = "+maxError+"  max number of iterations = "+maxIterations);
       
       // testNeuralNetwork(neuralNetwork, testSet);
        String starttime = JfxChartLogger.getTimeStamp();
        System.out.println("start time:"+starttime);
      
        System.out.println("Training phase...");
        neuralNetwork.learn(trainingSet);
        
        System.out.println("training end time: "+JfxChartLogger.getTimeStamp());
        
        System.out.println("Starting test...");
        testNeuralNetwork(neuralNetwork, testSet);
       
        neuralNetwork.save("ANNs/approximation_"+starttime+".nnet");
        netIsTrained = true;
    }
    
    /**
     * Loads a network from file.
     * @param nnetFile file of the neural network.
     * @param testData Data for testing the network.
     */
    public void loadANN(String nnetFile, String testData)
    {
        testSet = FunctionApproximation.parseCSVLineByLine(testData);
        MultiLayerPerceptron neuralNetwork = (MultiLayerPerceptron)NeuralNetwork.createFromFile(nnetFile);
        netIsTrained = true;
        
        System.out.println("Starting test...");
        testNeuralNetwork(neuralNetwork, testSet);
    }
    
    public static DataSet parseCSVLineByLine(String csvFilePath)
    {
        DataSet retDs = new DataSet(1,1);
        try{
            CSVReader reader = new CSVReader(new FileReader(csvFilePath), ',' , '"' , 0);
            String[] line =  reader.readNext();
            String zahl = null;
            while (line != null) {
                try{
                    double[] input = new double[1];
                    zahl = line[0].replace(',', '.');
                    input[0]=Double.parseDouble(zahl)/4;
                    //input[0]=bereich100(Double.parseDouble(zahl),min_input,max_input);
                    double[] soll_output = new double[1];
                    zahl = line[5].replace(',', '.');
                    soll_output[0]=bereich01(Double.parseDouble(zahl),min_output,max_output);
                    retDs.add(input,soll_output);
                }catch(NumberFormatException nfe){/* Zeilen, die keine Zahlen in der ersten Spalte enthalten überspringen */}
                
                line = reader.readNext();
            }
        }catch(Exception e) {
            System.out.println(e.toString());
        }
        return retDs;
    }
       
    @Override
    public void handleLearningEvent(LearningEvent event) {
        BackPropagation lr = (BackPropagation)event.getSource();
        if (event.getEventType()==LearningEvent.Type.EPOCH_ENDED){
            if (cnt_trainingEpoch==0) System.out.println("epoch, error rate");
            if (cnt_trainingEpoch%EVALUATION_INTERVAL==0){
                System.out.println(cnt_trainingEpoch+","+lr.getTotalNetworkError());
            }
            cnt_trainingEpoch++;
        }
    } 
            
    public void testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet) {
        int cnt_record = 1;
        if (jfxLogger==null) {
            initJfxLogger();
            jfxLogger.platformAddCurve("Annual performance of the German Share Index (DAX)");
           
        }else{
            jfxLogger.clear();
        }
        
        System.out.println("record num,day,input,target-output,net-output,difference");
        for (DataSetRow dataRow : testSet.getRows()) {
            neuralNet.setInput(dataRow.getInput());
            neuralNet.calculate();
            double[] networkOutput = neuralNet.getOutput();         
            double[] soll_output = dataRow.getDesiredOutput();
            double o = originalWert(networkOutput[0],min_output,max_output);
            double s = originalWert(soll_output[0],min_output,max_output);
            int day = (int)(4*dataRow.getInput()[0]);
            System.out.println(cnt_record+","+day+","+dataRow.getInput()[0]+","+s+","+o+","+(s-o));
            jfxLogger.append(day,o);
            jfxLogger.append(day,s,1,"");
            cnt_record++;
        }
        
        System.out.println("extrapolation");
        for (int i=1;i<30;i++){
            neuralNet.setInput(bereich100((double)365+i,min_input,max_input));
            neuralNet.calculate();
            double[] networkOutput=neuralNet.getOutput();
            double o = originalWert(networkOutput[0],min_output,max_output);
            System.out.println(cnt_record+",prediction:,"+o);
            cnt_record++;
        }
        
       
        
    }
    
    private static double bereich100(double wert, double min, double max){
        return (100*(wert-min))/(max-min);
    }
    
    private static double bereich01(double wert, double min, double max){
        return (wert-min)/(max-min);
    }
    
    private static double originalWert(double wert01, double min, double max)
    {
        return wert01*(max-min)+min;
    }
    
    public void testNeuralNetwork(String nnetFile, DataSet testSet){
        NeuralNetwork neuralNet = NeuralNetwork.createFromFile(nnetFile);
        testNeuralNetwork(neuralNet, testSet);
    }
}
