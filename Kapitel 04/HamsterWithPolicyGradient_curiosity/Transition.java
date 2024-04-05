/**
 * Helper structure for the transition model. Represents the probability for a certain subsequent state.
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
public class Transition  
{
    public int nachbarX = 0;
    public int nachbarY = 0;
    public double p = 0.0;
    
    public Transition( int nachbarX, int nachbarY, double p)
    {
        this.nachbarX = nachbarX;
        this.nachbarY = nachbarY;
        this.p = p;
    }
}