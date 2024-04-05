import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * PPO hamster (AC style)
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
public class PPO_Hamster extends ActorCritic_Hamster
{
    private LinkedList <AdvPi> advantages = new LinkedList <AdvPi> ();
    private int horizon = 5;
    private double ETA_ppo = 1; //0.2;
    private double EPSILON_ppo = 0.2;
    
    private double determ_bound = 0.999999; // bound for saying "pi(s) is deterministic.", to avoid infinte thetas.
    
    private double THETA_MAX = 500;
    private double THETA_MIN = -500;
    
    public PPO_Hamster()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_blau.png");
        else
            setImage((GreenfootImage)null);
        imgBackup = this.getImage(); 
        max_episodes = 1000;    // maximum number of episodes
    }
    
    @Override
    protected void update( String s, int a, double reward, String s_new, boolean episodeEnd ) 
    { 
        double observation = 0.0;
        if (episodeEnd) {
            observation = reward; 
        }
        else {
            observation = reward + (GAMMA * getV(s_new));    
        }
        
        double v = getV(s);
        double delta = observation-v;
        
        // update "critic"
        double v_new =  v + ETA_V*delta; 
        setV(s, v_new);
        
        // update "actor"
        double[] pi_s = P_Policy(s);
        
        advantages.add(new AdvPi(s,a,delta,pi_s[a]));
        /* double[] pi = P_Policy(e.s);
         if (!determ_pi(pi_s)){ 
            double r_theta = 1; //pi[e.a]/e.pi_sa;  //  r_theta = pi/pi_old;
            double delta_theta = ETA_ppo*min(r_theta,1-EPSILON_ppo,1+EPSILON_ppo)*delta; //e.advantage;
            double[] theta = getTheta(s); 
            theta[a]= theta[a]+delta_theta;
        } */
        
        if (cnt_steps%horizon==0) {
            //System.out.println("UPDATE "+cnt_steps+" theta[s]="+Arrays.toString(getTheta(s))+" pi[s]="+Arrays.toString(P_Policy(s)));
            while (!advantages.isEmpty()){
                AdvPi e = advantages.removeLast();
                double[] pi = P_Policy(e.s);
                if (!determ_pi(pi_s)){ 
                    double r_theta = (pi[e.a]/e.pi_sa);  //  r_theta = pi/pi_old;
                    System.out.println("Update t="+cnt_steps+" r(theta)="+r_theta);
                    double delta_theta = ETA_ppo*min(r_theta*e.adv,clip(r_theta,1-EPSILON_ppo,1+EPSILON_ppo)*e.adv);
                    double[] theta = getTheta(s); 
                    theta[e.a]= theta[e.a]+delta_theta;
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
