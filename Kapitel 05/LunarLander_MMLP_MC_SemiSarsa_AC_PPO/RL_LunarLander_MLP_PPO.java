import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.LinkedList;

/**
 * This class describes an agent that learns with a kind of "single thread"-PPO update rule (actor-critic style).
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
public class RL_LunarLander_MLP_PPO extends RL_MLP_ActorCritic_Lander
{
    private LinkedList <AdvPi> advantages = new LinkedList <AdvPi> ();
    private int horizon = 12;
    public double ETA_ppo = 0.5;
    private double EPSILON_ppo = 0.2;
    
    private double determ_bound = 0.999999; // bound for saying "pi(s) is deterministic.", to avoid infinte thetas.
 /*   
    private double THETA_MAX = 500;
    private double THETA_MIN = -500; */

    protected void update( State s, int a, double reward, State s_new, boolean episodeEnd ) 
    { 
        double[] x_s =  s.getFeatureVector();        
        
        double observation = 0.0; 
        if (episodeEnd) {
            observation = reward;    
        }
        else {
            observation = reward + (GAMMA * getV(s_new));  
        } 
        
        // TD-error
        double V_is = getV(s);
        double delta=observation-V_is;
      
        // Update "critic"
        addV_MLP(x_s,observation); // batch update
        
        // Update "actor"   
        double[] pi_s = P_Policy(s); 
        advantages.add(new AdvPi(s,a,delta,pi_s[a]));
        
        if (cnt_steps%horizon==0) {  // If horizon is reached, get current policy values and update with the clipped pi / pi_old ratio times advantage.
            //System.out.println("UPDATE "+cnt_steps+" theta[s]="+Arrays.toString(getTheta(s))+" pi[s]="+Arrays.toString(P_Policy(s)));
            while (!advantages.isEmpty()){
                AdvPi e = advantages.removeLast();
                double[] pi = P_Policy(e.s);
                if (!determ_pi(pi_s)){ 
                    double r_theta = (pi[e.a]/e.pi_sa);  //  r_theta = pi/pi_old;
                    if (clip(r_theta,1-EPSILON_ppo,1+EPSILON_ppo)!=r_theta) System.out.println("Update episode="+cnt_episodes+" t="+cnt_steps+"e.adv="+e.adv+" r(theta)="+r_theta);
                    double delta_theta = ETA_ppo*min(r_theta*e.adv,clip(r_theta,1-EPSILON_ppo,1+EPSILON_ppo)*e.adv);
                    double[] exs = e.s.getFeatureVector();
                    double[] theta = get_h(exs); 
                    theta[e.a]= theta[e.a]+delta_theta;
                    addH_MLP(exs,theta); // batch update
                }
            }
        }
    } 
    
    private boolean determ_pi(double[] pi_s){
        for (int i=0;i<pi_s.length;i++){
            if (pi_s[i]>determ_bound) return true;
        }
        return false;
    }
   
    private double min(double x1, double x2){
        return (x1<x2) ? x1 : x2;
    }
    
    private double clip(double x, double min, double max){
        if (x>max) return max;
        if (x<min) return min;
        return x;
    }
}
