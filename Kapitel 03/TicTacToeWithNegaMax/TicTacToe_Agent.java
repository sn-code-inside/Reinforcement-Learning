import greenfoot.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Abstract class for an agent that is able to play tictactoe.
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
public abstract class TicTacToe_Agent 
{
    protected static Random random = new Random();
    
    protected String ident = "TicTacToe_Agent";
    
    protected TicTacToe_Env env = null;
    
    protected char ownSign = 'o';
    protected char[] state = new char[9];
      
    public TicTacToe_Agent(char player, TicTacToe_Env env, String ident)
    {
        this.ownSign = player;
        this.env = env;
        this.ident = ident;
        for (int i=0;i<state.length;i++) state[i]='-';
    }
    
    public TicTacToe_Agent(char player, TicTacToe_Env env, String ident, char[] initialState)
    {
        this.ownSign = player;
        this.env = env;
        this.ident = ident;    
        state=initialState.clone();
    }
    
    /**
     * Produces the set of action possibilities A_s for a playing field state.
     * @param board playing field
     * @return List of action possibilities A_s
     */
    public static ArrayList <Integer> coursesOfAction(char[] board)
    {
        ArrayList <Integer> A_s = new ArrayList<Integer> ();
        for (int i=0;i<9;i++){
            if (board[i]=='-'){
                A_s.add(i);
            }
        }
        return A_s;
    }
  
    /**
     * Returns the name of the player (= currently the algorithm ).
     * @return The name of the player.
     */
    public String getIdent()
    {
        return ident;
    }
    
    /**
     * Gets the state the agent currently considers.
     * @return State the agent currently considers.
     */
    public char[] getState(){
        return state;
    }
    
    /**
     * Sets the current board state to the agent.
     */
    public void setState(char[] state){
        this.state=state.clone();
    }
    
    /**
     * Checks, if the state of the board generates a reward for given player.
     * @param state board
     * @param player Player for which the state is checked.
     * @return reward
     */
    public double getReward(char[] state, char player)
    {
        char opponent = (player=='x') ? 'o' : 'x';
        if ( TicTacToe_Env.checkMatrixWon(state)==player) return TicTacToe_Env.REWARD_WIN;
        if ( TicTacToe_Env.checkMatrixWon(state)==opponent) return -TicTacToe_Env.REWARD_WIN;
        return 0;
    }
    
    /**
     * GreedyPolicy:  Returns an action with the best rating for the given state.
     * @param state Represents the state of the board.
     * @return favorite action
     */
    public int policy(char[] state)
    {
        return policy(state, ownSign);
    }

    /**
     * GreedyPolicy:  Returns an action with the best rating for the given state.
     * @param state Represents the state of the board.
     * @param player Symbol of the player for whom the calculation is to be made.
     * @return favorite action
     */
    public int policy(char[] state, char player)
    {
        this.state = state.clone();
        double wmax=-100000;
        ArrayList <Integer> lstAmax= new <Integer> ArrayList();
        ArrayList <Integer> lstW = new <Integer> ArrayList();
        
        ArrayList <Integer> A_s = this.coursesOfAction(state);
        if (A_s.size()==0) { 
            System.out.println("No action possible!");
            return -1;
        }
        
        for (int i=0;i<A_s.size();i++)
        {
            double w = evaluateAction(A_s.get(i),player); 
            lstW.add((int)Math.round(w));
            if (w>wmax) {
                wmax=w;
                lstAmax.clear();
                lstAmax.add(A_s.get(i));
            }else if (w==wmax)
            {
                lstAmax.add(A_s.get(i));
            }
        }
        
        if (TicTacToe_Env.ACTIONVALUES_TO_CONSOLE) System.out.println("State valuation by "+ident);
        if ((TicTacToe_Env.ACTIONVALUES_TO_CONSOLE)||(env.DISPLAY_ACTIONVALUES)) 
        {
            for (int i=0;i<A_s.size();i++) {
                if (TicTacToe_Env.DISPLAY_ACTIONVALUES) env.showText(""+lstW.get(i),A_s.get(i)%3,A_s.get(i)/3);
                if (TicTacToe_Env.ACTIONVALUES_TO_CONSOLE) System.out.println("Q(s,"+A_s.get(i)+") = "+lstW.get(i));
            }
        }
         
        return lstAmax.get(random.nextInt(lstAmax.size())); // Randomly select an action from the list of (equivalent) best.
    }

    /**
     * Evaluates an action in a state for one of the players based on the field
     * state attribute (the 9 TicTacToe fields, they are either '-','x' or 'o').  
     * At this level, a random choice is made between 0 and 100.
     * 
     * @param action Number of the field into which is set.
     * @param player Symbol of the player ('x' or 'o')
     * @return Evaluation of the action from the point of view of the specified player.
     */
    public double evaluateAction( int action, char player )
    {
        return random.nextDouble()*TicTacToe_Env.REWARD_WIN;
    }

    /**
     * Sets the symbol of the agent (X or O).
     * @param c Symbol of the player ('x' or 'o')
     */
    public void setSymbol(char c)
    {
        this.ownSign=c;
    }
    
    /**
     * Returns the name of the algorithm used.
     * @return Name of the algorithm.
     */
    public String getAlgIdent()
    {
        return ident;
    }
}