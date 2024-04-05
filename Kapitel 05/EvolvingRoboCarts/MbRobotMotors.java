import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Class for simulated Makeblock(TM) robot components that receive motor outputs (see the kinematics method).
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
public class MbRobotMotors extends Motors
{
           
    public MbRobotMotors(){
        motornames = new String[]{"left wheel motor","right wheel motor"};
    }
    
    /**
     * Differential drive kinematics. Transforms motor values to movement vector.
     * @param m values for the robot's wheel motors.
     */
    protected Vector kinematics(double[] m)
    { 
        this.m = m;
        double current_direction = actor.getExactRotation();
        Vector v1 = new Vector(current_direction,1);
        Vector v2 = v1.copy();
        v1.setLength(m[0]);
        v2.setLength(m[1]);
        double deltaDir = 4.0*(v2.getLength()-v1.getLength());
        double new_direction = current_direction+deltaDir;
        v1.add(v2);
        v1.scale(0.5);
        v1.setDirection(new_direction);
        return v1;
    }
      
    public String toString(){
        return motornames[0]+" "+motor_nf.format(m[0])+" "+motornames[1]+" "+motor_nf.format(m[1]);
    }
}
