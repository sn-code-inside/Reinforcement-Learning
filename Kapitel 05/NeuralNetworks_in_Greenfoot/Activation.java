import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)

/**
 * Help class for displaying values.
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
public class Activation extends Actor
{
    protected double bewertung = 0;
    
    public Activation(double v)
    {
        super();
        setImage("eingabefeld_schwarz.jpg");
        setBewertung(v);
    }
    
    public void setBewertung(double bewertung)
    {
        if ((bewertung<0)||(bewertung>1.0)){
            System.out.println("Wert "+bewertung+" ist außerhalb des Definitionsbereichs von 0 bis 1.");
            return;
        }
        this.bewertung = bewertung;
        
        int transparency = (int)(255*bewertung);
        GreenfootImage nImg = this.getImage();
        nImg.setTransparency(transparency);
        setImage(nImg);
    }
    
    public double getBewertung()
    {
        return this.bewertung;
    }
}