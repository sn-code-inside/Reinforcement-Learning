import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.text.NumberFormat;

/**
 * Abstract class for robot components that receive motor outputs (see the kinematics method).
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
public abstract class Motors extends Actor
{
    public final static NumberFormat motor_nf = NumberFormat.getInstance(); 
    
    protected String[] motornames;  // identifers
    protected double[] m = null;    // values
    
    protected SmoothMover actor = null;
    
    public Motors(){
        super();
        motor_nf.setMaximumFractionDigits(2);
        setImage(new GreenfootImage(1,1));
    }
    
    public void connectWith(SmoothMover actor){
        this.actor = actor;
    }
    
    /**
     * Gives the number of the registered robot motors.
     */
    public int getNumber(){
        return motornames.length;
    }
    
    public String[] getMotorNames(){
        return motornames;
    }
    
    /**
     * Transforms motor values to movement vector.
     * @param m values for the robot's motors.
     */
    abstract protected Vector kinematics(double[] m);
}
