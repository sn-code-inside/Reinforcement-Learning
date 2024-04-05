import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ActorCritic DynaHamster with FV
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
public class AC_DynaHamster_FV extends ActorCritic_Hamster_FV
{
    protected final int planningIterations = 50;
    protected Map <String, HashMap<Integer, Observation>> model; // Modell: S x A -> R x S

    public AC_DynaHamster_FV()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_orange.png");
        else
            setImage((GreenfootImage)null);
        model = new HashMap <String, HashMap<Integer, Observation>>  ();
    }
    
    @Override
    public void act() 
    {
        if (xs_new==null) {
            xs = getState();
        }else{
            xs = xs_new;
        }
        
        // apply policy
        double[] P = P_Policy(xs); 
        a = selectAccordingToDistribution(P);
        //incN(xs,a);
         
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
        xs_new = getState();
         
        // get the reward from the environment
        double r = env.getReward(xs_new);
        sum_reward+=r;
        
        episodeEnd = false;
        if ((env.isTerminal(xs_new))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }   
        
        // AC learning
        update(xs,a,r,xs_new,episodeEnd);
        
        // update model
        setToModel(xs,a,new Observation(xs_new,r));
         
        // simulate experiences "planning"
        for (int i=0;i<this.planningIterations;i++){
             simulateAnExperience();
        }
         
        //if ((env.DO_EVALUATION_PHASE)&&(this.evaluationPhase)) env.putTracemarker(getX(),getY(),a,1.0);
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
        
        // episode end reached?
        if (episodeEnd) {   
           startNewEpisode();
        }
    }
    
    /**
     * Simulates an experience by using a random observation in the past.
     */
    public void simulateAnExperience(){
        String[] s_keys = model.keySet().toArray(new String[0]);
        String s_sim = s_keys[random.nextInt(s_keys.length)];
        HashMap <Integer,Observation> knownTransitions = model.get(s_sim);
        Integer[] actions = (Integer[])knownTransitions.keySet().toArray(new Integer[0]);
        int a_sim = actions[random.nextInt(actions.length)];
        Observation b = knownTransitions.get(a_sim);
        update(s_sim,a_sim,b.getR(),b.getS(),env.isTerminal(b.getS())); // policy update (Q-table)
    }
    
    /**
     * Predicts next observation given current observation and action.
     * @param xs observation
     * @param action action
     * @return predicted subsequent observation
     */
    public Observation makePrediction(String xs, int action){
        HashMap <Integer,Observation> knownTransitions = model.get(xs);
        Observation pred_o = knownTransitions.get(action);
        return pred_o;
    }
    
    /**
     * Adds an experience to the model.
     * @param s state key
     * @param a action
     * @param obs (subsequent state and reward)
     */
    public void setToModel(String s, Integer a, Observation obs){
        HashMap <Integer,Observation> value = model.get(s);
        if (value==null){
            value = new HashMap <Integer,Observation> ();
        }
        value.put(a,obs);
        model.put(s,value);
    }
}
