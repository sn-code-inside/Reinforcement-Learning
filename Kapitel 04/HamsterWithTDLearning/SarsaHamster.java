import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Java-Hamster with sarsa learning
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
public class SarsaHamster extends QHamster
{
    public SarsaHamster(){
        super();
        setImage("hamster_gruen.png");
    }

    /**
     * This method is called once by Greenfoot as soon as the 'Act' button or repeatedly if the 'Run' button is clicked.  
     */
    public void act() 
    {
        if (s_new==null) {
            s = getState();
            double[] P = P_Policy(s); 
            a = selectAccordingToDistribution(P);
        }
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
        
        // apply policy
        double[] P = P_Policy(s_new); 
        int a_new = selectAccordingToDistribution(P);
        
        // get the reward from the environment
        double r = env.getReward(s_new);
        sum_reward+=r;
        
        episodeEnd = false;
        if ((env.isTerminal(s_new))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }
        
        // Sarsa-update
        update(s,a,r,s_new, a_new, episodeEnd); 
        
        s=s_new; a=a_new; 
         
        updateDisplay();
        // episode end reached?
        if (episodeEnd) {   
           startNewEpisode();
           s_new=null;
        }
    } 
    
    /**
     * Update of Q(s,a)  ("Sarsa learning")
     * @param s_key state key
     * @param a action
     * @param reward Reward
     * @param s_key_new Successor state
     * @param a_new Successor state
     * @param end Has a terminal state or the step limit been reached?
     */
    protected void update( String s_key, int a, double reward, String s_new_key, int a_new, boolean end ) 
    { 
        double observation = 0.0;
        if (end) {
            observation = reward; 
        } else {
            observation = reward + (GAMMA * getQ(s_new_key,a_new));    
        }   
        double q = getQ(s_key, a); 
        q = q + ETA * (observation - q); 
        setQ(s_key,a, q); 
    }
}
