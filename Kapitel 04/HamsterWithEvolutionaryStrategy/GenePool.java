import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Random;
import java.text.NumberFormat;

/**
 * GeneticAlgorithm
 * @author Hai Nguyen Ho
 * @version 18 Nov 2016
 * 
 * https://www.codeproject.com/Articles/1160551/Self-Driven-Car-Simulator-Using-a-Neural-Network-a
 * https://github.com/nguyen124/AutoDrivingCarUsingNeuronNetwork
 * 
 * adapted by Uwe Lorenz in 2021
 */
public class GenePool {
    public static final NumberFormat nf = NumberFormat.getInstance();

    private int cnt_generation;
    private HashMap <Integer,Genome> pool;
    private Random random = new Random(System.nanoTime());
    
    private Evolutionary_PolicySearch_Environment env = null;
    
    private ArrayList<Genome> bestGenomes = null;

    public GenePool(Evolutionary_PolicySearch_Environment env) {
        nf.setMaximumFractionDigits(3);
        this.env = env;
        this.pool = generatePool(env.POPULATION_SIZE);
        cnt_generation = 0;
    }

    /**
     * Generate a new random pool.
     * @param populationSize  size of the gene pool
     */
    public static HashMap <Integer,Genome> generatePool(int populationSize) {
        HashMap <Integer,Genome> ret = new HashMap <Integer,Genome> ();
        for (int i = 0; i < populationSize; i++) {
            Genome gene =  new Genome();
            ret.put(gene.ID,gene);
        }
        return ret;
    }

    /**
     * Returns the size of the gene pool.
     * @return size
     */
    public int size(){
        return pool.size();
    }
    
    /**
     * Returns a list of the genomes that are stored in the pool.
     * @return list of the genomes of the pool
     */
    public ArrayList <Genome> getListOfGenomes(){
        return new ArrayList <Genome> (pool.values());
    }
   
    /**
     * Sets a fitness value to a genome.
     * @param ID ident code of the genome
     * @param fitness fitness value
     */
    public void setFitness( int ID, double fitness) {
        Genome gene = get(ID);
        gene.fitness = fitness;
        put(gene);
    }
    
    /**
     * Puts a genome into the gene pool. If there is a genome with the same ID,
     * it will be overwritten.
     * @param gene the genome to be written into the gene pool.
     * 
     */
    public void put(Genome gene){
         pool.put(gene.ID,gene);
    }
    
    /**
     * Gets a genome from the gene pool by ID code.
     * @param ID The id of the related genome.
     */
    public Genome get(Integer ID){
        return pool.get(ID);
    }

    /**
     * Clears the gene pool (removes all objects).
     */
    public void clearPool() {
        pool.clear();
    }

    /**
     * This function will generate new population of genomes based on best
     * genomes (genomes which have highest fitness). 
     */
    public double breedNextGeneration() {
        int num_top = (int)(env.SIZE_OF_TOPGROUP);
        if (num_top>pool.size()){
            System.out.println("Size of the top group must be smaller than the population size!");
            return 0;
        }
        
        bestGenomes = getBestGenomes(num_top);
        ArrayList<Genome> children = new ArrayList<Genome>();
        System.out.println("--- breed from generation "+cnt_generation+" ---");
        System.out.println("take over top "+num_top+" group = "+this.genomeListToString(bestGenomes));
        double avg_top_fitness=0;
        for (Genome gene : bestGenomes){
            avg_top_fitness+=gene.fitness;
            gene.fitness=0.0;
            children.add(gene); 
        }
        avg_top_fitness/=bestGenomes.size();
        System.out.println("top group average fitness is "+avg_top_fitness);
        System.out.print("Mix up top group...");
        int c=0;
        for (int i=0;i<bestGenomes.size();i++){
            for (int j=i+1;j<bestGenomes.size();j++){
                Genome[] mixed = mix_it(bestGenomes.get(i),bestGenomes.get(j));
                children.add(mixed[0]);
                children.add(mixed[1]);
                System.out.print("["+i+","+j+"],");
                c+=2;
            }
        }
        System.out.println("\n"+c+" hybrids added.");
        int remainingChildren = (env.POPULATION_SIZE - children.size());
        System.out.println("Remaining children:"+remainingChildren);
        c=0;
        for (int i = 0; i < 3*remainingChildren/4; i++) { 
            children.add(clone_mutate(bestGenomes.get(i%num_top)));
            c++;
        }
        System.out.println(c+" clones of top group added.");
        c=0;
        for (int i = children.size(); i < env.POPULATION_SIZE; i++) { 
            children.add(new Genome());
            c++;
        }
        System.out.println(c+" randoms added.");
        clearPool();
        for (Genome next_gen : children) put(next_gen);
        cnt_generation++;
        return avg_top_fitness;
    }
    
