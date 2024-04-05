import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * SarsaHamster with feature vector states.
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
public class SarsaHamster_FV extends QHamster_FV
{
    protected int a_new = -1; // next action
    
    public SarsaHamster_FV()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_gruen.png");
        else
            setImage((GreenfootImage)null);
    }
    
    @Override
    public void act() 
    {
        if (env==null) return;
        
        if (xs_new==null) {
            xs = getState();
             // initial application of the policy
            double[] P = P_Policy(xs); 
            a = selectAccordingToDistribution(P);
        }
        // incN(xs,a);
         
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
        xs_new = getState();
        
        // apply policy
        double[] P = P_Policy(xs_new); 
        a_new = selectAccordingToDistribution(P);
         
        // get the reward from the environment
        double r = env.getReward(getX(),getY());
        sum_reward+=r;
        
        episodeEnd = false;
        if ((env.isTerminal(getX(),getY()))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }
        
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
        // if ((env.DO_EVALUATION_PHASE)&&(this.evaluationPhase)) env.putTracemarker(getX(),getY(),a,1.0);
        System.out.println(cnt_steps+": a="+a+" a_new="+a_new);
        
        // Sarsa-update
        update(xs,a,r,xs_new,a_new,episodeEnd);  
        
        xs=xs_new; a=a_new; 
        
        // episode end reached?
        if (episodeEnd) {   
           startNewEpisode();
           xs_new=null;
        }
    }
    
    /**
     * Update of Q(s,a)  ("Sarsa learning" approach)
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
        q = q + ETA_Q * (observation - q); 
        setQ(s_key,a, q); 
    }
}
