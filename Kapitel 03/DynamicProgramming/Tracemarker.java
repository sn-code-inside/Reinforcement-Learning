import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Marks a 'footprint' on an eligibility trace.
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
public class Tracemarker extends Actor
{
    private final static String[] nameImg = {"tracemarker_o1.png","tracemarker_r1.png","tracemarker_u1.png","tracemarker_l1.png"};
    double e = 0;
    int a = 0;
    GreenfootImage image = null;
    
    public Tracemarker( double e, int a )
    {
        super();
        this.e=e;
        this.a=a;
        image = new GreenfootImage(nameImg[a]);
        setImage(updateAlphaKanal(image));
    }
    
    public void setA( int a )
    {
        this.a=a;
        image = new GreenfootImage(nameImg[a]);
        setImage(updateAlphaKanal(image));
    }
    
    public int getA()
    {
        return a;
    }
    
    public double getE()
    {
        return e;
    }
    
    public void setE(double e)
    {
        this.e=e;
        setImage(updateAlphaKanal(image));
    }
    
    private GreenfootImage updateAlphaKanal(GreenfootImage img)
    {   
        for(int x = 0; x < img.getWidth(); x++)
        {
            for(int y = 0; y < img.getHeight(); y++)
            {
                Color c = img.getColorAt(x, y);
                if (c.getAlpha()>0){
                    img.setColorAt(x, y, new Color(c.getRed(),c.getGreen(),c.getBlue(),(int)(255*e)));
                }
            }
        }
        return img;
    }
}
