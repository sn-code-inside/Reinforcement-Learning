import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.*;

/**
 * An agent environment for calculating the valuation of the states V(s) in the hamster world using value iteration.
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
public class ValueIteration extends RL_GridEnv
{
    public final static double GAMMA = 0.8; // discount
   
    protected final static double minDelta = 0.001;
    
    protected double[][] V; // S in NxN; V:S->R
    
    public static boolean DISPLAY_UPDATE = false;

    private GreedyHamster hamster = null; 

    protected int worldWidth;
    protected int worldHeight;    
    private int k=0;  
   
    public final static int UNTIL_STABLE = -1;
    
    /**
     * Constructor for objects of class ValueIteration.
     * 
     */
    public ValueIteration()
    {
        /*# Here you can change the arena: */
        super(mapFlat);
        //super(mapWithTrap1);
        
        worldWidth = getWidth();
        worldHeight = getHeight();
        V = new double[worldWidth][worldHeight]; // initialize V(s)
        clearV();
        
        hamster = new GreedyHamster();
        this.addObject(hamster,getHamsterStartX(),getHamsterStartY());
        
        iterationFinished=false; 
        DISPLAY_UPDATE = true;
    }

    public void act() 
    {
        if(!iterationFinished) { 
            iterationFinished=evaluateStates();
            this.updateDisplay();
        }
    }
    
    /**
     * Performs a ValueIteration completely up to the termination criterion.
     * @param n maximum number of iterations
     */
    public void iterate(int n)
    {
        clearV();
        int k=0;
        boolean minDeltaReached = false;
        while (!minDeltaReached) { 
            if ((n!=UNTIL_STABLE) && (k>=n)) break;   
            minDeltaReached=evaluateStates(); 
            k++;
        }    
    }
    
    /**
     * Performs a sweep over all states s of the state space S (makes a "sweep").
     */
    private boolean evaluateStates()
    {
        List objects;
        double maxDelta=0.0;
        // For each field s from S (which is not a wall or the terminal state), update the field value V(s).
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                objects = this.getObjectsAt(i,j,Wall.class);
                if (( objects.size()==0 ) && (!isTerminal(i,j))){
                    double v_alt = V[i][j]; 
                    Actionvalue maxAW = targetOrientedEvaluation(i,j);
                    V[i][j] = maxAW.v;
                    double delta = V[i][j]-v_alt;          
                    if (maxDelta<delta) maxDelta = delta;
                }
            }
        }
        return (maxDelta<=minDelta);
    }
  
    /**
     * Sum over P(s'|s,a)*V(s') ,for all s' from S ( for which P(s')>0 ), i.e. add up probability*valuation for each subsequent state.
     * @param subsequentStates List of possible consequential states with their probabilities.
     * @return weighted sum of the valuations of the possible subsequent states (of an action).
     */
    public double weightedValuation(ArrayList <Transition> subsequentStates) 
    {
        double v = 0.0;
        for (Transition t : subsequentStates){
            v+=t.p*(getReward(t.neighborX,t.neighborY)+GAMMA*getV(t.neighborX,t.neighborY));
        }
        return v;
    };
    
    /**
     * Calculates the value of a state from the best possible subsequent state distribution.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return Actionvalue
     */
    public Actionvalue targetOrientedEvaluation(int x, int y)
    {
        double maxV=Double.NEGATIVE_INFINITY;
        int maxA=-1;
        double value=0;
        List <Integer> A =  coursesOfAction(x,y);
        for ( Integer a : A ){
            ArrayList <Transition> successorStates = RL_GridWorldAgent.successorStateDistribution(this, x,y,a);
            value = weightedValuation(successorStates);
            if ( value>maxV ) {
                maxV=value;
                maxA=a;
            } 
        }
        return new Actionvalue(maxA,maxV);
    }
    
    /**
     * Calculates the expected reward based on a given policy.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return Actionvalue according to given policy.
     */
    public Actionvalue policybasedEvaluation(int x, int y)
    {
        double value=0;
        int maxA = -1;
        double[] P = hamster.P_Policy(x,y);
        for (int a=0;a<P.length;a++){
            if (P[a]>0){
                ArrayList <Transition> successorStates = RL_GridWorldAgent.successorStateDistribution(this,x,y,a);
                value += P[a]*weightedValuation(successorStates);
                maxA=a;
            }
        }
        return new Actionvalue(maxA,value);
    }
    
    /**
     *  Resets all field valuations to 0.
     */
    public void clearV()
    {         
       for ( int i=0;i<V.length;i++ ){
             for( int j=0;j<V[0].length;j++ ) V[i][j]=0.0;          
       }
    }
   
    /**
    * Updates the display of the state value objects in the territory according to V(s).
    */
    public void updateDisplay()
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateValue(i,j);
                
            }
        }
    }
    
    /**
     * Updates the display of the state value objects in the territory according to V(s).
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updateValue(int x, int y)
    {
        Double val = V[x][y];
        if (val!=null) {
            List <Value> valueObjects = this.getObjectsAt(x,y,Value.class);
            Value v = null;
            if ( valueObjects.size()>0 ) {
                v=((Value)valueObjects.get(0));
            }else{
                v = new Value(isTerminal(x,y));
                super.addObject(v,x,y);
            }
            v.update(val);
        }
    }
    
    /**
     * Returns the current state value.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return the current state value.
     */
    public double getV(int x, int y)
    {
        if (((x>=0)&&(x<getWidth())) && ((y>=0)&&(y<getHeight()))){
           if (!isTerminal(x,y)){
               return V[x][y];
           }
        }
        return 0.0;
    }
    
    @Override
    public void removeObject(Actor object)
    {
        super.removeObject(object);
        if (DISPLAY_UPDATE) {
            iterate(ValueIteration.UNTIL_STABLE);
            updateDisplay();
        }
    }

    @Override
    public void addObject(Actor object, int x, int y)
    {
        super.addObject(object,x,y); 
        if (V==null) return;
        if (DISPLAY_UPDATE){ 
           if (object.getClass()==Wall.class) V[x][y]=0.0;
           if ((object.getClass()==Trap.class)||(object.getClass()==Grain.class)){
               V[x][y]=0.0;
               List objects = this.getObjectsAt(x,y,Value.class);
               if ( objects.size()>0 ) {
                    Value v = ((Value)objects.get(0));
                    v.setTerminal(true);
               }
           }
           iterate(ValueIteration.UNTIL_STABLE);
           updateDisplay();
        }
    }
}
