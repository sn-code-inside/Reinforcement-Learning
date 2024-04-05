import java.util.Random;

/**
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
public class ToolKit  
{
    /**
     * Normalize value to make it from 1 to -1
     */
    public static double Sigmoid(double a, double p) {
        double ap = (-a) / p;
        return (1 / (1 + Math.exp(ap)));
    }

    /**
     * Random number from -1 to 1;
     */
    public static double RandomSigmoid() {
        Random ran = new Random(System.nanoTime());
        double r = ran.nextDouble() - ran.nextDouble();
        return r;
    }
    
    /**
     * Compare value of a to b and c. If is smaller then b or greater than c, a will become b or c
     */
    public static double getValueInRange(double a, double b, double c) {
        if (a < b) {
            return b;
        } else if (a > c) {
            return c;
        }
        return a;
    }
    
    /** 
     * Rounds a double and makes it int.
     */
    public static int round(double x){
        return (int)(x+0.5);
    }
}
