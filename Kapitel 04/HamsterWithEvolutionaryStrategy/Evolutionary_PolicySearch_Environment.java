import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.*;

/**
 * An environment for policy searching hamster agents.
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
public class Evolutionary_PolicySearch_Environment extends RL_Evo_GridEnv
{
    public static RL_GridWorldAgent hamster;
    
    /* size of the gene pool */
    public static final int POPULATION_SIZE = 1000;
    
    /* size of the top group */
    public static final int SIZE_OF_TOPGROUP = 20; 
    
    /* determines percentage of genes (entries of the policy table) that will be arbitrary manipulatet */
    public static final float MUTATION_RATE = 0.9f; 
    
    /* standard deviation of the modifications */
    public static final float MUTATION_STANDARD_DEVIATION = 0.005f; 
    
    private GenePool genePool = null; 
    private Evolving_Hamster[] hamsterPopulation;
    
    private JfxChartLogger jfxLogger = null;
    private String logFilePath = "data\\"; 
    
    private int living_agents=0;
    
    private int max_generation=3010; // maximum number of generations (causes greenfoot stopp)
    /**
     * Constructor of this environment.
     */
    public Evolutionary_PolicySearch_Environment()
    { 
        super(mapFrozenLake);
        //super(mapDynaMaze);
        //super(mapStochasticSmall);
        //super(mapWithTrap4);   
        //super(mapFlat);
        
        genePool = new GenePool(this); // Initialization according to the parameters preset above.
        makePopulation(getHamsterStartX(),getHamsterStartY());
    
        //addObject(new FPS(),2,0);  // display simulation speed (frames per second)
        //setPaintOrder(FPS.class);
    }
    
    @Override
    public void started() {
        System.out.println("greenfoot started");
        if (jfxLogger==null) {
            initJfxLogger();
        }
    }
    
    @Override
    public void stopped() {
        System.out.println("greenfoot stopped");
        if (jfxLogger!=null) {
            jfxLogger.flush(); // store cached data to disc
        }
    }
    
    private void initJfxLogger(){
        jfxLogger = new JfxChartLogger(logFilePath,"Hamster agent that adjusts its policy with an evolutionary strategy.","cumulated reward","generation","reward");
        jfxLogger.appendln("environment attributes:");
        jfxLogger.appendln("reward for a grain;"+rewardForAGrain);
        jfxLogger.appendln("reward trap;"+rewardTrap);
        jfxLogger.appendln("transition effort;"+rewardPerTransition);
        jfxLogger.appendln("arena:");
        for (String line : fieldDescription) jfxLogger.appendln(line);
        
        jfxLogger.appendln("learning parameter:");
        jfxLogger.appendln("population size;"+POPULATION_SIZE);
        jfxLogger.appendln("size of selected top group;"+SIZE_OF_TOPGROUP);
        jfxLogger.appendln("mutation rate;"+MUTATION_RATE);
        jfxLogger.appendln("mutation standard deviation;"+MUTATION_STANDARD_DEVIATION);
        jfxLogger.appendln("exploration T;"+Evolving_Hamster.T);  
        jfxLogger.appendln("policy test iterations;"+Evolving_Hamster.EVALUATION_INTERVAL);
        
        jfxLogger.append("start;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n");
        
    }
    
    /**
     * Take the fitness value after the end of an agent's episode and start breeding the next generation 
     * when all agents finished their episode.
     * @param genome Genome of the individual that ended its episode.
     * @param fitness Fitness value determined.
     */
    public void evaluationFinished(Genome genome, double fitness){
        genePool.setFitness(genome.ID,fitness);
        living_agents--;
        if (living_agents<=0){
            double avg_fitness_topgroup = genePool.breedNextGeneration();
            makePopulation(getHamsterStartX(),getHamsterStartY());
            updateDisplay(genePool.getTopGroup());
            jfxLogger.append(genePool.getGeneration(),avg_fitness_topgroup);
            if (genePool.getGeneration()>=this.max_generation) Greenfoot.stop();
        }
    }
    
    /**
     * Production of the population according to the current genpool.
     * @param x x-component of the start state.
     * @param y y-component of the start state.
     */ 
    public void makePopulation(int x, int y){
        removeObjects(getObjects(Evolving_Hamster.class));
        hamsterPopulation = new Evolving_Hamster[POPULATION_SIZE];
        ArrayList <Genome> currGenePool = genePool.getListOfGenomes(); 
        int i=0;
        for (Genome gene : currGenePool){
            hamsterPopulation[i] = new Evolving_Hamster(i,gene);
            addObject(hamsterPopulation[i],x,y);
            i++;
        }
        living_agents=i;
    }  
  
   /**
    * Updates the objects in the territory marking the average policy of the population.
    */
    public void updateDisplay(Evolving_Hamster[] hamsterPopulation)
    {
        int weltBreite = getWidth();
        int weltHoehe = getHeight();  
        for ( int i=0;i<weltBreite;i++ ){
            for( int j=0;j<weltHoehe;j++ ){
                updatePolicyMarker(hamsterPopulation,i,j);
            }
        }
    }
    
   /**
    * Updates the objects in the territory marking the average policy of the given genome list.
    */
    public void updateDisplay(ArrayList<Genome> genomes)
    {
        if (genomes==null) return;
        int weltBreite = getWidth();
        int weltHoehe = getHeight();  
        for ( int i=0;i<weltBreite;i++ ){
            for( int j=0;j<weltHoehe;j++ ){
                updatePolicyMarker(genomes,i,j);
            }
        }
    }
    
   /**
     * Updates the objects that display the average policy of the given genome list.
     * @param hamsterPopulation current agent population
     * @param x column  (state x component)
     * @param y row  (state y component)
     */
    public void updatePolicyMarker(ArrayList<Genome> genomes, int x, int y)
    {
        if (genomes==null) return;
        String s_key="["+x+","+y+",0]";
        double[] avg_pi_s = new double[this.getSizeOfActionspace()];
        for (Genome genome : genomes){
            double[] theta = Evolving_Hamster.producePolicyParameter(genome).get(s_key);
            if (theta!=null){
                double[] pi_s = Evolving_Hamster.P_SoftMax(getSizeOfActionspace(),coursesOfAction(x,y),theta,s_key); 
                if (pi_s!=null) {
                    for (int i=0;i<pi_s.length;i++){
                        avg_pi_s[i]+=pi_s[i];
                    }
                }
            }
        }
        for (int i=0;i<avg_pi_s.length;i++){
            avg_pi_s[i]= avg_pi_s[i]/genomes.size();
        }
        List <Policymarker> objects = getObjectsAt(x,y,Policymarker.class);
        removeObjects(objects);
        for (int a=0;a<avg_pi_s.length;a++){
            if (avg_pi_s[a]>0) addObject(new Policymarker(avg_pi_s[a],a),x,y);
        }       
    }

    /**
     * Updates the objects that display the average policy of the given population.
     * @param hamsterPopulation current agent population
     * @param x column  (state x component)
     * @param y row  (state y component)
     */
    public void updatePolicyMarker(Evolving_Hamster[] hamsterPopulation, int x, int y)
    {
        String s_key="["+x+","+y+",0]";
        double[] avg_pi_s = new double[this.getSizeOfActionspace()];
        for (int j=0;j<hamsterPopulation.length;j++){
            Evolving_Hamster pgHamster = hamsterPopulation[j];
            if (pgHamster==null){
                System.out.println("No hamster at position "+j+" of hamster population array.");
            }
            double[] pi_s = pgHamster.P_Policy(s_key);
            if (pi_s!=null) {
                for (int i=0;i<pi_s.length;i++){
                    avg_pi_s[i]+=pi_s[i];
                }
            }
        }
        for (int i=0;i<avg_pi_s.length;i++){
            avg_pi_s[i]= avg_pi_s[i]/hamsterPopulation.length;
        }
        List <Policymarker> objects = getObjectsAt(x,y,Policymarker.class);
        removeObjects(objects);
        for (int a=0;a<avg_pi_s.length;a++){
            if (avg_pi_s[a]>0) addObject(new Policymarker(avg_pi_s[a],a),x,y);
        }       
    }
   
    /**
     * Remove all policy marker.
     */ 
    public void removeTracemarker()
    {
        List objects = getObjects(Policymarker.class);
        removeObjects(objects);
    }
}