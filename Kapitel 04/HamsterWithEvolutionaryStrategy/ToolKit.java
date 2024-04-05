import java.util.Random;

/**
 * ToolKit
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
public class ToolKit  
{
    public static Random random = new Random(System.nanoTime());
    /*
     * compare value of a to b and c. If is smaller then b or greater than c, a will become b or c
     */
    public static double getValueInRange(double a, double b, double c) {
        if (a < b) {
            return b;
        } else if (a > c) {
            return c;
        }
        return a;
    }
    
    public static int round(double x){
        return (int)(x+0.5);
    }
    
     /**
     * Calculates a sigmoid value.
     * @param x argument
     * @param p a parameter that specifies the slope of the sigmoid
     */
    public static double Sigmoid(double x, double p) {
        double e_xp = (-x) / p;
        return (1 / (1 + Math.exp(e_xp)));
    }

    /**
     * Returns a normally distributed random value with the mean m and 
     * the standard deviation sd.
     * @param m mean
     * @param sd standard deviation
     * @return a normally distributed random value with the mean m and 
     *           the standard deviation sd.
     */
    public static double getNormalDistribValue(double m, double sd){
        return(m + (random.nextGaussian()*sd));
    }
    
    /**
     * Returns a sigmoidial random number from -1 to 1.
     */
    public double getRandomSigmoidValue() {      
        return (random.nextDouble() - random.nextDouble());
    }
}
