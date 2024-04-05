import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;

/**
 * Cursor for selecting robots to watch their state.
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
public class Cursor extends SmoothMover
{
    AdaptiveMbRobot connectedRobot = null;
    
    public static final double CURSOR_SPEED = 2.0;
    
    public Cursor(){
        GreenfootImage img = new GreenfootImage("cursor40x40.png");
        img.scale(20, 20);
        setImage(img);
    }
     
    public AdaptiveMbRobot getRobot(){
        return this.connectedRobot;
    }
    
    public void setRobot(AdaptiveMbRobot robot){
        if (this.connectedRobot!=null) {
            connectedRobot.setCursor(null); // clear cursor at previous robot
        }
        this.connectedRobot = robot; // connect cursor with robot
    }
    
    public AdaptiveMbRobot getIntersectingRobot(){
        return (AdaptiveMbRobot)(getOneIntersectingObject(AdaptiveMbRobot.class));
    }  
}