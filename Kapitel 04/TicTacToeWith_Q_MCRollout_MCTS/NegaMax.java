import greenfoot.*;
import java.util.ArrayList;

/**
 * Optimal playing TicTacToe agent based on NegaMax algorithm.
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
public class NegaMax extends TicTacToe_Agent    
{
 
    public NegaMax(char player,TicTacToe_Env env, String ident)
    {
        super(player,env, ident);
    }
    
    /**
     * Evaluates an action in a state for one of the players based on the field state attribute (the 9 TicTacToe fields, 
     * they are either '-','x' or 'o'). The recursive NegaMax algorithm is used for this purpose. The actions of the
     * agent are selected with the (greedy-)policy of the superclass.
     * 
     * @param action Number of the field into which is set.
     * @param player the character of the player ('x' or 'o')
     * @return Evaluation of the action from the point of view of the specified player
     */
    public double evaluateAction( int action, char player ) 
    {
        // generate state (do action as trial)
        state[action]=player;
        
        double reward = getReward(state, player);
        if (reward!=0) {
            state[action]='-';
            return reward;
        }
        
        player = (player=='o') ? 'x':'o';
        ArrayList <Integer> A_s = coursesOfAction(state);
        if (A_s.size()==0){
            state[action]='-';
            return 0; // If the field is full, the game is over and the return is 0.  
        }

        // Evaluation without reward: determine best possible action for the opponent after our move, 
        // minimum evaluation for it ("worst-case" assumption).
        double maxNegative = Double.POSITIVE_INFINITY;
        for (int i=0;i<A_s.size();i++)
        {
           double value = -evaluateAction( A_s.get(i), player ); 
           if (value < maxNegative) {
              maxNegative = value;
           }
        }
        
        state[action]='-'; // undo the trial
        return maxNegative;
     }
}