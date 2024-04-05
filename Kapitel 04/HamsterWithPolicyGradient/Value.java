import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * For marking values of states. V(s)
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
public class Value extends Actor
{
    private boolean qOutput = false;
    private boolean isOutputOn = true;
    private boolean isTerminal = false;
    
    final java.text.DecimalFormat formatter = new java.text.DecimalFormat( "##0.0##" );
    private double value=0;
    
    Color schriftfarbe = new Color(70,70,255,255);
    
    public Value()
    {
        super();
        isTerminal=false;
        if (isOutputOn) { 
            setImage(produceImage(""+formatter.format(value)));
        }else{
            setImage(new GreenfootImage("",10, schriftfarbe,new Color(0, 0, 0, 0)));
        }
    }
    
    public Value(boolean isTerminal)
    {
        super();
        this.isTerminal = isTerminal;
        if (isOutputOn) { 
            setImage(produceImage(""+formatter.format(value)));
        }else{
            setImage(new GreenfootImage("",10, schriftfarbe,new Color(0, 0, 0, 0)));
        }
    } 
       
    public void update(double value_new)
    {
        this.value = value_new;
        if (isOutputOn) { 
            String s = formatter.format(value);
            if (s.length()>5) s=s.substring(0,5);
            setImage(produceImage(s));
        }
    }
    
    public boolean isOutputOn()
    {
        return isOutputOn;
    }
    
    public void setOutputOn(boolean outputOn)
    {
        this.isOutputOn=outputOn;
        if (!outputOn){
            setImage(new GreenfootImage("",10, schriftfarbe,new Color(0, 0, 0, 0)));
        }
    }
   
    public void setTerminal(boolean isTerminal)
    {
        this.isTerminal=isTerminal;
    }
    
    public void setValue(double value)
    {
        this.value = value;
    }
    
    GreenfootImage produceImage(String s)
    {
        if (isTerminal) s="T";
        return new GreenfootImage(s, 10, schriftfarbe, new Color(0, 0, 0, 0));
    }
}
