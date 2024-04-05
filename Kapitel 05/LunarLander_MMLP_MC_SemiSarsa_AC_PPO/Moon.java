import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * A moon.
 * 
 * @author Poul Henriksen
 * @author Michael Kölling
 * 
 * @version 1.1
 */
public class Moon extends World
{
    /** Gravity on the moon */
    private double gravity = 0.9;
    
    /** Color of the landing platform */
    private Color landingColor = Color.WHITE;
    
    /** Color of the space */
    protected Color spaceColor = Color.BLACK;
    
    protected int landerStartX = 326;
    protected int landerStartY = 100;
    
    public Moon() 
    {
        super(600,600,1);
        Explosion.initialiseImages();
    }
    
    /** 
     * Gravity on the moon  
     *
     */
    public double getGravity()  {
        return gravity;
    }
    
    
    /**
     * Color of the landing platform 
     * 
     */
    public Color getLandingColor() {
        return landingColor;
    }    
    
    
    /**
     * Color of the space 
     * 
     */
    public Color getSpaceColor() {
        return spaceColor;
    }
 
}