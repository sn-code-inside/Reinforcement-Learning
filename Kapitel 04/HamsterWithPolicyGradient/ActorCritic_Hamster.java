import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;

/**
 * A hamster-agent with "actor-critic"-policy.
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
public class ActorCritic_Hamster extends RL_GridWorldAgent
{
    protected PolicySearch_Environment env = null;
    
    // data structures for the reinforcement learning algorithm
    protected Map <String, double[]> thetas;  // Thetas: S x A -> R  
    protected Map <String, Double> V; // V: S -> R
    
    protected final double minThetaVal = 0.000001;   // Minimum amount for theta and V. Should help against side effects 
    protected final double minVal = 0.000001;        // due to rounding or truncated decimal places. 
    
    protected double I_gamma = 1;
    
    // learning parameter
    public double ETA_V = 0.2;      // Learning rate for the adjustment of the state-value function V
    public double ETA_theta = 1; //0.2;  // Learning rate for the adjustment of the policy (parameters theta) 
    public double GAMMA = 0.9999999;      // discount factor
    public double T = 1;            // exploration parameter in softmax ("temperature")
    
    // simulation counters and restrictions
    protected int max_steps = 5000; 
    protected int avg_steps = 0; // average
    
    protected int max_episodes = 1000;    // maximum number of episodes
     
    protected GreenfootImage imgBackup = null; // agents' image backup
    
    public ActorCritic_Hamster()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_gelb.png");
        else
            setImage((GreenfootImage)null);
        
        imgBackup = this.getImage();    
        SIZE_OF_ACTIONSPACE = RL_GridEnv.getSizeOfActionspace();
        thetas = new HashMap <String, double[]>();
        V = new HashMap <String, Double> ();
    }

    @Override
    public void addedToWorld(World world){
        env = (PolicySearch_Environment) world;
        cnt_episodes = 1;
    }
    
    public void act() 
    {
        String s = getState();
        incN(s);
       
        double[]P = P_Policy(s);
        int a = selectAccordingToDistribution(P);
        if (a<0) System.out.println("Was ist los? s="+s+" theta(s)"+Arrays.toString(getTheta(s))+" pi[s]="+Arrays.toString(P_Policy(s)));
        //apply transition model (consider uncertainties in the result of an action)
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
        String s_new = getState();
         
        // get the reward from the environment
        double r = env.getReward(s_new);
        sum_reward+=r;
        
        // episode end reached?
        boolean episodeEnd = false;
        if ((env.isTerminal(s_new)||(cnt_steps>=max_steps))) {   
            episodeEnd = true;
        }
        
        // learning
        update(s,a,r,s_new, episodeEnd);
        
        if (episodeEnd){
            startNewEpisode();
        }
        
        if (cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0) env.updateDisplay(this);
    } 
    
    @Override
    public String getState(){
        return getStateKey(getX(),getY(),getGrainsInJaws());
    }
    
    /**
     * Actor-critic Update
     */
    protected void update( String s, int a, double reward, String s_new, boolean episodeEnd ) 
    { 
        double observation = 0.0;
        if (episodeEnd) {
            observation = reward; 
        }
        else {
            observation = reward + (GAMMA * getV(s_new));    
        }
        
        double v = getV(s);
        double delta = observation-v;
        
        // update "critic"
        double v_new =  v + ETA_V*delta; 
        setV(s, v_new);
        
        // update "actor"
        double[] pi_sa = P_Policy(s);
        double[] theta = getTheta(s);        
        List <Integer> A_s = env.coursesOfAction(s);
        double gradient_ai = 0;
        for (int a_i : A_s){
            gradient_ai=-pi_sa[a_i];
            if (a_i==a) gradient_ai=gradient_ai+1;
            theta[a_i] += ETA_theta*I_gamma*delta*gradient_ai;
        }
        //setTheta(s,theta); not necessary here
        I_gamma = GAMMA*I_gamma; // update discount factor (global variable)
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
     * Assigns a probability distribution to a state over the set of possible actions
     * according to softmax action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_SoftMax(int n, List <Integer> A_s, String s )
    {
        double[] P = new double[n];
        Arrays.fill(P,0.0);
        double [] theta = getTheta(s);
        double sum = 0;
        for (int a_i : A_s){
            sum+=Math.exp(theta[a_i]/T);
        }
        for (int a_i : A_s){
            P[a_i]=Math.exp(theta[a_i]/T)/sum;
        }
        return P;
    }
   
    /**
     * Creates the key for accessing the table for V(s) an theta(s) . If it does not exist, the corresponding record is created.
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @param score collected grains
     * @return state key
     */
    public String getStateKey(int x, int y, int score) 
    { 
        String s_key="["+x+","+y+","+score+"]";    
        double[] theta = thetas.get(s_key); 
        if (theta == null) {
            theta = new double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(theta,0.0);
            List <Integer> A_s = env.coursesOfAction(s_key);
            for (int a : A_s) theta[a]=0.0; // initialize theta with 0.0
            thetas.put(s_key,theta);          
            V.put(s_key, 0.0);
        }
        return s_key;
    } 
    
    /**
     * A new episode is started, i.e. logging, counter updates or reset and set agent to start position.
     * Perform an evaluation period if necessary.
     */
    protected void startNewEpisode()
    {
        if (cnt_episodes%PolicySearch_Environment.EVALUATION_INTERVAL==0) {
            // avg_steps = (avg_steps*(cnt_episodes-1)+cnt_steps)/(cnt_episodes); // Schnitt aktualisieren
            env.jfxLogger.append(cnt_episodes,sum_reward/PolicySearch_Environment.EVALUATION_INTERVAL);
            //System.out.println(cnt_episodes+";"+rewardSum/disp_interval);
            sum_reward=0;
        }

        if (cnt_episodes>=max_episodes) Greenfoot.stop();
      
        I_gamma = 1;
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
        cnt_steps=0;
        cnt_episodes++; 
        Visitcounter.resetCMax();
        //N.clear();
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
     * Sets a value for state s.
     * @param s state key
     * @param v Evaluation of the state
     */
    protected void setV(String s, double v) 
    {
        if (Math.abs(v)<minVal) v=0.0;
        V.put(s,v); 
    } 
    
    /**
     * Gets value for state s.
     * @param s state key
     * @return value for state s.
     */ 
    protected Double getV(String s) 
    {
        return V.get(s); 
    }    
}