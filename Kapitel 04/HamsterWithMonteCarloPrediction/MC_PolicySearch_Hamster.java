import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Arrays;

/**
 *  A hamster agent conducting a Monte Carlo policy search.
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
public class MC_PolicySearch_Hamster extends MC_Hamster
{
    protected Map <String, double[]> pi = new HashMap <String, double[]>(); // pi: s -> P  (P in R^n)
    protected Map <String, Double[]> Q = new HashMap <String, Double[]>(); // Q: S x A -> R
    
    protected boolean episodeEnd = false;
    
    protected final double minQVal = 0.00001;// Minimum amount for Q(s,a). Should help against side effects due to rounding or truncated decimal places. 
    
    public MC_PolicySearch_Hamster() {
        super();
        setImage("hamster_tuerkis.png");
        imgBackup = this.getImage();
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
     * Update der Policy mit einer episodischen Auswertung.
     * @param episode Enthält die Folge von "Erfahrungen" der Episode jeweils bestehend aus Zustand,Aktion und Reward.
     */
    protected void update( LinkedList <Experience> episode ) 
    {
        double G = 0;   
        while (!episode.isEmpty()){
            Experience e = episode.removeLast();
            String s_e = e.getS();
            int a_e = e.getA();
            G=GAMMA*G+e.getR();
            if (!contains_sa(episode,s_e,a_e)){
                double avG = getQ(s_e,a_e);
                int numGs = incN(s_e,a_e);
                avG = (avG*(numGs-1)+G)/numGs;
                setQ(s_e,a_e,avG);
                incN(s_e);
                int a_max = getActionWithMaxQ(s_e);
                   
                // epsilon-greedy
                ArrayList <Integer> A_s = env.coursesOfAction(s_e);  
                double[] P = new double[SIZE_OF_ACTIONSPACE];
                int k = A_s.size();
                for (int a_i : A_s){
                    P[a_i] =  current_epsilon/k;
                }
                if (a_max>=0) P[a_max]+=(1-current_epsilon);  // Bei a_max die Wahrscheinlichkeit 1-Epsilon addieren.
                   
                // policy update
                setPi(s_e,P);
            }
        }
    }
    
    /**
     * Stochastic policy of the agent. In this case (MC policy-search) it just returns the
     * current table entry pi(s).
     * @param s_key state key
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    @Override
    public double[] P_Policy(String s_key)
    {
        return pi.get(s_key);
    }
    
    /**
     * Assigns a probability distribution over the actions to a state.
     * @param s_key state key
     * @param P Probability distribution for the actions a from [0,1,...,size of actionspace-1].
     */
    public void setPi(String s_key, double[] P){
        pi.put(s_key,P);
    }

    /**
     * Returns the probability distribution over the actions saved for a state.
     * @param s_key state key
     * @param P Probability distribution for the actions a from [0,1,...,size of actionspace-1].
     */
    public double[] getPi(String s_key){
        return pi.get(s_key);
    }
    
    /**
     * Tests if a state-action pair appears in an episode.
     * @param episode List of "experiences": ...,(s,a,r),... 
     * @param s state key
     * @return True, if (s,a) appears in an episode. False otherwise.
     */
    private boolean contains_sa(LinkedList <Experience> episode, String s, int a){
        ListIterator <Experience> iterator = episode.listIterator(); 
        while (iterator.hasNext()) {
            Experience e_i = iterator.next(); 
            if ((e_i.getA()==a) && (e_i.getS().equals(s))){
                return true;
            }
        } 
        return false;
    }
 
    /**
     * Creates the key for accessing the tables of pi and Q. If the state is unknown for one of them,
     * the corresponding record is created.
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
        double[] P = getPi(key);
        if (P==null) {
            P = new double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(P,0.0);
            ArrayList <Integer> A_s = env.coursesOfAction(x,y);
            for (int a_i:A_s){
                P[a_i]=1.0/A_s.size();
            }
            setPi(key,P);
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