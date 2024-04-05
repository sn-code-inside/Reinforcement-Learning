/**
 * Episode element: (state, action, reward, next state, next action)
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
public class Sarsa  
{
    public State s=null;
    public int action=-1;
    public double reward=0;
    public State s_new=null;
    public int action_new=-1;
    public boolean episodeEnd = false;

    public Sarsa(State s, int action, double reward, State s_new, int action_new, boolean episodeEnd)
    {
        this.s=s;
        this.action=action;
        this.reward=reward;
        this.s_new=s_new;
        this.action_new=action_new;
        this.episodeEnd = episodeEnd;
    }
}