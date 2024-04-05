import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.HashSet;

/**
 * Abstract class of "target" objects where the bots can get "rewards".
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
public abstract class Targets extends Actor
{
    HashSet eatenBy = new HashSet();
    
    public boolean take(long num){
        if (eatenBy.contains(num)) return false;
        eatenBy.add(num);
        return true;
    }
    
    public void clearEatenBySet(){
        eatenBy.clear();
    } 
    
    public abstract double getValue();
}
