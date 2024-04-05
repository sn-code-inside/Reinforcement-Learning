import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.*;

/**
 * A hamster agent with "on-policy" sarsa learning. In each single step, the TD error is evaluated 
 * and the (tabular) state-action score is updated accordingly. In addition, an eligibility trace 
 * is included to extract more information from the observations of a single episode.
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
public class SarsaLambdaHamster extends SarsaHamster
{
    protected Map <String, HashMap<Integer, Double>> e;
    protected final double LAMBDA = 0.9 ;
       
    public SarsaLambdaHamster(){
        super();
        e = new HashMap <String, HashMap<Integer, Double>>();
        setImage("hamster_hellgruen.png");
    }

    @Override
    protected void update( String s, int a, double reward, String s_new, int a_new, boolean episodeEnd ) 
    {
        double observation = 0.0;
        if (episodeEnd) {
            observation = reward; 
        } else {
            observation = reward + (GAMMA * getQ(s_new,a_new));    
        }   
        double TD_error = observation - getQ(s,a); 
        setE(s,a,1);
        for(Map.Entry <String, HashMap<Integer, Double>> eItem : e.entrySet()){
            String keyS = (String)(eItem.getKey());
            HashMap <Integer, Double> values = eItem.getValue();
            for (HashMap.Entry <Integer,Double> valItem : values.entrySet()){
                Integer a_e = valItem.getKey();
                Double e = valItem.getValue(); // eligibility value
                if (e>0) {
                    double q_old = getQ(keyS,a_e);
                    double q_new = q_old + ETA * TD_error * e;
                    setQ(keyS, a_e, q_new); 
                    setE(keyS, a_e, GAMMA*LAMBDA*e); // decrease of the eligibility value
                }
            }
        }
    }
    
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
        
        e.clear();  
        a=-1;
        env.removePolicymarkers();
        current_epsilon+=this.delta_epsilon; // reduces epsilon during learning (if desired)
        cnt_steps=0;
        cnt_episodes++;
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
    }
    
    /**
     * Sets a eligibility value to a state action pair.
     * @param s state key
     * @param a action
     * @param eligibility value    
     */
    protected void setE(String s, int a, double v) 
    {    
        HashMap <Integer, Double> eav = e.get(s);
        if (eav==null){
            eav = new HashMap <Integer, Double> ();
        }
        eav.put(a,v);
        e.put(s,eav);
    } 
 
    /**
     * Gets a eligibility value of a state action pair.
     * @param s state key
     * @param a action
     * @param eligibility value  
     */
    public double getE(String s, int a) 
    {
        HashMap <Integer, Double> values = e.get(s);
        if (values==null) return 0;
        Double ev = values.get(a);
        if (ev==null) return 0;
        return ev; 
    }  
    
    /**
     * Gets a list of all eligibility values of a state.
     * @param s state key
     * @param a action
     * @param eligibility value  
     */
    public HashMap <Integer, Double> getEValues(String s)
    {
        return e.get(s);
    }
    
    @Override
    protected void updateDisplay(){
        if (this.evaluationPhase) env.putTracemarker(getSX(s),getSY(s),a,1.0);
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
    }
}