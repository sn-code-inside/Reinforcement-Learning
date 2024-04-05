import java.util.ArrayList;
import java.util.Random;
import java.text.NumberFormat;

/**
 * GeneticAlgorithm is used to train neuron network. It has a list of 
 * genomes called population. Each population will have some of the best genomes
 * with highest fitness. Fitness is the sum of distance that how far a car could go. 
 * The best genomes will be used to create other genomes by mixing them up (crossing over 
 * between 2 genomes) and mutate some of their genes. It is a little bit different with basic 
 * genetic algorithm, the mutate will not turn on and off a gene but they randomly change the weight of genes.
 * 
 * @author Hai Nguyen Ho
 * @version 18 Nov 2016
 * 
 * Licence: The Code Project Open License (CPOL) 1.02
 *
 * https://www.codeproject.com/Articles/1160551/Self-Driven-Car-Simulator-Using-a-Neural-Network-a
 * https://github.com/nguyen124/AutoDrivingCarUsingNeuronNetwork
 * 
 */
public class GeneticAlgorithm {
    public static final NumberFormat nf = NumberFormat.getInstance();
    public static final float MAX_PERMUTATION = 0.1f;
    public static final float MUTATION_RATE = 0.1f;
    private int genomeID;
    private int cnt_generation;
    private ArrayList<Genome> genePool;

    public GeneticAlgorithm() {
        genomeID = 0;
        cnt_generation = 0;
        nf.setMaximumFractionDigits(2);
        genePool = new ArrayList<Genome>();
    }

    /*
     * Generate genomes population with ID, fitness and random Sigmoid weights
     */
    public void generateNewGenePool(int totalPop, int totalWeights) {
        cnt_generation = 1;
        clearGenePool();
        for (int i = 0; i < totalPop; i++) {
            Genome genome = new Genome();
            genome.ID = genomeID;
            genome.fitness = 0.0;
            genome.weights = new ArrayList<Double>();
            for (int j = 0; j < totalWeights; j++) {
                genome.weights.add(ToolKit.RandomSigmoid());
            }
            genomeID++;
            genePool.add(genome);
        }
    }
    
    public void setGenePool(ArrayList <Genome> genomes){
        this.genePool = genomes;
    }
    
    public ArrayList <Genome> getGenePool(){
        return this.genePool;
    }
   
    public void setGenomeFitness(double fitness, int index) {
        if (index >= genePool.size() || index < 0)
            return;
        genePool.get(index).fitness = fitness;
    }

    public void clearGenePool() {
        genePool.clear();
    }

    /**
     * This function will generate new population of genomes based on best 4
     * genomes (genomes which have highest fitness). The best genomes will be
     * mixed up and mutated to create new genomes.
     */
    public void breedPopulation() {
        ArrayList<Genome> bestGenomes = new ArrayList<Genome>();
        // Find the 4 best genomes which have highest fitness.
        bestGenomes = getBestGenomes(4);
       
        // Carry on the best 4 genomes.
        ArrayList<Genome> children = new ArrayList<Genome>();
        for (Genome gene : bestGenomes){
            Genome top = new Genome();
            top.fitness = 0.0;
            top.ID = gene.ID;
            top.weights = gene.weights;
            children.add(top);
        }
        
        // mutate few gene of genome to create new genome
        Genome mutBestGenome = new Genome();
        mutBestGenome.fitness = 0.0;
        mutBestGenome.ID = bestGenomes.get(0).ID;
        mutBestGenome.weights = bestGenomes.get(0).weights;       
        mutate(mutBestGenome);
        children.add(mutBestGenome);
        
        // Child genomes.
        ArrayList<Genome> crossedOverGenomes;
        // Breed with genome 0.
        crossedOverGenomes = crossOver(bestGenomes.get(0), bestGenomes.get(1));
        mutate(crossedOverGenomes.get(0));
        mutate(crossedOverGenomes.get(1));
        children.add(crossedOverGenomes.get(0));
        children.add(crossedOverGenomes.get(1));
        crossedOverGenomes = crossOver(bestGenomes.get(0), bestGenomes.get(2));
        mutate(crossedOverGenomes.get(0));
        mutate(crossedOverGenomes.get(1));
        children.add(crossedOverGenomes.get(0));
        children.add(crossedOverGenomes.get(1));
        crossedOverGenomes = crossOver(bestGenomes.get(0), bestGenomes.get(3));
        mutate(crossedOverGenomes.get(0));
        mutate(crossedOverGenomes.get(1));
        children.add(crossedOverGenomes.get(0));
        children.add(crossedOverGenomes.get(1));

        // Breed with genome 1.
        crossedOverGenomes = crossOver(bestGenomes.get(1), bestGenomes.get(2));
        mutate(crossedOverGenomes.get(0));
        mutate(crossedOverGenomes.get(1));
        children.add(crossedOverGenomes.get(0));
        children.add(crossedOverGenomes.get(1));
        crossedOverGenomes = crossOver(bestGenomes.get(1), bestGenomes.get(3));
        mutate(crossedOverGenomes.get(0));
        mutate(crossedOverGenomes.get(1));
        children.add(crossedOverGenomes.get(0));
        children.add(crossedOverGenomes.get(1));
        // For the remaining n population, add some random genomes.
        int remainingChildren = (genePool.size() - children.size());
        for (int i = 0; i < remainingChildren; i++) {
            children.add(this.createNewGenome(bestGenomes.get(0).weights.size()));
        }
        clearGenePool();
        genePool = children;
        cnt_generation++;
    }

