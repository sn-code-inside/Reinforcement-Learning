import greenfoot.*;
import java.util.Random;

/**
 * Randomly moving TicTacToe agent.
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
public class Arbitrary extends TicTacToe_Agent 
{
    private Random random = new Random();

    public Arbitrary(char spieler,TicTacToe_Env env, String ident)
    { 
        super(spieler, env, ident);
    }

    /**
     * Evaluates an action in a state for one of the players based on the 
     * field state attribute (the 9 TicTacToe fields, they are either '-','x' or 'o').
     * For this, a number between 0 and 100 is drawn at random.
     * 
     * @param action Number of the field into which is set.
     * @param player the character of the player ('x' or 'o')
     * @return Evaluation of the action from the point of view of the specified player
     */
    public double evaluateAction( int action, char player ) 
    {
        return random.nextDouble()*TicTacToe_Env.REWARD_WIN;
    }
}
