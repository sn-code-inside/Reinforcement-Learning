import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * RL_GridEnv "hamster-model" gridworld environment for testing reinforcement learning algorithms. 
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
public class RL_GridEnv_FV extends Territory
{
    public static RL_GridWorldAgent_FV hamster = null;
    
    public static final double rewardForAGrain = 1; 
    public static final double rewardTrap = -1;
    public static final double rewardPerTransition = -0.01; 
    // maps
    protected static final String[] empty= 
                                 {"00000",
                                  "00000",
                                  "00M00",
                                  "00000",
                                  "H0000"};
                                  
    protected static final String[] mapRusselNorvig= 
                                 {"0001",
                                  "HM0F",
                                  "0000"};
                                  
    protected static final String[] mapSilver= 
                                 {"MMMMMMM",
                                  "M00H00M",
                                  "MFM1MFM"};
                                  
    protected static final String[] mapFrozenLake= 
                                 {"H000",
                                  "0F0F",
                                  "000F",
                                  "F001"};
                                  
    protected static final String[] mapDemoTerritory1= 
                                 {"MMMMMMM",
                                  "M1000FM",
                                  "M00000M",
                                  "M00H00M",
                                  "M00000M",
                                  "MF0001M",
                                  "MMMMMMM"};
                                  
    protected static final String[] mapFrochte1= 
                                 {"0000H",
                                  "0000F",
                                  "0000F",
                                  "0000F",
                                  "00001"};
                                  
    protected static final String[] mapWithTrap1= 
                                 {"00000",
                                  "00000",
                                  "0MMM0",
                                  "00000",
                                  "HMFM1"};
                                  
    protected  static final String[] mapWithTrap2= 
                                 {"000000",
                                  "000000",
                                  "000000",
                                  "000000",
                                  "H0FFM1"};
                                  
    protected  static final String[] mapWithTrap3= 
                                 {"0000000",
                                  "0000000",
                                  "00MMMM0",
                                  "0000000",
                                  "H0MFFM1"};
                                  
    protected  static final String[] mapWithTrap4= 
                                 {"0000000",
                                  "0000000",
                                  "H0MFFM1"};
    
    protected static final String[] mapWindyCliff= 
                                 {"000000000000",
                                  "000000000000",
                                  "000000000000",
                                  "000000000000",
                                  "HFFFFFFFFFF1"};
                                  
    protected static final String[] mapDynaMaze= 
                                 {"0000000M1",
                                  "00M0000M0",
                                  "H0M0000M0",
                                  "00M000000",
                                  "00000M000",
                                  "000000000"};
                                  
    protected  static final String[] mapStochastic= 
                                 {"00F00",
                                  "00001",
                                  "H0MMM",
                                  "00001",
                                  "00000"};
                                  
    protected  static final String[] mapStochasticSmall= 
                                 {"0001",
                                  "HMMM",
                                  "0001"};
                                    
    protected  static final String[] mapLabyrinth1= 
                                 {"M0MF0200",
                                  "M0M00MM0",
                                  "M0M0MMM0",
                                  "H0000000",
                                  "M0MMMMMM",
                                  "M000MMMM",
                                  "MMM0001M",
                                  "A000MMMM"};
                                  
    protected static final String[] mapFlat= 
                                 {"MMMMMMMMMMMMMMM",
                                  "M20000000M0100M",
                                  "M00000M000000FM",
                                  "M0100FM00M0000M",
                                  "MMMMMMM00MMMMMM",
                                  "M00000M0000100M",
                                  "M00000000M0000M",
                                  "MH0000M00M0000M", 
                                  "M00000M00M010FM", 
                                  "MMMMMMMMMMMMMMM"};
                                  
    protected static final String[] mapFlat2= 
                                 {"MMMMMMMMMMMMMMM",
                                  "M20000000M0003M",
                                  "M00000M000000FM",
                                  "M0100FM00MF000M",
                                  "MMMM0MM00MMM0MM",
                                  "M00000M0000000M",
                                  "M00000000M0000M",
                                  "MH0000M00M0000M", 
                                  "M00000M00M010FM", 
                                  "MMMMMMMMMMMMMMM"};
                                  
    protected static final String[] mapLongCorridor =
                    {"MMMMMMMM",
                     "00000000",
                     "00000000",   
                     "H0000001", 
                     "00000000",
                     "00000000",
                     "MMMMMMMM"};
                     
    // --- maps with featured walls ---                
    protected static final String[] mapFeatureFlat1= 
                                 {"PPPPPPPMMNNNNNN",
                                  "P20000000N0100N",
                                  "P00000P000000FN",
                                  "P0100FP00N0000N",
                                  "PPPPPPP00NNNNNN",
                                  "MMMMMMM00OOOOOO",
                                  "M00000M0000100O",
                                  "M00000000O0000O",
                                  "MH0000M00O0000O", 
                                  "M00000M00O010FO", 
                                  "MMMMMMMMMOOOOOO"};
                                  
       
    public static final int[][] neighborStates = {{0,-1},{1,0},{0,1},{-1,0}}; //,{0,0}}; would allow action "stay on place".
    public static final int[][] observationArea = {{0,-1},{1,-1},{1,0},{1,1},{0,1},{-1,1},{-1,0},{-1,-1}};
    
    protected boolean iterationFinished = false;
    public final static int WITHMINDELTA = -1;
    
    private int hamsterX = 0;
    private int hamsterY = 0;
    
    protected String[] fieldDescription = null;
                  
