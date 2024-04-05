import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

/**
 * Hamster agent with policy-gradient-algorithm REINFORCE with baseline
 * (Williams,1992), (Sutton und Barto, 2018) 
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
public class REINFORCE_Hamster_withBaseline extends REINFORCE_Hamster
{
    protected Map <String, Double> V; // V: S -> R
    public double ETA_V = 0.2;
    
    public REINFORCE_Hamster_withBaseline()
    {
        super();
        setImage("hamster_tuerkis.png");
        imgBackup = this.getImage();
        V = new HashMap <String, Double>();
    }
  
    /**
     * Episodic update of 'REINFORCE with baseline'
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
            
            double v = getV(s_e);
            double advantage = G-v;
            double v_new =  v + ETA_V*advantage; 
            setV(s_e, v_new);
            
            double[] pi_sa = P_Policy(s_e);
            double[] theta = getTheta(s_e);
            List <Integer> A_s = env.coursesOfAction(s_e);
            double gradient_ai = 0;
            for (int a_i : A_s){
                gradient_ai=-pi_sa[a_i];
                if (a_i==a_e) gradient_ai=gradient_ai+1;
                theta[a_i] += ETA_theta*Math.pow(GAMMA,t)*advantage*gradient_ai;
            }
            setTheta(s_e,theta);

            t++;
        }
    } 
    
    /**
     * Tests if a state has been visited in a given episode (is contained in episode log).
     * @param episode Episode consisting of "experiences" ...,(s,a,r),.... 
     * @param s state key
     * @return true if a state has been visited in a given episode (is contained in episode log).
     */
    private boolean contain_s(LinkedList <Experience> episode, String s){
        ListIterator <Experience> iterator = episode.listIterator(); 
        while (iterator.hasNext()) {
            Experience e_i = iterator.next(); 
            if (e_i.getS().equals(s)){
                return true;
            }
        } 
        return false;
    }
    
    @Override
    public String getStateKey(int x, int y, int koerner) 
    { 
        String s_key="["+x+","+y+","+koerner+"]";
        
        Double v = V.get(s_key);
        if (v==null) V.put(s_key,0.0);
        
        double[] theta = thetas.get(s_key); 
        if ((theta == null) && (!env.isTerminal(x,y))) {
            theta = new double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(theta,0.0);
            List <Integer> A_s = env.coursesOfAction(s_key);
            for (int a : A_s) theta[a]=0.0;  // initialize theta with 0.0
            thetas.put(s_key,theta);
        }
        return s_key;
    }
    
    @Override
    public void act() 
    {
        String s = getState();
        incN(s);
       
        double[]P = P_Policy(s);
        int a = selectAccordingToDistribution(P);
        
        // Apply transition model. (Consider uncertainties in the result of an action.)
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
        boolean episodeEnd = false;
        if ((env.isTerminal(s_new))||(cnt_steps>=max_steps)) {   
            episodeEnd = true;
            update(episode);
            startNewEpisode();
        }
        
        if (cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0) env.updateDisplay(this);
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