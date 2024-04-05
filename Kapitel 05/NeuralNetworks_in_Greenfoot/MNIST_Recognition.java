import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import au.com.bytecode.opencsv.CSVReader; 
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.LearningRule;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.learning.PerceptronLearning;
import org.neuroph.util.TransferFunctionType;

/**
 * Environment for testing neural networks under Greenfoot. A perceptron is trained to recognize 
 * the digits of the MNIST dataset. recognizes. The Neuroph library (http://neuroph.sourceforge.net) 
 * is used for this purpose.
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
public class MNIST_Recognition extends World implements LearningEventListener
{  
    protected final static int DIM_input = 784;
    protected final static int DIM_output = 10;
    protected DataSet trainingSet = new  DataSet(DIM_input, DIM_output);
    
    protected double learningRate = 0.001;
    protected double maxError = 0.00001;
    protected int maxIterations = 10000;
    protected int cnt_trainingEpoch = 0;
    
    NeuralNetwork mnistPerceptron = new Perceptron(784, 10, TransferFunctionType.STEP);

    Random random = new Random();
    
    private int displayedLine = 0;
  
    private DataSet testSet = null;
    
    private static boolean netIsTrained = false;
    
    protected JfxChartLogger jfxLogger = null;
    
    protected String logFilePath = "data\\";
    
    /**
     * Konstruktor für Objekte der Klasse MyWorld
     * 
     */
    public MNIST_Recognition()
    {    
        super(28, 28, 16);
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
    public void started() {
        System.out.println("greenfoot started ("+JfxChartLogger.getTimeStamp()+")");
        if (!netIsTrained){
            //loadANN("ANNs/mnist_perceptron.nnet","data/mnist_test200.csv");
            train("data/mnist_training1000.csv","data/mnist_test200.csv");
        }
    }

    public void act()
    {
        if (!netIsTrained) return;
        String lastKeyPressed = Greenfoot.getKey();
        if (lastKeyPressed!=null){
            if (lastKeyPressed.equals("right")){
                if (displayedLine<testSet.size()) displayedLine++;
                showInputRow(testSet,displayedLine);
                int recognizedDigit = recognizeTheDigit(mnistPerceptron,testSet,displayedLine);
                this.showText("Recognized digit="+recognizedDigit, 7,1);
            }else if (lastKeyPressed.equals("left")){
                if (displayedLine>0) displayedLine--;
                showInputRow(testSet,displayedLine);
                int recognizedDigit = recognizeTheDigit(mnistPerceptron,testSet,displayedLine);
                this.showText("Recognized digit="+recognizedDigit, 7,1);
            }
        }
    }
    
    /**
     * Trains the neural network.
     * @param trainingDataFile file with the training data.
     * @param validationDataFile file with the test data.
     */
    public void train(String trainingDataFile, String validationDataFile)
    {
        if (jfxLogger==null) {
            initJfxLogger();
        } 
        
        trainingSet = MNIST_Recognition.parseMNISTCSVLineByLine(trainingDataFile);
        testSet = MNIST_Recognition.parseMNISTCSVLineByLine(validationDataFile);
        showInputRow(trainingSet,displayedLine);
        
        // Set the learning parameters
        PerceptronLearning learningRule = (PerceptronLearning)mnistPerceptron.getLearningRule();
        learningRule.addListener(this);
        learningRule.setLearningRate(learningRate);
        learningRule.setMaxError(maxError);
        learningRule.setMaxIterations(maxIterations);

        System.out.println("start time:"+JfxChartLogger.getTimeStamp());
      
        System.out.println("Training phase... learning rate="+learningRate);
        mnistPerceptron.learn(trainingSet);
       
        System.out.println("end time: "+JfxChartLogger.getTimeStamp());
       
        int recognizedDigit = recognizeTheDigit(mnistPerceptron,testSet,displayedLine);
        this.showText("Recognized digit="+recognizedDigit, 7,1);
        
        System.out.println("Starting test...");
        testNeuralNetwork(mnistPerceptron, testSet);
       
        mnistPerceptron.save("ANNs/mnist_perceptron"+JfxChartLogger.getTimeStamp()+".nnet");
        netIsTrained= true;
    }
    
    /**
     * Loads a network from file.
     * @param nnetFile file of the neural network.
     * @param testData Data for testing the network.
     */
    public void loadANN(String nnetFile, String testData)
    {
        testSet = MNIST_Recognition.parseMNISTCSVLineByLine(testData);
        mnistPerceptron = NeuralNetwork.createFromFile(nnetFile);
        netIsTrained = true;
        
        showInputRow(testSet,displayedLine);
        int recognizedDigit = recognizeTheDigit(mnistPerceptron,testSet,displayedLine);
        this.showText("Recognized digit="+recognizedDigit, 7,1);
        
        System.out.println("Starting test...");
        testNeuralNetwork(mnistPerceptron, testSet);
    }
    
    public void showInputRow(DataSet ds, int n){
        DataSetRow dsr = ds.get(n);
        double[] input = dsr.getInput();
        for (int ix=0;ix<28;ix++){
            for (int iy=0;iy<28;iy++){
                double v=input[iy*28+ix];
                List <Activation> efs = this.getObjectsAt(ix,iy, Activation.class);
                if (!efs.isEmpty()){ 
                    Activation ef=efs.get(0);
                    ef.setBewertung(v);
                }else{
                    this.addObject(new Activation(v),ix,iy);
                }
            }
        }   
    }
    
    public static DataSet parseMNISTCSVLineByLine(String csvFilePath)
    {
        DataSet retDs = new DataSet(784,10);
        try{
            CSVReader reader = new CSVReader(new FileReader(csvFilePath), ',' , '"' , 0);
            String[] line =  reader.readNext();
            while (line != null) {
                try{
                    double[] soll_output = new double[10];
                    int label = Integer.parseInt(line[0]);
                    soll_output[label]=1.0;
                    double[] input = new double[784];
                    for (int n=1;n<=784;n++){
                        input[n-1]=Double.parseDouble(line[n])/255.0;
                    }
                    retDs.add(input,soll_output);
                }catch(NumberFormatException nfe){/* Skip rows that do not contain numbers in the first column. */}
                line = reader.readNext();
            }
        }catch(Exception e) {
            System.out.println(e.toString());
        }
        return retDs;
    }
       
    @Override
    public void handleLearningEvent(LearningEvent event) {
        PerceptronLearning lr = (PerceptronLearning)event.getSource();
        if (event.getEventType()==LearningEvent.Type.EPOCH_ENDED){
            if (cnt_trainingEpoch==0) System.out.println("epoch, error rate");
            System.out.println(cnt_trainingEpoch+","+lr.getTotalNetworkError());
            jfxLogger.append(cnt_trainingEpoch,lr.getTotalNetworkError());
            cnt_trainingEpoch++;
        }
    } 
    
    /**
     * Does the inference with the trained network.
     * @param neuralNet the ann
     * @param dataSet Data to be processed by the network.
     * @param row current row (entry in data set)
     */
    public int recognizeTheDigit(NeuralNetwork neuralNet, DataSet dataSet, int row)
    {
        DataSetRow dataRow = dataSet.getRowAt(row);
        neuralNet.setInput(dataRow.getInput());
        neuralNet.calculate();
        double[] networkOutput = neuralNet.getOutput();
        double[] desired_output = dataRow.getDesiredOutput();
        
        int s=0;
        while (s<desired_output.length && desired_output[s]==0.0)  s++; // Stelle finden, wo Output > 0
        System.out.print("desired output: " + s);
        
        int i=0; double max_output = Double.NEGATIVE_INFINITY; int max_i=-1;
        while (i<networkOutput.length){
            if (networkOutput[i]>max_output){
                max_output=networkOutput[i];
                max_i=i;
            }   
            i++;
        }
        
        System.out.print(" net output: " + max_i);
        if (max_i!=s) 
            System.out.println("  error");
        else
            System.out.println("  OK!");
        return max_i;
    }
    
    /**
     * Tests the network with a validation dataset.
     * @param neuralNet
     * @param testSet
     */
    public void testNeuralNetwork(NeuralNetwork neuralNet, DataSet testSet) {
        int cnt_right = 0;
        int cnt_wrong = 0;
        int cnt_random_right = 0;
        int cnt_random_wrong = 0;
        int cnt_record = 0;

        System.out.println("record num,target-output,net-output,correct,wrong,wrong %,random,random correct,random wrong,random wrong %");
        for (DataSetRow dataRow : testSet.getRows()) {
            neuralNet.setInput(dataRow.getInput());
            neuralNet.calculate();
            double[] networkOutput = neuralNet.getOutput();         
            double[] soll_output = dataRow.getDesiredOutput();
        
            int s=0;
            while (s<soll_output.length && soll_output[s]==0.0)  s++; // Stelle finden, wo Output > 0
            
            int i=0; double max_output = Double.NEGATIVE_INFINITY; int max_i=-1;
            while (i<networkOutput.length){
                if (networkOutput[i]>max_output){
                    max_output=networkOutput[i];
                    max_i=i;
                }   
                i++;
            }
            
            if (s==max_i){
                cnt_right++;
            }else{
                cnt_wrong++;
            }
            
            int z=random.nextInt(10);
            if (s==z){
                cnt_random_right++;
            }else{
                cnt_random_wrong++;
            }

            System.out.println(cnt_record+","+s+","+max_i+","+cnt_right+","+cnt_wrong+","+((double)(100*cnt_wrong))/(cnt_right+cnt_wrong)+","+z+","+cnt_random_right+","+cnt_random_wrong+","+((double)(100*cnt_random_wrong))/(cnt_random_right+cnt_random_wrong));
            cnt_record++;
        }
    }
    
    public void testNeuralNetwork(String nnetFile, DataSet testSet){
        NeuralNetwork neuralNet = NeuralNetwork.createFromFile(nnetFile);
        testNeuralNetwork(neuralNet, testSet);
    }
    
    
}