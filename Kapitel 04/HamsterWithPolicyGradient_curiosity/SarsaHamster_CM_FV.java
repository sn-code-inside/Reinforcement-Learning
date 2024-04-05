import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
/**
 * SarsaHamster CM FV
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
public class SarsaHamster_CM_FV extends SarsaHamster_FV
{
    public final static double REWARD_INTRINSIC_CURIOSITY = 1;
    public final static double REWARD_INTRINSIC_BOREDOM = 0;
    
    protected Map <String, HashMap<Integer, List <SubsequentObservation>>> model2; 
    
    public SarsaHamster_CM_FV()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_hellgruen.png");
        else
            setImage((GreenfootImage)null);

        model2 = new HashMap<String, HashMap<Integer, List <SubsequentObservation>>> (); 
    }
    
    @Override
    public void act() 
    {
        if (env==null) return;
        
        if ((xs_new==null)||(xs==null)) {
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
        double r_e = env.getReward(xs_new);
        sum_reward+=r_e;
   
        // update model and intrinsic curiosity
        double r_i = setToModel_C(xs,a,xs_new,r_e); 
          
        double r = r_e+r_i;

        episodeEnd = false;
        if ((env.isTerminal(getX(),getY()))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }
                  
        if ((env.DISPLAY_UPDATE)&&(cnt_steps%env.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
        // if ((env.DO_EVALUATION_PHASE)&&(this.evaluationPhase)) env.putTracemarker(getX(),getY(),a,1.0);
        
        // Sarsa-update
        update(xs,a,r,xs_new,a_new,episodeEnd);  
        xs=xs_new; a=a_new; 
 
        // episode end reached?
        if (episodeEnd) {   
           startNewEpisode();
           xs_new=null;
        }
    }
    
    @Override
    public String getStateKey() 
    { 
        int x = getX();
        int y = getY();
        String xs_key = env.produceFeatureKey(x,y);
        if (!Q.containsKey(xs_key)) {
            Double[] vals = new Double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(vals,0.0); 
            List <Integer> A_s = getPossibleActions(getX(),getY());
            for (int a_s : A_s){
                vals[a_s]=REWARD_INTRINSIC_CURIOSITY;
            }
            Q.put(xs_key,vals);
        } 
        return xs_key;
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
        HashMap <Integer,List <SubsequentObservation>> knownTransitions = model2.get(xs);
        if (knownTransitions==null){
            // no transitions were known in this state.
            knownTransitions = new HashMap <Integer,List <SubsequentObservation>> ();
            ArrayList <SubsequentObservation> obs  = new ArrayList <SubsequentObservation>();
            obs.add(new SubsequentObservation(new Observation(xs_new,r_e+r_i)));
            knownTransitions.put(a,obs);
            model2.put(xs,knownTransitions);
            return r_i;
        }
        List <SubsequentObservation> obs = knownTransitions.get(a);
        if (obs==null){
            // no observations were assigned to this (xs,a) yet.
            obs  = new ArrayList <SubsequentObservation>();
            obs.add(new SubsequentObservation(new Observation(xs_new,r_e+r_i)));
            knownTransitions.put(a,obs);
            model2.put(xs,knownTransitions);
            return r_i;
        }else{
            // Is the observation already known?
            for (SubsequentObservation known_o : obs){
                if (known_o.correspondsTo(xs_new)) {
                    known_o.incN();
                    r_i = REWARD_INTRINSIC_BOREDOM;  // "already_known"->0, maybe, that here is a "surprise"-value possible  
                    known_o.setR(r_e+r_i);
                    break;
                }
            }
            if (r_i==REWARD_INTRINSIC_CURIOSITY) {
                obs.add(new SubsequentObservation(new Observation(xs_new,r_e+r_i)));
            }
            knownTransitions.put(a,obs);
        }
        model2.put(xs,knownTransitions);
        return r_i;
    }
}

