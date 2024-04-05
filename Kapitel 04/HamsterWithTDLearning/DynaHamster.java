import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A hamster agent with "Dyna-Q" algorithm that builds a model supporting model-free learning (Q).
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
public class DynaHamster extends QHamster
{
    protected final int planningIterations = 5;
    protected Map <String, HashMap<Integer, Observation>> model; // Modell: S x A -> R x S
    
    public DynaHamster(){
        super();
        setImage("hamster_gelb.png");
        model = new HashMap <String, HashMap<Integer, Observation>> ();
    }
        
    @Override
    public void act() 
    {
        if (s_new==null) {
            s = getState();
        }else{
            s = s_new;
        }
        
        // apply policy
        double[] P = P_Policy(s); 
        a = selectAccordingToDistribution(P);
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
         
        // get the reward from the environment
        double r = env.getReward(s_new);
        sum_reward+=r;
        
        episodeEnd = false;
        if ((env.isTerminal(s_new))||(cnt_steps>=max_steps)) {   
           episodeEnd=true;
        }   
        
        // Q-update
        update(s,a,r,s_new, episodeEnd); 
         
        // Modell aktualisieren 
        setToModel(s,a,new Observation(s_new,r));
         
        // Simulierte Erfahrungen generieren "Planen"
        for (int i=0;i<this.planningIterations;i++){
             simulateAnExperience();
        }
         
        updateDisplay();
        
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