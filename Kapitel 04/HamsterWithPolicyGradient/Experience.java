/**
 * Element of an episode (s_t,a_t,r_t+1)
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
public class Experience  
{
    private String s;
    private int a;
    private double r;

    public Experience(String s, int a, double r)
    {
        this.s=s;
        this.a=a;
        this.r=r;
    }

    public String getS(){
        return s;
    }
    
    public int getA(){
        return a;
    }
    
    public double getR(){
        return r;
    }
    
    public String toString(){
        return "("+s+","+a+","+r+")";
    }
}