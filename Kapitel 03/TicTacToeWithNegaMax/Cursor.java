import greenfoot.*;

/**
 * Creates the cursor.
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
public class Cursor extends Actor
{
    public Cursor()
    {
        GreenfootImage image = new GreenfootImage("auswahl.png");
        setImage(makeWhiteTransparent(image));
    }
    
    private GreenfootImage makeWhiteTransparent(GreenfootImage img)
    {   
        Color transparent = new Color(0, 0, 0, 0);
        for(int x = 0; x < img.getWidth(); x++)
        {
            for(int y = 0; y < img.getHeight(); y++)
            {
                if( img.getColorAt(x, y).equals(Color.WHITE) )
                img.setColorAt(x, y, transparent);
            }
        }
        return img;
    }
}
