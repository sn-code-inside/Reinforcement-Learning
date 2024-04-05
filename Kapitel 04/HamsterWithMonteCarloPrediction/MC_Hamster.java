import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;

/**
 * A "Monte Carlo" hamster agent that evaluates the results of entire episodes for the state valuation.
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
public class MC_Hamster extends RL_GridWorldAgent
{
    protected MC_AgentEnv env = null; // agent's environment
    
    public double GAMMA = 0.999; // discount factor
    public double EPSILON = 0.7; // Startrate of "exploratory behavior" in "epsilon-greedy" action selection.

    protected HashMap <String, Double> V = new HashMap <String, Double>();      // V(s) 
    protected LinkedList <Experience> episode = new LinkedList <Experience> (); // list for the episode record
        
    protected int max_steps = 1000;                                // maximum length of one episode
    protected int max_episodes = MC_AgentEnv.MAX_NUMBER_EPISODES;  // maximum number of learning episodes of this agent
    
    protected double delta_epsilon = -EPSILON/max_episodes; // This reduces epsilon during learning (0.0 holds epsilon constant).
    protected double current_epsilon = EPSILON;               // current value of epsilon
    
    protected boolean evaluationPhase = false;
    protected int cnt_grains = 0;

    protected GreenfootImage imgBackup = null;
    protected final double minVal = 0.000001;   // Minimum amount for V(s). Should help against side effects due to rounding or truncated decimal places.
    
    protected String s = null;     // observed current state
    protected String s_new = null; // observed next state
    
    public MC_Hamster()
    {
        super();
        setImage("hamster_rot.png");
        imgBackup = this.getImage();
    }
    
    @Override
    public void addedToWorld(World world){
        env = (MC_AgentEnv)this.getWorld();
        cnt_steps=1;
        N.clear();
        Visitcounter.resetCMax();
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
        Actionvalue av = targetOrientedValuation(s_key);
        int a_max = av.a;
        if (evaluationPhase) {
            // only "greedy" evaluation phase
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
            s=s_new;
        }
        // apply policy
        double[] P = P_Policy(s); 
        int a = selectAccordingToDistribution(P);
         
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
        
        // log experiences
        episode.add(new Experience(s,a,r));
        
        if (this.evaluationPhase) env.putTracemarker(getSX(s),getSY(s),a,1.0);
       
        // episode end reached?
        if ((env.isTerminal(s_new))||(cnt_steps>=max_steps)) {   
           update(episode);
           startNewEpisode();
        }
        
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
    }
    
    /**
     * Update of the V(s) by evaluating a complete episode ("Monte Carlo" approach).
     * @param episode Contains the sequence of experiences each consisting of state,action and reward.
     */
    protected void update( LinkedList <Experience> episode )
    {
        double G = 0; 
        while (!episode.isEmpty()){
            Experience e = episode.removeLast();
            String s_e = e.getS();
            G=GAMMA*G+e.getR();
            if (!contains_s(episode,s_e)){
                double avG = V.get(s_e);
                int numGs = incN(s_e);
                avG = avG+(G-avG)/numGs; //calculate new average 
                V.put(s_e,avG);
            }
        }
    }
    
    /**
     * A new episode is started, i.e. logging, counter updates or reset and set agent to start position.
     * Perform an evaluation period if necessary.
     */
    protected void startNewEpisode()
    {
        if (evaluationPhase){
            evaluationPhase=false;
        }else if (cnt_episodes%MC_AgentEnv.EVALUATION_INTERVAL==0) {
            env.jfxLogger.append(cnt_episodes,sum_reward/MC_AgentEnv.EVALUATION_INTERVAL);
            if (!((max_episodes-cnt_episodes)<MC_AgentEnv.EVALUATION_INTERVAL)) env.removeTracemarkers(); // remove, if not the last logged episode.
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
     * Creates the key for accessing the table of V(s). If it does not exist, the corresponding record is created.
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @param score collected grains
     * @return state key
     */
    public String getStateKey(int x, int y, int score) 
    { 
        String key="["+x+","+y+","+score+"]";   
        if(!V.containsKey(key)) 
        {
           V.put(key, 0.0);
        }
        return key;
    } 
    
    /**
     * Tests if a state has been visited in an episode.
     * @param episode List of "experiences": ...,(s,a,r),... 
     * @param s state key
     * @return True, if the state s appears in an episode. False otherwise.
     */
    private boolean contains_s(LinkedList <Experience> episode, String s_key){
        ListIterator <Experience> iterator = episode.listIterator(); 
        while (iterator.hasNext()) {
            Experience e_i = iterator.next(); 
            if (e_i.getS().equals(s_key)){
                return true;
            }
        } 
        return false;
    }
   
    /**
     * Sum over P(s'|s,a)*V(s') ,for all s' of S [for which P(s')>0], i.e. add up probability*value for each subsequent state.
     * @param sucessorStates List of possible subsequent states with their probabilities.
     * @return Weighted evaluation sum of the possible subsequent states of an action.
     */
    public double weightedValuation(ArrayList <Transition> sucessorStates)
    {
        double v = 0.0;
        for (Transition t : sucessorStates){
            v+=t.p*(env.getReward(t.nachbarX,t.nachbarY)+GAMMA*getV(t.nachbarX,t.nachbarY));
        }
        return v;
    };
    
    /**
     * Calculates the value of a state from the best possible subsequent state distribution. "maxQ"
     * @param s state key
     * @return valuation of an action
     */
    public Actionvalue targetOrientedValuation(String s_key)
    {
        double maxV=Double.NEGATIVE_INFINITY;
        int x = getSX(s_key);
        int y = getSY(s_key);
        int maxA=-1;
        double value=0;
        List <Integer> A =  env.coursesOfAction(x,y);
        for ( Integer a : A ){
            ArrayList <Transition> sucessorStates = this.successorStateDistribution(env,x,y,a);
            value = this.weightedValuation(sucessorStates);
            if ( value>maxV ) {
                maxV=value;
                maxA=a;
            } 
        }
        return new Actionvalue(maxA,maxV);
    }
  
    /**
     * Resets all state values to 0.
     */
    public void clearV()
    {         
        V.clear();
    }
    
    /**
     * Gets value for state s. If there is no corresponding record in the HashMap, one will be created.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return value for state s.
     */
    protected Double getV(int x, int y)
    { 
        if (((x>=0)&&(x<env.getWidth())) && ((y>=0)&&(y<env.getHeight()))){
           String key= this.getStateKey(x, y, getGrainsInJaws());
           if (!env.isTerminal(x,y)){
               return V.get(key);
           }
        }
        return 0.0;
    }
    
    /**
     * Gets value for state s.
     * @param s state key
     * @return value for state s.
     */ 
    protected Double getV(String s_key) 
    {
        return V.get(s_key); 
    }  

    /**
     * Sets a value for state s.
     * @param s state key
     * @param v Evaluation of the state
     */
    protected void setV(String s_key, double v) 
    {
        if (Math.abs(v)<minVal) v=0.0;
        V.put(s_key,v); 
    } 
}
