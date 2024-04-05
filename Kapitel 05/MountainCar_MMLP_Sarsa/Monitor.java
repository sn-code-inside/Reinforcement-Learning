import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

import greenfoot.Color;
import java.awt.Graphics;

/**
 * Monitor that displays a text and number.
 * 
 * @author Michael Kolling
 * @version 1.0.1
 */
public class Monitor extends Actor
{
    private static final Color textColor = new Color(255, 255, 255);
    private String text;
    
    public Monitor()
    {
        this("");
    }

    public Monitor(String text)
    {
        this.text = text;
        int laenge = (text.length() + 10) * 10;

        setImage(new GreenfootImage(laenge, 16));
        GreenfootImage image = getImage();
        image.setColor(textColor);
        updateImage();
    }
    
    public void setText(String text)
    {
        this.text = text;
        updateImage();
    }
    
    private void updateImage()
    {
        GreenfootImage image = getImage();
        image.clear();
        image.drawString(text, 1, 12);
    }
}
