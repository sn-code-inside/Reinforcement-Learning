/**
 * state-action-reward object
 */
public class Sar  
{
    public State s=null;
    public int action=-1;
    public double reward=0;

    public Sar(State s, int action, double reward)
    {
        this.s=s;
        this.action=action;
        this.reward=reward;
    }

    @Override
    public String toString(){
        return "["+s.toString()+";"+action+";"+reward+"]";
    }
}