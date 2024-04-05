import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;

/**
 * A greedy hamster agent who always follows the neighbor state with the highest rating. 
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
public class GreedyHamster extends RL_GridWorldAgent
{
    protected ValueIteration env = null;
    protected static int numHamsters = 0;
    protected String hamsterName = "";
    
    public GreedyHamster()
    {
        super();
        setImage("hamster_rot.png");
        numHamsters++;
        hamsterName= this.getClass().getName()+" "+numHamsters;
    }
    
    @Override
    public void addedToWorld(World world){
        env = (ValueIteration) world;
    }
    
    @Override
    public String getState(){
        return getStateKey(getX(),getY(),getGrainsInJaws());
    }
    
    /**
    * Produces a string that represents a state.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
    public String getStateKey(int x, int y, int score) 
    { 
        return "["+x+","+y+","+score+"]";   
    } 
    
    @Override
    public void act() 
    {
        if (!(env.isIterationFinished())){
            return;
        }
        int x = this.getX();
        int y = this.getY();
       
        double[]P = P_Policy(x,y);
        int a = selectAccordingToDistribution(P);
        
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
      
        if (env.isTerminal(getX(),getY())){
            Greenfoot.stop();
        }else{
            if (isGrainThere()) {
                take();
                env.clearV();
                env.iterate(ValueIteration.UNTIL_STABLE);
            }
        }
    }
    
    /** 
     * Policy of the agent. Here you can set the behavior of the hamster.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return probability-distribution over actionspace
     */
    public double[] P_Policy(int x, int y)
    {
        return P_Policy_withTransitionmodel(x,y);
    }
    
    /**
     * "Greedy" policy. The agent follows the state with the highest rating. (Multiple
     * maximums may occur).
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return probability-distribution over actionspace
     */
    public double[] P_Policy_Deterministic(int x, int y)
    {
       List objects=null;
       double max=Double.NEGATIVE_INFINITY;
       double value=0.0;
       double[] P = new double[SIZE_OF_ACTIONSPACE];

       List <Integer> A_s = env.coursesOfAction(x,y);
       
       // Evaluation of the possibilities, create list of maxima:
       ArrayList <Integer> A_max = new <Integer> ArrayList(P.length);
       for ( Integer a : A_s ){        
          int neighborX = x+env.neighborStates[a][0];
          int neighborY = y+env.neighborStates[a][1];
          value = env.getReward(neighborX,neighborY)+env.GAMMA*env.getV(neighborX,neighborY);
          if ( value>max ) {
              max=value;
              A_max.clear();
              A_max.add(a); 
              if ( value==max ) {
                  A_max.add(a);
              }
          }
       }
       
       // The multiple maxima, if any, share the probability of 1 equally.
       double p=1.0/A_max.size();
       Arrays.fill(P,0.0);
       for (Integer a : A_max){
           P[a]=p;
       }
       
       return P;
    }
    
    /**
     * Policy that take into account the transition probabilities. The agent follows the action with 
     * the best subsequent state distribution.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return probability-distribution over actionspace
     */
    public double[] P_Policy_withTransitionmodel(int x, int y)
    {
       List objects=null;
       double max=Double.NEGATIVE_INFINITY;
       double value=0.0;
       double[] P = new double[SIZE_OF_ACTIONSPACE];
       List <Integer> A_s = env.coursesOfAction(x,y);
       
       // Evaluation of the possibilities, create list of maxima:
       ArrayList <Integer> A_max = new <Integer> ArrayList(P.length);
       for ( Integer a : A_s ){ 
          ArrayList <Transition> subsequentStates = successorStateDistribution(env,x,y,a);
          value = env.weightedValuation(subsequentStates);       
          if ( value>max ) {
              max=value;
              A_max.clear();
              A_max.add(a); // neues absolutes Maximum
          }else{
              if ( value==max ) {
                  A_max.add(a); // zur "Bestenliste" hinzufügen
              }
          }
       }
       
       // The multiple maxima, if any, share the probability of 1 equally.
       double p=1.0/A_max.size();
       Arrays.fill(P,0.0);
       for (Integer a : A_max){
           P[a]=p;
       }
       
       return P;
    }
    
    /** 
     * Overloads the standard function "take" from the hamster model. The purpose is to recalculate 
     * the ratings after the grain is taken.
     */
    @Override
    public void take() throws KachelLeerException {
        super.take();
        if (env.DISPLAY_UPDATE){
            env.iterate(ValueIteration.UNTIL_STABLE);
            env.updateDisplay();
        }
    }
}
