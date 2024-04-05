/**
 * Helper structure for propabilistic world model.
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
public class SubsequentObservation  
{
    public Observation o;
    private int n = 0;
    
    public SubsequentObservation( Observation o )
    {
        this.o = o;
        this.n = 1;
    }
    
    public boolean correspondsTo(Observation o2){
        return o.getS().equals(o2.getS());
    }
    
    public boolean correspondsTo(String xs_o2){
        return o.getS().equals(xs_o2);
    }
    
    public void incN(){
        this.n++;
    }
    
    public int getN(){
        return n;
    }
    
    public void setR(double r){
        o.setR(r);
    }
    
    public Observation getO(){
        return o;
    }
}
