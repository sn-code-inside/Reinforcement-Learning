import greenfoot.*;
import java.util.ArrayList;
import java.util.*;

/**
 * TicTacToe-Agent that valuates actions with a Monte-Carlo Rollout.
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
public class MonteCarlo_Rollout extends TicTacToe_Agent    
{
    protected int samplesNumber = 100; // default number of MC rollouts
    public boolean konsolenausgabe_rollouts = false;
    
    public MonteCarlo_Rollout(char player,TicTacToe_Env env, String ident)
    {
        super(player,env, ident);
    }
    
    /**
     * Evaluates an action in a state for one of the players based on the field state attribute
     * (the 9 TicTacToe fields, they are either '-','x' or 'o'). The average result of the
     * MonteCarlo-rollouts is used for this. The agent uses the greedy policy of the superclass.
     * 
     * @param action Number of the field to be set
     * @param player the player's sign ('x' oder 'o')
     * @return Evaluation of the action from the point of view of the specified player
     */
    public double evaluateAction( int action, char player ) 
    {
        double sum = 0;
        for (int i=1;i<=samplesNumber;i++)
        {
            if (konsolenausgabe_rollouts){
                System.out.println("Start rollout "+i+" (valuation of a="+action+")");   
            }
            double v = rollout_evaluation(action,player);
            sum += v;
                     
            if (konsolenausgabe_rollouts){
                System.out.println("result  rollout: v_"+i+"="+String.format(Locale.ENGLISH,"%3.1f", v)+"  v_m_"+i+"="+String.format(Locale.ENGLISH,"%3.1f",sum/i)+"(valuation of a="+action+")");   
            }
        }
        return sum/samplesNumber;
    }
    
    /**
     * Runs a random game based on the given field state. Returns only the result.
     * 
     * @param action Number of the field into which is set.
     * @param player the character of the active player ('x' or 'o')
     * @return Evaluation of the action from the point of view of the specified player.
     */
    public double rollout_evaluation( int action, char player ) 
    {
        // run action on a trial basis
        state[action]=player;
        
        if (konsolenausgabe_rollouts){
            System.out.println(TicTacToe_Env.matrixToString(state));
        }
        
        // If field state generates a reward for the player, then return the reward and you're done.
        double reward = getReward(state, player);
        if (reward!=0) {
            state[action]='-';
            return reward;
        }
        
        // opponent
        player = (player=='o') ? 'x':'o';
        ArrayList <Integer> A = coursesOfAction(state);
        if (A.size()==0){
            state[action]='-';
            return 0; // Falls Feld voll, dann fertig und Rückgabe 0.  
        }
        
        // choose random action for the opponent
        double value = -rollout_evaluation( A.get(random.nextInt(A.size())), player ); // Ergebnis negativ bewerten
        
        //  Undo 'test execution'
        state[action]='-'; //
        
        return value;
     }
     
     /**
      * Sets the number of rollouts to be performed in each case.
      * @param n Sample size of MC rollout evaluation.
      */
     public void setMaxRollouts(int n){
         this.samplesNumber=n;
     }
     
     /**
      * Returns the number of rollouts to be performed in each case.
      * @return n  Sample size of MC rollout evaluation.
      */
     public int getMaxRollouts(){
         return this.samplesNumber;
     }
     
}
