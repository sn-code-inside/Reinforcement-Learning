import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;

/**
 * A hamster-agent in partially observable environment with "actor-critic"-policy.
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
public class ActorCritic_Hamster_FV extends RL_GridWorldAgent_FV
{   
    // data structures for the reinforcement learning algorithm
    // protected Map <String, Integer> C; // C (counter for visits)
    protected Map <String, double[]> thetas;  // Thetas: S x A -> R  
    protected Map <String, Double> V; // V: S -> R
    
    protected final double minThetaVal = 0.000001;   // Minimum amount for theta and V. Should help against side effects 
    protected final double minVal = 0.000001;        // due to rounding or truncated decimal places. 
    
    // learning parameter
    public double ETA_V = 0.2;      // Learning rate for the adjustment of the state-value function V
    public double ETA_theta = 0.05;  // Learning rate for the adjustment of the policy (parameters theta) 
    public double GAMMA = 0.9999999;      // discount factor
    public double T = 1;            // exploration parameter in softmax ("temperature")
    
    protected double I_gamma = 1;
    
    protected int a = -1; // current action
    protected String xs = null;     // observed current state
    protected String xs_new = null; // observed next state
    protected boolean episodeEnd = false;
    
    protected GreenfootImage imgBackup = null; // agents' image backup
    
    public ActorCritic_Hamster_FV()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_green_yellow.png");
        else
            setImage((GreenfootImage)null);
        
        imgBackup = this.getImage();    
        SIZE_OF_ACTIONSPACE = RL_GridEnv_FV.getSizeOfActionspace();
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
        if (xs_new==null) {
            xs = getState();
        }else{
            xs = xs_new;
        }
        
        double[]P = P_Policy(xs);
        a = selectAccordingToDistribution(P);
        //incN(xs);
        
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

        // get new observation
        String xs_new = getState();
         
        // get the reward from the environment
        double r = env.getReward(getX(),getY());
        sum_reward+=r;
        
        // episode end reached?
        boolean episodeEnd = false;
        if ((env.isTerminal(getX(),getY())||(cnt_steps>=max_steps))) {   
            episodeEnd = true;
        }
        
        // AC learning
        update(xs,a,r,xs_new,episodeEnd);
        
        if ((env.DISPLAY_UPDATE) && (cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
        
        if (episodeEnd){
            startNewEpisode();
        }
    }   
    
    @Override
    public String getStateKey()
    {
        String xs_key=super.getStateKey();
        if (!V.containsKey(xs_key)) {
            setV(xs_key, 0.0);
        }
        return xs_key;
    }
    
    /**
     * Stochastic policy of the agent. Assigns a probability distribution to a state over 
     * the set of possible actions.
     * @param xs_key state key
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(String xs_key)
    {
        if (xs_key.equals("[T]")) return null;
        List <Integer> A_s = getPossibleActions(getX(),getY());
        double[] retP = P_XSoftMax(A_s,xs_key);
        return retP;
    }
    

    @Override
    public double[] P_PolicyVisualization(String xs_key,int x,int y){
        if (xs_key.equals("[T]")) return null;
        if (getV(xs_key)==null) return null;
        List <Integer> A_s = getPossibleActions(x,y);
        double[] retP = P_XSoftMax(A_s,xs_key);
        return retP;
    }
   
    /**
     * Assigns a probability distribution to a feature vector over the set of possible actions
     * according to softmax action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_XSoftMax(List <Integer> A_s, String xs)
    {
        double[] P = new double[SIZE_OF_ACTIONSPACE];
        Arrays.fill(P,0.0); // zero probability for impossible actions
        if ((A_s==null)||(A_s.isEmpty())) return P;
        
        // action preferences
        double[] h_sa = new double[SIZE_OF_ACTIONSPACE];
        for (int a_i : A_s){
            double[] x_sa = this.getFeatureVector(xs,a_i);
            double[] thetas_xa = getTheta(xs,a_i);
            for  (int i=0;i<x_sa.length;i++){
                h_sa[a_i]+=thetas_xa[i]*x_sa[i];
            }
        }
        
        // sum
        double sum = 0;
        for (int a_i : A_s){
            sum+=Math.exp(h_sa[a_i]/T);
        }
        
        // softmax probabilities
        for (int a_i : A_s){
            P[a_i]=Math.exp(h_sa[a_i]/T)/sum;
        }
        
        return P;
    }
    
    /**
     * Actor-critic Update
     * @param xs_key state key consisting of features
     * @param a action
     * @param reward Reward
     * @param xs_key_new successor state
     * @param end Has a terminal state or the step limit been reached?
     */
    protected void update( String xs_key, int a, double reward, String xs_new_key, boolean episodeEnd ) 
    {   
        double observation = 0.0;
        if (episodeEnd) {
            observation = reward; 
        }
        else {
            observation = reward + (GAMMA * getV(xs_new_key));    
        }
        
        double v = getV(xs_key);
        double delta = observation-v;
        
        // Update "critic"
        double v_new =  v + ETA_V*delta; 
        setV(xs_key, v_new);
         
        // Update "actor"
        double[] theta_xa = getTheta(xs_key,a);
        double[] gradient= gradient_ln_pi(xs_key,a);
        for (int k=0;k<theta_xa.length;k++){
            theta_xa[k] += ETA_theta*I_gamma*delta*gradient[k];
        }
        setTheta(xs_key,a,theta_xa);
        I_gamma = GAMMA*I_gamma;
    } 
    
    /**
     * Calculates the policy gradient of a state consisting of a feature vector.
     * @param xs the feature-state key
     * @param x_sa feature vector
     * @return gradient
     */
    private double[] gradient_ln_pi(String xs, int a){
        double[] gradient = this.getFeatureVector(xs,a);
        double[] pi_s = P_Policy(xs);
        for (int k=0;k<gradient.length;k++) {
            double sum=0;
            for (int b=0;b<pi_s.length;b++){
                double[] x_sb = this.getFeatureVector(xs,b);
                sum+=pi_s[b]*x_sb[k];
            }  
            gradient[k]-=sum;
        }
        return gradient;
    }
    
    /**
     * A new episode is started, i.e. logging, counter updates or reset and set agent to start position.
     * Perform an evaluation period if necessary.
     */
    protected void startNewEpisode()
    {
        if (cnt_episodes%PolicySearch_Environment.EVALUATION_INTERVAL==0) {
            env.jfxLogger.append(cnt_episodes,sum_reward/PolicySearch_Environment.EVALUATION_INTERVAL);
            sum_reward=0;
        }
        if (cnt_episodes>=max_episodes) Greenfoot.stop();
       
        xs = null;
        xs_new = null;
        a=-1;
        episodeEnd = false;
        I_gamma = 1;
        cnt_steps=0;
        cnt_episodes++; 
        Visitcounter.resetCMax();
        //N.clear();
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
        //setLocation(random.nextInt(5)+1,env.getHamsterStartY()); // special test case for "mapSilver"
    }
    
    /**
     * Creates the numerical feature vector for calculations from feature string and related action. 
     * @param xs the feature-state key
     * @param a action
     * @return numerical feature vector
     */
    public double[] getFeatureVector(String xs, int a) 
    { 
        int vec_size = getSizeOfObservationVector()+SIZE_OF_ACTIONSPACE;
        double[] x_sa = new double[vec_size];
        Arrays.fill(x_sa,0);
        int f_i = 0;
        for (int i=0;i<xs.length();i++){
            if (xs.charAt(i)=='1') x_sa[f_i]=1.0;
            if (xs.charAt(i)==',') f_i++;
        }  
        x_sa[getSizeOfObservationVector()+a]=1.0;    
        String xsa_key = getStateActionKey(xs,a);
        if (!thetas.containsKey(xsa_key)) {
            double[] theta = new double[vec_size];
            Arrays.fill(theta,0.0);         
            setTheta(xsa_key,theta);
        }    
        return x_sa;
    }
    
    /**
     *  Creates the numerical feature vector for calculations from sensoric feature string.
     *  @param xs string with observed sensoric features
     *  @return numerical vector
     */
    public double[] getObservationVector(String xs)
    {
        int vec_size = getSizeOfObservationVector();
        double[] x_sa = new double[vec_size];
        Arrays.fill(x_sa,0);
        int f_i = 0;
        for (int i=0;i<xs.length();i++){
            if (xs.charAt(i)=='1') x_sa[f_i]=1.0;
            if (xs.charAt(i)==',') f_i++;
        }  
        return x_sa;
    }
    
    /**
     * Calculates the size of the vector, that contains the features of the observation. 
     * @param size of the observation vector
     */
    private int getSizeOfObservationVector(){
        return env.observationArea.length*SIZE_OF_FEATURESPACE;
    }
    
    @Override
    public String getState(){
        return getStateKey();
    }
    
    /** 
     * Set parameters for given (feature-)state-action pair.
     * @param xs state key (based on features)
     * @param a action
     * @param parameters theta for calculating action preferences
     */
    public void setTheta(String xs, int a, double[] theta){
        thetas.put(getStateActionKey(xs,a),theta);
    }
   
    /** 
     * Get parameters for a given (feature-)state-action pair.
     * @param xs state key (based on features)
     * @param a action
     * @return parameters for calculating action preferences
     */
    public double[] getTheta(String xs, int a){
        return thetas.get(getStateActionKey(xs,a));
    }
    
    /**
     * Creates the key for accessing the table theta(s,a) based on features and action. 
     * @param xs the feature-state key
     * @param a action
     * @return key
     */
    public String getStateActionKey(String xs, int a) 
    { 
        String xsa = xs;
        xsa+='[';
        for (int i=0;i<SIZE_OF_ACTIONSPACE;i++){
            if (i==a)
                xsa+='1';
            else
                xsa+='0';
            if (i==SIZE_OF_ACTIONSPACE-1){
                xsa+=']';
            }else{
                xsa+=',';
            }
        }
        return xsa;
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
     * Set action preferences for a given state.
     * @param xs_key observations
     * @param theta parameters for action preferences
     */
    public void setTheta(String xs_key, double[] theta){
        thetas.put(xs_key,theta);
    }
    
    /** 
     * Get action preferences for a given state.
     * @param xs_key observations
     * @return parameters for action preferences
     */
    public double[] getTheta(String xs_key){
        double[] ret = thetas.get(xs_key);
        return ret;
    }
 
    /** 
     * Get preferences for given state and action.
     * @param xs_key observations
     * @param a action
     * @return parameter describing the action preference.
     */
    protected double getTheta_A(String xs_key, int action) 
    {
        double[] theta = thetas.get(xs_key);
        return theta[action]; 
    } 
      
    /**
     * Sets a value for observed features xs.
     * @param xs observed features
     * @param v evaluation of the observation
     */
    protected void setV(String xs, double v) 
    {
        if (Math.abs(v)<minVal) v=0.0;
        V.put(xs,v); 
    } 
    
    /**
     * Gets value of observation xs.
     * @param xs observed features
     * @return stored evaluation of the given observation
     */ 
    protected Double getV(String xs) 
    {
        return V.get(xs); 
    }    
}