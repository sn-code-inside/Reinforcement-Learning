
import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;

/**
 * Hamster agent that adapts with an evolutionary strategy.
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
public class Evolving_Hamster extends Hamster
{
    protected int NR = -1;
    protected Genome genome = null;
   
    protected boolean graphicRepresentation = false; // Turns the display of the hamster on or off.
    
    protected HashMap <String, double[]> thetas = new HashMap <String, double[]>();  // pi(s|theta) = P(a|s) 
    
    protected boolean episodeFinished = false;
    
    public static double NEW_GENE_SIGMA = 1; // standard deviation, if new random genes are produced
    public final static double T = 1;           // exploration parameter in softmax ("temperature")
    public final static int EVALUATION_INTERVAL = 3; // interval for making an average and displaying a result.
                                             
    protected int max_steps = 500;      // maximum number of steps of one episode 
        
    public Evolutionary_PolicySearch_Environment env = null;
    protected GreenfootImage imgBackup = null;
        
    @Override
    public void addedToWorld(World world)
    {
        env = (Evolutionary_PolicySearch_Environment) world;
    }
    
    public Evolving_Hamster(int nr, Genome genome)
    {
        super();
        this.NR = nr;
        if (graphicRepresentation)
            setImage("hamster_gruen.png");
        else
            setImage((GreenfootImage)null);       
        imgBackup = this.getImage();    
        
        cnt_episodes = 1;
        SIZE_OF_ACTIONSPACE = RL_Evo_GridEnv.getSizeOfActionspace();
        this.genome = genome;
        thetas = producePolicyParameter(genome); // The genome defines the parameter theta, which are used for the policy pi(s|theta) 
                                        // that is making the action probabilities. 
    }
    
    /**
      * A new episode is started, i.e. counter reset and agent to start position
      * Perform an evaluation period if necessary.
     */
    protected void startNewEpisode()
     {
        if (cnt_episodes%EVALUATION_INTERVAL==0) {
           // System.out.println(genome.ID+";"+cnt_episodes+";"+rewardSum/disp_interval);
            this.genome = updateGenome();  
            env.evaluationFinished(genome,rewardSum/EVALUATION_INTERVAL);
            rewardSum = 0;
            env.removeObject(this);
            return;
        }
        
        if (rewardSum>0){
            cnt_won++;
        }else{
            cnt_lost++;    
        }
      
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
        cnt_steps=0;
        cnt_episodes++; 
    }
    
    @Override
    public void act() 
    {
        int x = this.getX();
        int y = this.getY();
        String s = getStateKey(x,y,getGrainsInJaws());
        incN(s);
        
        // applay policy
        double[]P = P_Policy(s);
        int a = selectAccordingToDistribution(P);
        
        // apply transition model (Consider uncertainties in the result of an action.)
        int dir = transitUncertainty(a);
         
        // execute action a
        if (dir<4){   
            try{
                setDirectionOfView(dir);
                goAhead();
            }catch (Exception e){
                //System.out.println("bump!");
            }   
            cnt_steps++;
        } 

        // get new state
        int x_new = this.getX();
        int y_new = this.getY();
          
        // get the reward
        double r = env.getReward(x_new,y_new);
        rewardSum+=r;
        
        // episode end reached?
        boolean episodeEnd = false;
        if ((env.isTerminal(x_new,y_new))||(cnt_steps>=max_steps)) {   
            episodeEnd = true;
            startNewEpisode();
        }
    }    
    
    /**
     * Stochastic policy of the agent. Assigns a probability distribution to a state over the set of possible actions.
     * @param s_key state key
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(String s_key)
    {
        List <Integer> A_s = env.coursesOfAction(s_key);
        double[] retP = P_SoftMax(SIZE_OF_ACTIONSPACE,A_s,s_key);
        return retP;
    }
    
    /**
     * SoftMax policy of the agent. Assigns a probability distribution to a state over the set of possible actions
     * according to SoftMax-action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_SoftMax(int n, List <Integer> A_s, String s )
    {
        double[] theta = getTheta(s);
        if (theta==null){
            return null; // to show that state is not discovered yet.          
            //theta = new double[n];
            //Arrays.fill(theta,0.0);
        }
        return P_SoftMax(n,A_s,theta,s);
    }
    
    /**
     * SoftMax policy of the agent. Assigns a probability distribution to a state over
     * the set of possible actions according to SoftMax-action selection strategy with 
     * the given parameter array.
     * @param n number of sucessor states
     * @param A_s List of possible actions available to the agent at the given time in s. The probability of impossible
     *            actions should be 0, but that would mean an infinite value of theta. That is why this list is necessary.
     * @param theta parameters for calculating the probability distribution
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public static double[] P_SoftMax(int n, List <Integer> A_s, double[] theta, String s )
    {
        if (A_s==null) return null;
        double[] P = new double[n];
        Arrays.fill(P,0.0);
        if (theta==null) return P;
        double sum = 0;
        for (int a_i : A_s){
            sum+=Math.exp(theta[a_i]/T);
        }
        for (int a_i : A_s){
            P[a_i]=Math.exp(theta[a_i]/T)/sum;
        }
        return P;
    }
    
    @Override
    public String getStateKey(int x, int y, int koerner) 
    { 
        String s_key="["+x+","+y+","+koerner+"]";    
        double[] theta = thetas.get(s_key); 
        if ((theta == null) && (!env.isTerminal(x,y))) {
            theta = produceRandomGen(env,s_key,NEW_GENE_SIGMA);
            thetas.put(s_key,theta);
        }
        return s_key;
    } 
    
    /** 
     * Set action preferences for a given state.
     * @param s_key state key
     * @param theta parameters for action preferences
     */
    public void setTheta(String s_key, double[] theta){
        thetas.put(s_key,theta);
    }
        
    /** 
     * Get action preferences for a given state.
     * @param s_key state key
     * @return parameters for action preferences
     */
    public double[] getTheta(String s_key){
        double[] ret = thetas.get(s_key);
        return ret;
    }
 
    /** 
     * Get preferences for given state and action.
     * @param s_key state key
     * @param a action
     * @return parameter describing the action preference.
     */
    protected double getTheta_A(String s_key, int a) 
    {
        double[] theta = thetas.get(s_key);
        return theta[a]; 
    } 
       
    /**
     * Produces a genome from current policy table (thetas).
     */
    public Genome updateGenome(){
        Set <Map.Entry<String, double[]>> set = thetas.entrySet();
        ArrayList<Map.Entry<String, double[]>> policy_table = new ArrayList<Map.Entry<String, double[]>>(set); 
        genome.policy_table = policy_table;
        return genome;
    }
    
    /**
     * Produces a policy table from a given genome.
     * @param genome A genome that contains a corresponding policy table.
     */
    public static HashMap <String, double[]> producePolicyParameter(Genome genome){
        HashMap <String, double[]> policy_parameter = new HashMap <String, double[]> ();
        for (Map.Entry <String, double[]> entry : genome.policy_table){
            policy_parameter.put(entry.getKey(), entry.getValue());
        }
        return policy_parameter;
    }
    
    /**
     * Produce a new random gene (action preferences theta for one state).
     * @param env environment for getting information about the action space.
     * @param s_key state key
     * @param sigma standard deviation for producing the new random gene.
     * @return action preferences theta for one state, what a "gene" is in this setting.
     */
    public static double[] produceRandomGen(Evolutionary_PolicySearch_Environment env, String s_key, double sigma){
        double[] ret = new double[env.getSizeOfActionspace()];
        Arrays.fill(ret,0.0);
        List <Integer> A_s = env.coursesOfAction(s_key);
        for (int a : A_s) ret[a]=ToolKit.getNormalDistribValue(0, sigma); 
        return ret;
    }
}