    /**
     * Constructor for a grid world territory.
     * @param fielddescription map of the arena
     */
    public RL_GridEnv_FV(String[] fieldDescription)
    {
       super(fieldDescription.length,fieldDescription[0].length());
       this.fieldDescription = fieldDescription;
       arenaSetup(fieldDescription);
    }
    
    /**
     * Size of the action space
     * @return Theoretical number of all possible actions (independent of state)
     */
    public static int getSizeOfActionspace()
    {
       return neighborStates.length;
    }
    
    /**
     * Returns the state of the "iteration finished" flag.
     * @return state of the "iteration finished" flag.
     */
    public boolean isIterationFinished()
    {
        return iterationFinished;
    }
    
    /**
     * Returns reward for the given state.
     * @param s_key state key
     * @return reward for state s
     */
    public double getReward(String s_key )
    {
        return getReward(hamster.getX(),hamster.getY());
    }
    
    /**
     * Returns reward for the specified position in the grid.
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @return reward at the specified position in the grid.
     */
    public double getReward(int x, int y)
    {
        double reward = 0;
        char element = this.getMapElementAt(x,y);
        switch (element) {
            case '1': reward=RL_GridEnv_FV.rewardForAGrain;
                      break;
            case '2': reward=2*RL_GridEnv_FV.rewardForAGrain;
                      break;
            case '3': reward=3*RL_GridEnv_FV.rewardForAGrain; 
                      break;
            case 'F': reward=RL_GridEnv_FV.rewardTrap;  
                      break;
        }
        reward+=rewardPerTransition;      
        return reward;
    }
    
    /**
     * Tests if a state is terminal.
     * @param s_key state key
     * @return true if a state is terminal.
     */
    public boolean isTerminal(String s_key){
        return isTerminal(hamster.getX(),hamster.getY());
    }
    
    /**
     * Tests (and defines) whether a state is terminal.
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @return true if a state is terminal.
     */
    public boolean isTerminal(int x, int y)
    {
        char element = this.getMapElementAt(x,y);
        switch (element) {
            case '1': return true;
            case '2': return true;
            case '3': return true;
            case 'F': return true;     
        }
        return false;
    }
    
    /**
     * Returns a list of possible actions for the agent.
     * @return a list of possible actions.
     */
    public ArrayList <Integer> coursesOfAction()
    {
        ArrayList <Integer> ret = new ArrayList <Integer> ();
        int x = hamster.getX();
        int y = hamster.getY();
        if (!isStatePossible(x,y)) return null;
        for (int n=0; n<neighborStates.length; n++) {
            int neighborX = x+neighborStates[n][0];
            int neighborY = y+neighborStates[n][1];
            if (isStatePossible(neighborX,neighborY)) {
                ret.add(new Integer(n));
            }
        }    
        return ret;
    }
    
    /**
     * Tests if there is a wall on the field or if it is "out of the world".
     * @param x column
     * @param y row
     * @return false if there is a wall on the field or if it is "out of the world", true otherwise.
     */
    public boolean isStatePossible(int x, int y)
    {
        if (((x<0)||(x>= this.getWidth())) || ((y<0)||(y>=this.getHeight()))) return false;
        char element = getMapElementAt(x,y);
        switch (element) {
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q': return false;
        }
        return true;
    }
    
    /**
     * Uses map array to identify elements of the, no moveable actors can be recognized (critical for execution speed).
     * @param x column
     * @param y row
     * @return character that describes the specified tile
     */
    public char getMapElementAt(int x, int y){
        return (fieldDescription[y]).charAt(x);
    }
    
    /**
     * Storing a Actor object for displaying state values on all fields where there is no wall.
     */
    protected void depositStateValueObjects()
    {
       Visitcounter.resetCMax();
       int weltBreite = getWidth();
       int weltHoehe = getHeight();
       for ( int i=0;i<weltBreite;i++ ){
            for( int j=0;j<weltHoehe;j++ ){
                List objects = this.getObjectsAt(i,j,Wall.class);
                if ( objects.size()==0 ) {            
                    super.addObject(new Visitcounter(0),i,j);
                }
            }
        }
    } 
    
    /**
     * Produces the elements of the agentsn environment according to the field description string.
     * @param field
     */
    public void arenaSetup(String[] field)
    { 
        for (int i=0; i<field.length; i++){
            for (int j=0; j<field[i].length(); j++){
                char c = field[i].charAt(j);
                switch (c) {
                    case 'M': 
                            this.addObject(new Wall(),j,i);
                            break;
                    case 'N': 
                            this.addObject(new Wall1(),j,i);
                            break;
                    case 'O': 
                            this.addObject(new Wall2(),j,i);
                            break;
                    case 'P': 
                            this.addObject(new Wall3(),j,i);
                            break;
                    case 'Q': 
                            this.addObject(new Wall4(),j,i);
                            break;
                    case '1':
                            this.addObject(new Grain(),j,i);
                            break;
                    case '2':
                            this.addObject(new Grain(2),j,i);
                            break;
                    case '3':
                            this.addObject(new Grain(3),j,i);
                            break;
                    case 'F':
                            this.addObject(new Trap(),j,i);
                            break;
                    case 'H':
                            setHamsterStart(j,i);
                            break;
                }
            }
        }
    }
    
    /**
     *  Returns the X start position of the hamster agent.
     */
    public int getHamsterStartX()
    {
        return hamsterX;
    }
   
    /**
     * Returns the Y start position of the hamster agent.
     */
    public int getHamsterStartY()
    {
        return hamsterY;
    }
    
    /**
     * Defines the start position.
     * @param x column in the gridworld
     * @param y row in the gridworld
     */ 
    public void setHamsterStart(int x, int y)
    {
        this.hamsterX=x;
        this.hamsterY=y;
    }
}