    /**
     * Mix policy of gen1 with gen2.
     * @param gen1 represents a policy table
     * @param gen2 represents a policy table
     * @return children two complementary genomes that represents the mixed policy tables 
     */
    public Genome[] mix_it(Genome gen1, Genome gen2){
        ArrayList<Map.Entry<String, double[]>> child1 =  new ArrayList<Map.Entry<String, double[]>> ();
        ArrayList<Map.Entry<String, double[]>> child2 =  new ArrayList<Map.Entry<String, double[]>> ();
        
        HashMap <String, double[]> policy1 = Evolving_Hamster.producePolicyParameter(gen1);
        HashMap <String, double[]> policy2 = Evolving_Hamster.producePolicyParameter(gen2);
        
        HashSet <String> states = new HashSet<String> ();
        states.addAll(policy1.keySet());
        states.addAll(policy2.keySet());
        
        Iterator<String> iter = states.iterator();
        while (iter.hasNext()) {
            String s_key = iter.next();
            double[] Pa_1 = policy1.get(s_key);
            double[] Pa_2 = policy2.get(s_key);

            if (Pa_1==null){
                Pa_1 = Evolving_Hamster.produceRandomGen(env, s_key, Evolving_Hamster.NEW_GENE_SIGMA);
            }
            if (Pa_2==null){
                Pa_2 = Evolving_Hamster.produceRandomGen(env, s_key, Evolving_Hamster.NEW_GENE_SIGMA);        
            }
            
            double part = 0.5;  // = random.nextFloat() Maybe set the proportion randomly? 
            if (random.nextFloat()<part){
                child1.add(new SimpleEntry <String, double[]>(s_key,Pa_1));
                child2.add(new SimpleEntry <String, double[]>(s_key,Pa_2));
            }else{
                // complement
                child1.add(new SimpleEntry <String, double[]>(s_key,Pa_2));
                child2.add(new SimpleEntry <String, double[]>(s_key,Pa_1));
            }
        }
        Genome[] children = new Genome[2];
        children[0] = new Genome(child1);
        children[1] = new Genome(child2);
        
        return children;
    }

    /**
     * Generates a mutated clone.
     * @param genome Genome to be mutated.
     * @param mutated clone
     */
    public Genome clone_mutate(Genome genome) {
        Genome clone_gene = (Genome)genome.clone();
        // Iterate over all states stored in the policy table.
        for (int i = 0; i < clone_gene.policy_table.size(); i++) {
            if (random.nextFloat()<env.MUTATION_RATE){
                double r_normal = ToolKit.getNormalDistribValue(0, env.MUTATION_STANDARD_DEVIATION);
                Map.Entry <String, double[]> entry = clone_gene.policy_table.get(i);
                double[] P_a = entry.getValue();
                P_a[ random.nextInt(P_a.length) ] += r_normal; // modify one selection probability in the policy table.
                entry.setValue(P_a);
                clone_gene.policy_table.set(i, entry );
            }
        }
        return clone_gene;
    }

    /**
     * Get the best n genomes.
     * @param n size of the top group
     * @return List with the best n genomes.
     */
    private ArrayList<Genome> getBestGenomes(int n) {
        ArrayList <Genome> all = this.getListOfGenomes();
        ArrayList <Genome> best = new ArrayList<Genome>();
        while (best.size() < n) {
            Genome bestGen = getBestNotIn(all, best);      
            if (bestGen !=null) {
                best.add(bestGen);
            }else{
                return best;
            }
        }
        return best;
    }
    
    /**
     * Returns the currrent list of the best genomes.
     */
    public  ArrayList<Genome> getTopGroup(){
        return this.bestGenomes;
    }
    
    /**
     * Find the best case based on fitness score, that is not in the given list "used".
     * @param all the complete pool.
     * @param used The method will return the genome with the highest fitness, that is not in the list "used".
     * @return genome with the highest fitness, that is not in the given list.
     */
    private Genome getBestNotIn( ArrayList <Genome> all, ArrayList<Genome> used ){
        double bestFitness = Double.NEGATIVE_INFINITY;
        Genome bestGenome = null;
        for ( Genome gen : all) {
            if ((gen.fitness >= bestFitness) && (!used.contains(gen))){
                bestFitness = gen.fitness;
                bestGenome = gen;
            }
        }
        return bestGenome;
    }

    /**
     * Returns the value of the generation counter.
     * @return value of the generation counter
     */
    public int getGeneration() {
        return cnt_generation;
    } 
          
 /*   public String genePoolToString(){
        String ret = "";
        ret+=genomeListToString(genePool);
        ret+="\nThe best 4\n";
        ArrayList <Genome> best4 = getBestGenomes(4);
        ret+=genomeListToString(best4);
        return ret;
    }
    */
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
