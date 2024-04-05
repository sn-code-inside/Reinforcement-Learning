import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Arrays;
import java.util.List;

/**
 * A hamster agent with a policy that can tabulate any action to any state.
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
public class HamsterWithAdjustablePolicy extends RL_GridWorldAgent
{
    protected PolicyIteration env = null;
    
    protected static int numHamsters = 0;
    protected int[][] pi=null; // Holds the action assigned to the state.
    protected String hamsterName = "";
    
    public HamsterWithAdjustablePolicy()
    {
        super();
        setImage("hamster_gruen.png");
        numHamsters++;
        hamsterName= this.getClass().getName()+" "+numHamsters;
    }
    
    @Override
    public void addedToWorld(World world){
        env = (PolicyIteration) world;
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
     * Flexible deterministic policy (with random initialization), a certain action is assigned to each state.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return deterministic probability distribution (one action has probability 1.0)
     */
    public double[] P_Policy(int x, int y)
    {
        double[] P = new double[env.neighborStates.length];
        Arrays.fill(P,0.0);
        int a=getPi(x,y);
        P[a]=1.0;
        return P;
    }
    
    /**
     * Returns the action stored for a state.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return The action that is stored at the state.
     */
    public int getPi(int x, int y)
    {
        return pi[x][y];
    }
    
    /**
     * Stores an action for a state.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @param a Action to be stored at the state.
     */
    public void setPi(int x, int y, int a)
    {
        this.pi[x][y]=a;
    }
    
    /**
     * Initializes the assignment state->action for each world state s from S arbitrarily.
     */
    public void setArbitraryPolicy( )
    {
        int worldWidth = env.getWidth();
        int worldHeight = env.getHeight();
        this.pi = new int[worldWidth][worldHeight];
        // Für jedes Feld s aus S (was keine Wand oder der Terminalzustand ist) den Feldwert V(s) aktualisieren.
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                List objects = env.getObjectsAt(i,j,Wall.class);
                if (( objects.size()==0 ) && (!env.isTerminal(i,j))){
                    List <Integer> A_s = env.coursesOfAction(i,j);
                    int a_nr=random.nextInt(A_s.size());
                    setPi(i,j,A_s.get(a_nr)); // pi(s) <- a
                }
            }
        }
    }
    
    
    @Override
    public String getState(){
        return getStateKey(getX(),getY(),getGrainsInJaws());
    }
    
    /**
     * Produces a string that represents a state.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @return state key
     */
    public String getStateKey(int x, int y, int score) 
    { 
        return "["+x+","+y+","+score+"]";   
    } 
}
