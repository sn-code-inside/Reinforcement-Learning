/**
 * episode element: (state, action, reward, next state, next action)
 * 
 */
public class Sarsa  
{
    public State s=null;
    public int action=-1;
    public double reward=0;
    public State s_new=null;
    public int action_new=-1;
    public boolean episodeEnd = false;

    public Sarsa(State s, int action, double reward, State s_new, int aktion_new, boolean episodeEnd)
    {
        this.s=s;
        this.action=action;
        this.reward=reward;
        this.s_new=s_new;
        this.action_new=action_new;
        this.episodeEnd = episodeEnd;
    }
}