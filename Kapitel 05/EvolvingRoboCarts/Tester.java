import java.util.ArrayList;
import java.util.Arrays;

public class Tester {
  private NeuralNetwork netw;
  private GeneticAlgorithm genAlg;
  private int[] testNetworkLayers = {3,2,1};
  
  public Tester(){
      init();
      testNet();
  }
 
  public void init() {
    netw = new NeuralNetwork(testNetworkLayers);
    genAlg = new GeneticAlgorithm();
    genAlg.generateNewGenePool(1, NeuralNetwork.calcNumberOfWeights(testNetworkLayers));
  }

  public void testNet() { 
    Genome genome = generateTestGenome(NeuralNetwork.calcNumberOfWeights(testNetworkLayers));
    checkEquality("size of genome",11,genome.weights.size());
    
    netw.createWeightsFromGenome(genome);
    checkEquality("number of wheights",11, netw.getNumberOfWeights() );
    
    double[] testInput1 = {1.0,1.0,1.0};
    netw.setInput(testInput1);
    double[] outputs = netw.getOutput();
    checkIfDifferenceIsSmall("net output without activation function",0.90731,outputs[0],0.00001); 
    
    System.out.println("Network weights=\n"+netw.toString());
    
    genAlg.generateNewGenePool(100, 11);
    checkEquality("genes generated",100,genAlg.getGenePool().size());
    
    ArrayList <Genome> best = genAlg.getBestGenomes(4);
    Genome gen1 = genAlg.getGenePool().get(0);
    Genome gen2 = genAlg.getGenePool().get(1);
    System.out.println("Gen1="+gen1.weights.toString());
    System.out.println("Gen2="+gen2.weights.toString());
    genAlg.mutate(gen1);
    System.out.println("mutated Gen1="+gen1.weights.toString());
    ArrayList <Genome> crossOverList = genAlg.crossOver(gen1,gen2);
    for (Genome gen : crossOverList)
        System.out.println("crossov Gene="+gen.weights.toString());
  }
  
  public Genome generateTestGenome(int totalWeights){
        Genome genome = new Genome();
        genome.ID = 0;
        genome.fitness = 0.0;
        genome.weights = new ArrayList<Double>();
        double w=0.0;
        for (int j = 0; j < totalWeights; j++) {
            genome.weights.add(w);
            w=w+0.1;
        }
        return genome;
  }
  
  /**
   * Print testresult.
   */
  private void checkEquality(String text, int expectedValue, int result) {
      System.out.println("Test '"+text+"'("+expectedValue+"=="+result+") : "+((expectedValue==result) ? "passed" : "failed"));
  }
  
  private void checkIfDifferenceIsSmall(String text, double expectedValue, double result, double maxDiff) {
      System.out.println("Test '"+text+"'("+expectedValue+"=="+result+") : "+((expectedValue-result)<maxDiff ? "passed" : "failed"));
  }
}