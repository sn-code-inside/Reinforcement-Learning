import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AC DynaHamster ICM FV
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
public class AC_Hamster_CM_FV extends ActorCritic_Hamster_FV
{
    protected final int planningIterations = 0;
 
    protected Map <String, HashMap<Integer, List <SubsequentObservation>>> model; 
 
    public final static double REWARD_INTRINSIC_CURIOSITY = 1;
    public final static double REWARD_INTRINSIC_BOREDOM = -0.02;    
    
    public AC_Hamster_CM_FV()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_rot.png");
        else
            setImage((GreenfootImage)null);
        //model = new HashMap <String, HashMap<Integer, Observation>>  ();
        model = new HashMap<String, HashMap<Integer, List <SubsequentObservation>>> ();
        
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
        double[] P_A = P_Policy(xs);
        a = selectAccordingToDistribution(P_A);
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
        double r_e = env.getReward(xs_new);
        sum_reward+=r_e;
        
        // intrinsic curiosity
        double r_i = setToModel_C(xs,a,xs_new,r_e);
        double r = r_e+r_i;
       
        System.out.println(cnt_steps+": r_e="+r_e+" r_i="+r_i+" r="+r);
        
        episodeEnd = false;
        if ((env.isTerminal(xs_new))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }  
        
        // AC learning (policy update)
        update(xs,a,r,xs_new,episodeEnd); 
 
        //if ((env.DO_EVALUATION_PHASE)&&(this.evaluationPhase)) env.putTracemarker(getX(),getY(),a,1.0);
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
        
        // episode end reached?
        if (episodeEnd) {   
           startNewEpisode();
        }
    }

    /**
     * Adds an experience to the model.
     * @param s state key
     * @param a action
     * @param xs_new subsequent state
     * @param r_e observed extrinsic reward
     */
    public double setToModel_C(String xs, Integer a, String xs_new, double r_e){
        double r_i = REWARD_INTRINSIC_CURIOSITY;
        HashMap <Integer,List <SubsequentObservation>> knownTransitions = model.get(xs);
        if (knownTransitions==null){
            // no transitions were known in this state.
            knownTransitions = new HashMap <Integer,List <SubsequentObservation>> ();
            ArrayList <SubsequentObservation> obs  = new ArrayList <SubsequentObservation>();
            obs.add(new SubsequentObservation(new Observation(xs_new,r_e+r_i)));
            knownTransitions.put(a,obs);
            model.put(xs,knownTransitions);
            return r_i;
        }
        List <SubsequentObservation> obs = knownTransitions.get(a);
        if (obs==null){
            // no observations were assigned to this (xs,a) yet.
            obs  = new ArrayList <SubsequentObservation>();
            obs.add(new SubsequentObservation(new Observation(xs_new,r_e+r_i)));
            knownTransitions.put(a,obs);
            model.put(xs,knownTransitions);
            return r_i;
        }else{
            // Is the observation already known?
            for (SubsequentObservation known_o : obs){
                if (known_o.correspondsTo(xs_new)) {
                    known_o.incN();
                    r_i = this.REWARD_INTRINSIC_BOREDOM;  // "already_known"->0, maybe, that here is a "surprise"-value possible  
                    known_o.setR(r_e+r_i);
                    break;
                }
            }
            if (r_i==REWARD_INTRINSIC_CURIOSITY) {
                obs.add(new SubsequentObservation(new Observation(xs_new,r_e+r_i)));
            }
            knownTransitions.put(a,obs);
        }
        model.put(xs,knownTransitions);
        return r_i;
    }

}