    private Genome createNewGenome(int totalWeights) {
        Genome genome = new Genome();
        genome.ID = genomeID;
        genome.fitness = 0.0f;
        genome.weights = new ArrayList<Double>();
        for (int j = 0; j < totalWeights; j++) {
            genome.weights.add(ToolKit.RandomSigmoid());
        }
        genomeID++;
        return genome;
    }

    /**
     * This function will mix up two genomes to create 2 other new genomes
     */
    public ArrayList<Genome> crossOver(Genome g1, Genome g2) {
        Random random = new Random(System.nanoTime());
        // Select a random cross over point.
        int totalWeights = g1.weights.size();
        int crossover = Math.abs(random.nextInt()) % totalWeights;
        ArrayList<Genome> genomes = new ArrayList<Genome>();
        Genome genome1 = new Genome();
        genome1.ID = genomeID;
        genome1.weights = new ArrayList<Double>();
        genomeID++;
        Genome genome2 = new Genome();
        genome2.ID = genomeID;
        genome2.weights = new ArrayList<Double>();
        genomeID++;
        // Go from start to crossover point, copying the weights from g1 to children.
        for (int i = 0; i < crossover; i++) {
            genome1.weights.add(g1.weights.get(i));
            genome2.weights.add(g2.weights.get(i));
        }
        // Go from start to crossover point, copying the weights from g2 to children.
        for (int i = crossover; i < totalWeights; i++) {
            genome1.weights.add(g2.weights.get(i));
            genome2.weights.add(g1.weights.get(i));
        }
        genomes.add(genome1);
        genomes.add(genome2);
        return genomes;
    }

    /**
     * Generate a random chance of mutating the weight in the genome.
     * @param genome Genome to be mutated.
     */
    public void mutate(Genome genome) {
        for (int i = 0; i < genome.weights.size(); ++i) {
            double randomSigmoid = ToolKit.RandomSigmoid();
            if (randomSigmoid < MUTATION_RATE) {
                genome.weights.set(i, genome.weights.get(i)
                        + (randomSigmoid * MAX_PERMUTATION));
            }
        }
    }

    /**
     * Get the best genomes to breed new population
     * @param totalGenomes size of the top group
     */
    public ArrayList<Genome> getBestGenomes(int totalGenomes) {
        ArrayList<Genome> bestGenomes = new ArrayList<Genome>();
        while (bestGenomes.size() < totalGenomes) {
            Genome bestGen = getBestNotIn(bestGenomes);      
            if (bestGen !=null) {
                bestGenomes.add(bestGen);
            }else{
                return bestGenomes;
            }
        }
        return bestGenomes;
    }
    
    /**
     * Find the best case based on fitness score, that is not in the given list "used".
     * @param upperBound The method will return genome with the highest fitness, that is not in the list "used".
     * @return genome with the highest fitness, that is not in the given list.
     */
    private Genome getBestNotIn( ArrayList<Genome> used ){
        double bestFitness = Double.NEGATIVE_INFINITY;
        Genome bestGenome = null;
        for ( Genome gen : genePool) {
            if ((gen.fitness >= bestFitness) && (!used.contains(gen))){
                bestFitness = gen.fitness;
                bestGenome = gen;
            }
        }
        return bestGenome;
    }

    public int getCurrentGeneration() {
        return cnt_generation;
    }
    
    public String genePoolToString(){
        String ret = "";
        ret+=genomeListToString(genePool);
        ret+="\nThe best 4\n";
        ArrayList <Genome> best4 = getBestGenomes(4);
        ret+=genomeListToString(best4);
        return ret;
    }
    
    
    public String genomeListToString(ArrayList <Genome> gl){
        String ret ="";
        int line = 5;
        for (int i=0;i<gl.size();i++){
            Genome gene=gl.get(i);
            ret += "["+gene.ID+":"+nf.format(gene.fitness)+"]";
            if (i<gl.size()) ret+=",";
            if ((i+1)%line==0) ret+="\n";
        }
        return ret;
    }
    

}
