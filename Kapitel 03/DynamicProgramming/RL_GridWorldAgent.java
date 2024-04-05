import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;

/**
 * Reinforcement learning GridWorld-agents.
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
public abstract class RL_GridWorldAgent extends Hamster 
{
    protected static Random random = new Random(System.nanoTime());
    
    // Transition models to model uncertainties in state transition (during action execution).
    public static final double[][] transitUncertain = {{0.6,0.2,0.0,0.2},
                                                       {0.2,0.6,0.2,0.0},
                                                       {0.0,0.2,0.6,0.2},
                                                       {0.2,0.0,0.2,0.6}};
    
    public static final double[][] transitRusselNorvig = {{0.8,0.1,0.0,0.1},
                                                          {0.1,0.8,0.1,0.0},
                                                          {0.0,0.1,0.8,0.1},
                                                          {0.1,0.0,0.1,0.8}};
    
    public static final double[][] transitDeterministic = {{1.0,0.0,0.0,0.0},
                                                           {0.0,1.0,0.0,0.0},
                                                           {0.0,0.0,1.0,0.0},
                                                           {0.0,0.0,0.0,1.0}};
    
    protected static double[][] transitModel = transitDeterministic;
    
    protected int SIZE_OF_ACTIONSPACE = 4;
    
    protected HashMap <String, Integer> N = new HashMap <String, Integer>(); // counter at state s, N(s),  for visit statistics
    protected HashMap <String, int[]> N_sa = new HashMap <String, int[]>(); // counter at state s, N(s,a), for visit statistics
    
    // counters
    protected int cnt_steps = 0;    // Number of moves within an episode
    protected int cnt_episodes = 1; // Number of episodes performed
    protected int cnt_won = 0;      // successfully completed episodes
    protected int cnt_lost = 0;     // failures
    protected double sum_reward = 0; // cumulated reward
        
    /**
     * Stochastic policy of the agent. Assigns a probability distribution to a state over the set of 
     * possible actions.
     * @param s_key Key for given world state s.
     * @return Probability distribution over action set related to the state.
     */
    public double[] P_Policy(String s_key ){
        double[] P = new double[SIZE_OF_ACTIONSPACE]; 
        int a_max = random.nextInt();  // move randomly
        P[a_max]=1;
        return P;
    }
    
    /**
     * Modeling of unpredictable deviations in the execution of an action. Probability 
     * distribution is determined by the transit model.
     * @param dir id of transition ("direction" in our case).
     */
    public int transitUncertainty(int dir)
    {
        int dir_p = selectAccordingToDistribution(transitModel[dir]);
        return dir_p;
    }
    
    /**
     * Action selection according to the probability distribution P.
     * @return selected option, -1 if no selection (error) 
     */
    public int selectAccordingToDistribution(double P[])
    {
        double sum = 0;
        for (double p_i:P) sum+=p_i;
        if (sum>0){   
            double f = 1.0/sum; // normalize sum to 1
            int k=0;
            double e = random.nextDouble();
            double p=0.0;
            do{
                p+=f*P[k];
                if (e<p){
                    return k;
                }
                k++;
            }while(k<P.length);
        }
        return -1;
    }
    
    /**
     * Finds the possible successor states S_f that can be reached from the state [x,y] (gridworld) by 
     * action a with the corresponding probabilities. (Corresponds to P(s'|s,a) ,with s' in S_f)
     * 
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @param a action
     * @return List of transitions (sucessor state and propability)
     */
    public static ArrayList <Transition> successorStateDistribution(RL_GridEnv world, int x, int y, int a)
    {
        ArrayList <Transition> successorStateDistribution = new ArrayList <Transition> ();
        int k=0;
        double e = random.nextDouble();
        double p=0.0;
        while(k<transitModel[a].length)
        {
            int neighborX = x+world.neighborStates[k][0];
            int neighborY = y+world.neighborStates[k][1];  
            // bump: if movement is not possible stay on place.
            if (!(world.isStatePossible(neighborX, neighborY))){
               neighborX=x;
               neighborY=y;
            }
            if (transitModel[a][k]>0) successorStateDistribution.add(new Transition( neighborX, neighborY,transitModel[a][k]));
            k++;
        }
        return successorStateDistribution;
    }
    
    /**
     * Observes the current state from the environment.
     */
    public abstract String getState();
    
    /**
     * Gets the counter status at the state s.
     * @param s state key
     * @return counter status at state s.
     */
    protected int getN(String s_key)
    { 
        Integer c = N.get(s_key);
        if (c==null) return 0;
        return c;
    }
         
    /**
     * Increases counter at state s. N(s) := N(s)+1
     * @param s state key
     * @return new counter status at state s.
     */
    protected int incN(String s)
    {
        Integer c = N.get(s);
        if (c==null){
            c=1;
        }else{
            c++;
        }
        N.put(s,c);
        return c;
    }
    
    /**
     * Gets the counter status at the state-action pair (s,a).
     * @param s state key
     * @param a action
     * @return counter status at the state-action pair (s,a).
     */
    protected int getN(String s_key, int a)
    { 
        int[] ns = N_sa.get(s_key);
        if (ns==null) return 0;
        return ns[a];
    }
         
    /**
     * Increases counter at state-action pair (s,a). N(s,a) := N(s,a)+1
     * @param s state key
     * @param a action
     * @return new counter status at state s.
     */
    protected int incN(String s_key, int a)
    {
        int[] ns = N_sa.get(s_key);
        if (ns==null) {
            ns = new int[SIZE_OF_ACTIONSPACE];
            Arrays.fill(ns,0);
            ns[a]=1;
        }else{
            ns[a]++;
        }
        N_sa.put(s_key,ns);
        return ns[a];
    }
    
    /**
     * Gets the X-component as an integer value from the state key, which describes
     * the position of the agent within the grid.
     * @param s_key state key
     * @return x-component (column) of the agent position in the grid.
     */
    public static int getSX(String s_key){
        String xs = s_key.substring(1,s_key.indexOf(','));
        return Integer.parseInt(xs.trim());
    }
    
    /**
     * Gets the Y-component as an integer value from the state key, which describes
     * the position of the agent within the grid.
     * @param s_key state key
     * @return y-component (row) of the agent position in the grid.
     */
    public static int getSY(String s_key){
        int posKomma = s_key.indexOf(',');
        String ys = s_key.substring(posKomma+1,s_key.indexOf(',',posKomma+1));
        return Integer.parseInt(ys.trim());
    }
}