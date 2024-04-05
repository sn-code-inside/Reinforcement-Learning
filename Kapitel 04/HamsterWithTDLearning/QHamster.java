import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.ArrayList;
import java.util.Random;
import java.util.*;

/**
 *  A hamster agent with "off-policy" Q-learning. In each single step, the TD error is evaluated 
 *  and the (tabular) state action score is updated accordingly.
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
public class QHamster extends RL_GridWorldAgent
{
    protected TD_AgentEnv env = null; // agent's environment
    
    protected double EPSILON = 0.2; // Startrate of "exploratory behavior" in "epsilon-greedy" action selection.
    protected double ETA = 0.1;     // learning rate (step size)
    protected double GAMMA = 0.8; // discount factor
    
    protected Map <String, Double[]> Q = new HashMap <String, Double[]>(); // Q: S x A -> R
    protected final double minQVal = 0.00001;// Minimum amount for Q(s,a), lower is 0. Should help against side effects due to rounding or truncated decimal places. 
        
    protected int max_steps = 1000;                                // maximum length of one episode
    protected int max_episodes = TD_AgentEnv.MAX_NUMBER_EPISODES;  // maximum number of learning episodes of this agent
    
    protected double delta_epsilon = 0.0; //-EPSILON/max_episodes; // This reduces epsilon during learning (0.0 holds epsilon constant).
    protected double current_epsilon = EPSILON;                    // current value of epsilon
    
    protected boolean evaluationPhase = false;
    protected GreenfootImage imgBackup = null;
    
    // variables for the algorithm
    protected int a = -1; // current action
    protected String s = null;     // observed current state
    protected String s_new = null; // observed next state
    protected boolean episodeEnd = false;
    
    public QHamster()
    {
        super();
        setImage("hamster_blau.png");
        imgBackup = this.getImage();
    }
    
    @Override
    public void addedToWorld(World world){
        env = (TD_AgentEnv)this.getWorld();
        cnt_steps=1;
        N_sa.clear();
        Visitcounter.resetCMax();
        QValueMarker.resetQMaxMin();
    }
    
    /**
     * Stochastic policy of the agent. Assigns a probability distribution to a state over the set of possible actions.
     * @param s_key state key
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(String s_key)
    {
        List <Integer> A_s = env.coursesOfAction(s_key);
        double[] retP = P_Policy(SIZE_OF_ACTIONSPACE,A_s,s_key);
        return retP;
    }
   
    /**
     * Assigns a probability distribution to a state over the set of possible actions
     * according to epsilon-greedy action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(int n, List <Integer> A_s, String s_key )
    {
        double[] P = new double[n];
        Arrays.fill(P,0.0);
        int a_max = getActionWithMaxQ(s_key);
        if (evaluationPhase) {
            // decide only "greedy" in evaluation phase
            P[a_max]=1;
        }else{
            // epsilon-greedy otherwise
            int k = A_s.size();
            for (int a_i : A_s){
                P[a_i] =  current_epsilon/k;
            }
            P[a_max]+=(1-current_epsilon);  // For a_max add the probability 1-epsilon.
        }
        return P;
    }
    
    /**
     * This method is called once by Greenfoot as soon as the 'Act' button or repeatedly if the 'Run' button is clicked.  
     */
    public void act() 
    {
        if (s_new==null) {
            s = getState();
        }else{
            s = s_new;
        }
        
        // apply policy
        double[] P = P_Policy(s); 
        a = selectAccordingToDistribution(P);
        incN(s,a);
         
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
        s_new = getState();
         
        // get the reward from the environment
        double r = env.getReward(s_new);
        sum_reward+=r;
        
        episodeEnd = false;
        if ((env.isTerminal(s_new))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }
        
        // Q-update
        update(s,a,r,s_new, episodeEnd); 
        
        updateDisplay();
        
        // episode end reached?
        if (episodeEnd) {   
           startNewEpisode();
        }
    }
    
    /**
     * Updates the visualizations.
     */
    protected void updateDisplay(){
        if (this.evaluationPhase) env.putTracemarker(getSX(s),getSY(s),a,1.0);
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
    }
    
    /**
     * Update of Q(s,a)  ("Q-learning" approach)
     * @param s_key state key
     * @param a action
     * @param reward Reward
     * @param s_key_new Successor state
     * @param end Has a terminal state or the step limit been reached?
     */
    protected void update( String s_key, int a, double reward, 
                                    String s_new_key, boolean end ) 
    { 
        double observation = 0.0;
        if (end) {
            observation = reward; 
        } else {
            observation = reward + (GAMMA * maxQ(s_new_key));    
        }   
        double q = getQ(s_key, a); 
        q = q + ETA*(observation - q); 
        setQ(s_key,a, q); 
    }
    
    /**
     * A new episode is started, i.e. logging, counter updates or reset and set agent to start position.
     * Perform an evaluation period if necessary.
     */
    protected void startNewEpisode()
    {
        if (evaluationPhase){
            evaluationPhase=false;
        }else if (cnt_episodes%TD_AgentEnv.EVALUATION_INTERVAL==0) {
            env.jfxLogger.append(cnt_episodes,sum_reward/TD_AgentEnv.EVALUATION_INTERVAL);
            if (!((max_episodes-cnt_episodes)<TD_AgentEnv.EVALUATION_INTERVAL)) env.removeTracemarkers(); // remove, if not the last logged episode.
            sum_reward=0;
            evaluationPhase=true; // do one "evaluation episode" with no exploration
        }

        if (cnt_episodes>=max_episodes) Greenfoot.stop();

        current_epsilon+=this.delta_epsilon; // reduces epsilon during learning
        cnt_steps=0;
        cnt_episodes++;
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
    }
    
    @Override
    public String getState(){
        return getStateKey(getX(),getY(),getGrainsInJaws());
    }
    
    /**
     * Creates the key for accessing the table of Q. If it does not exist, the corresponding record is created.
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @param score collected grains
     * @return state key
     */
    public String getStateKey(int x, int y, int score) 
    { 
        String key="["+x+","+y+","+score+"]";   
        if (!Q.containsKey(key)) {
            Double[] vals = new Double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(vals,0.0); 
            Q.put(key,vals);
        }
        return key;
    } 
    
    /**
     * Gets the action with the largest Q value for a given state. If there are several Q_max actions
     * with the same value, they are selected randomly. If there are no Q-values, then -1 is returned.
     * @param s_key state key
     * @return Action with greatest Q-value stored for the state s. Null if state is unknown.
     */
    protected Integer getActionWithMaxQ(String s_key) 
    { 
        double maxQ = Double.NEGATIVE_INFINITY; 
        LinkedList <Integer> A_max = new LinkedList <Integer> (); 
        Double[] vals = Q.get(s_key); 
        if (vals!=null) {
            for(int a=0;a<vals.length;a++) {
                Double v=vals[a];
                if(v>maxQ) { 
                    maxQ = v; 
                    A_max.clear();
                    A_max.add(a);
                }
                if (v==maxQ) A_max.add(a); // include in the list of the best actions
            } 
         
        }
        if (A_max.isEmpty()) {
            return -1;
        }else{
            return A_max.get(random.nextInt(A_max.size())); // For not choosing always the same action, if there is more then one action with the same value.
        }
    }
        
    /**
     * Gets the greatest Q-value stored for the state s.
     * @param s_key state key
     * @return greatest Q-value stored for the state s. Null if state is unknown.
     */
    public Double maxQ(String s_key) 
    { 
        double maxQ = Double.NEGATIVE_INFINITY; 
        Double[] vals = Q.get(s_key); 
        if (vals!=null) {
            for(Double v : vals) { 
                if(v>maxQ) { 
                    maxQ = v; 
                }
            } 
            return maxQ;
        }else{
            return null;
        }
    }  
    
    /**
     * Sets a Q-value for the state-action pair (s,a).
     * @param s state key
     * @param v Q-value of the state-action pair (s,a) to be set.
     */
    protected void setQ(String s_key, int a, double v) 
    {
        if (Math.abs(v)<minQVal) v=0.0;
        Q.get(s_key)[a]=v; 
    } 
 
    /**
     * Gets the Q-value for the state-action pair (s,a).
     * @param s_key state key
     * @param a action
     * @return Q-value
     */
    protected double getQ(String s_key, int a) 
    {
        return Q.get(s_key)[a]; 
    } 
    
    /**
     * Gets all Q-Values at given state s.
     * @param s_key state key
     * @return Array with the Q action values.
     */
    public Double[] getQValues(String s_key)
    {
        return Q.get(s_key);
    }
    
}