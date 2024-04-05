import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.LinkedList;

/**
 * Write a description of class PPO_Hamster_MT here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PPO_Hamster_MT extends A3C_Hamster
{
        protected double[] pi_s_old = null;
    private LinkedList <AdvPi> advantages = new LinkedList <AdvPi> ();
    private int horizon = 5;
    private double ETA_ppo = 1; //0.2;
    private double EPSILON_ppo = 0.2;
    
    private double determ_bound = 0.999999; // bound for saying "pi(s) is deterministic.", to avoid infinte thetas.
    
    private double THETA_MAX = 500;
    private double THETA_MIN = -500;
    /**
     * Act - do whatever the PPO_Hamster_MT wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act() 
    {
        // Add your action code here.
    }    
}
