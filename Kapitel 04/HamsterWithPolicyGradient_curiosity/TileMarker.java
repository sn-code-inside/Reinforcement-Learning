import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Displays the number of visits of a state.
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
public class TileMarker extends Actor
{
    public static int c_max = 100;
    private final int transparency_max = 185;
    
    private int c;
    GreenfootImage image = new GreenfootImage("square_blue.png");
    
    public TileMarker( int c )
    {
        super();
        this.c=c;
        if (c>c_max) c = c_max;
        image.setTransparency(transparency_max-(c*transparency_max)/c_max);
        setImage(image);
    }
    
    public void setC( int c )
    {
        this.c=c;
        if (c>c_max) this.c=c_max;
        image.setTransparency((c*transparency_max)/c_max);
        setImage(image);
    }
    
    public int getC()
    {
        return c;
    }
}
