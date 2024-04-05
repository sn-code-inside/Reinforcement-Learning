/**
 * Observation of a transition result (s,r)
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
public class Observation  
{
    private String s;
    private double r;

    public Observation(String s, double r)
    {
        this.s=s;
        this.r=r;
    }

    public String getS(){
        return s;
    }
       
    public double getR(){
        return r;
    }
    
    public String toString(){
        return "("+s+","+r+")";
    }
}
