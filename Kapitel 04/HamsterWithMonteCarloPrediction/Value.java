import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * Marks the value of a state V(s).
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
    private double value=0.0;
    private String sValue = "";
    
    private Color fontColor = new Color(70,70,255,255);
    private int fontSize = 12;
    
    public Value()
    {
        super();
        isTerminal=false;
        if (isOutputOn) {
            String s = formatter.format(value);
            setImage(produceImage(""+s));
        }else{
            setImage(new GreenfootImage("",0, fontColor,new Color(0, 0, 0, 0)));
        }
    }
    
    public Value(boolean isTerminal)
    {
        super();
        this.isTerminal = isTerminal;
        if (isOutputOn) { 
            setImage(produceImage(""+formatter.format(value)));
        }else{
            setImage(new GreenfootImage("",0, fontColor,new Color(0, 0, 0, 0)));
        }
    } 
       
    public void update(double value_new)
    {
        if (this.value == value_new) return;
        this.value = value_new;
        String sValue_new = formatter.format(value);
        if (sValue_new.length()>5) sValue_new=sValue_new.substring(0,5);
        if (!sValue.equals(sValue_new)){
            sValue=sValue_new;
            if (isOutputOn) { 
                setImage(produceImage(sValue));
            }
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
            setImage(new GreenfootImage("",0, fontColor,new Color(0, 0, 0, 0)));
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
        return new GreenfootImage(s, fontSize, fontColor, new Color(0, 0, 0, 0));
    }
}
