import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;

/**
 * Hamster agent with policy-gradient-algorithm REINFORCE (Williams,1992)
 * and (Sutton und Barto, 2018) 
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
public class REINFORCE_Hamster extends RL_GridWorldAgent
{
    protected Map <String, double[]> thetas;  // Thetas: S x A -> R  
    
    protected LinkedList <Experience> episode;
    protected boolean episodeFinished = false;
   
    protected final double minThetaVal = 0.00001;   // Minimalbetrag für Theta und V. Soll gegen Nebenwirkungen
    protected final double minVal = 0.00001;        // durch Rundungen oder abgeschnittene Kommastellen helfen. 
    
    // Lernparameter
    protected double ETA_theta = 0.1;   // Learning rate for the adjustment of the policy (parameters theta) 
    protected double GAMMA = 0.999;    // discount factor
    protected double T = 1; // exploration parameter in softmax ("temperature")
                                              
    protected int max_steps = 500;      // maximum number of steps of one episode 
    protected int max_episodes = 10000;  // maximum number of episodes of one epoch
    protected int max_epochs = 1; // maximum number of epochs
    
    protected int cnt_epochs = 0;
        
    protected PolicySearch_Environment env = null;
    
    protected GreenfootImage imgBackup = null;
    
    public REINFORCE_Hamster()
    {
        super();
        setImage("hamster_grau.png");
        imgBackup = this.getImage();    
        SIZE_OF_ACTIONSPACE = RL_GridEnv.getSizeOfActionspace();
        thetas = new HashMap <String, double[]>();
        episode = new LinkedList <Experience> ();
    }
    
    @Override
    public void addedToWorld(World world){
        env = (PolicySearch_Environment) world;
        cnt_episodes = 1;
    }
    
    @Override
    public void act() 
    {
        String s = getState();
        incN(s); // visit statistics
       
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
        String s_new = getState();
         
        // get the reward
        double r = env.getReward(s_new);
        sum_reward+=r;
        
        // episode log
        episode.add(new Experience(s,a,r));
        
        // episode end reached?
        boolean episodeEnde = false;
        if ((env.isTerminal(s_new))||(cnt_steps>=max_steps)) {   
            episodeEnde = true;
            update(episode);
            startNewEpisode();
        }
        if (cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0) env.updateDisplay(this);   
    }    
    
    /**
     * Episodic update of REINFORCE
     * @param episode The episode is a sequence of "experiences" each consisting of state,action and reward.
     */
    protected void update( LinkedList <Experience> episode ) 
    {
        int t=0;
        while (!episode.isEmpty()){
            Experience e = episode.removeFirst();
            
            String s_e = e.getS();
            int a_e = e.getA();      
            
            // G = r_k+gamma^1*r_{k+1}+gamma^2*r_{k+2}+... until end of episode
            double G=e.getR(); // gamma to the power of 0 is 1
            ListIterator iterator = (ListIterator)episode.iterator();      
            int k=t+1;  
            while(iterator.hasNext()){
                Experience fe = (Experience)iterator.next();
                G+=Math.pow(GAMMA,k-t)*fe.getR();
                k++;
            }
            
            double[] pi_sa = P_Policy(s_e);
            double[] theta = getTheta(s_e);
            List <Integer> A_s = env.coursesOfAction(s_e);
            double gradient_ai = 0;
            for (int a_i : A_s){
                gradient_ai=-pi_sa[a_i];
                if (a_i==a_e) gradient_ai=gradient_ai+1;
                theta[a_i] += ETA_theta*Math.pow(GAMMA,t)*G*gradient_ai;
            }
            setTheta(s_e,theta);

            t++;
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
    
    @Override
    public String getState(){
        return getStateKey(getX(),getY(),getGrainsInJaws());
    }

    public String getStateKey(int x, int y, int koerner) 
    { 
        String s_key="["+x+","+y+","+koerner+"]";    
        double[] theta = thetas.get(s_key); 
        if ((theta == null) && (!env.isTerminal(x,y))) {
            theta = new double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(theta,0.0);
            List <Integer> A_s = env.coursesOfAction(s_key);
            for (int a : A_s) theta[a]=0.0; // initialize theta with 0.0
            thetas.put(s_key,theta);
        }
        return s_key;
    } 
    
    /**
     * A new episode is started, i.e. counter reset and agent to start position
     * Perform an evaluation period if necessary.
     */
    protected void startNewEpisode()
    { 
        if (cnt_episodes%PolicySearch_Environment.EVALUATION_INTERVAL==0) {
            // avg_steps = (avg_steps*(cnt_episodes-1)+cnt_steps)/(cnt_episodes);
            env.jfxLogger.append(cnt_episodes,sum_reward/PolicySearch_Environment.EVALUATION_INTERVAL);
            sum_reward=0;
        }
        
        cnt_episodes++; 
        
        if (cnt_episodes>=max_episodes){
            env.jfxLogger.clear();
            thetas.clear();
            cnt_epochs++;
            cnt_episodes=0;
        }
        
        if (cnt_epochs>=max_epochs){
            Greenfoot.stop();
        }
        
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
        N.clear();
        episode.clear();
        cnt_steps=0;
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
}
