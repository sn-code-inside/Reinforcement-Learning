import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * A place of the TicTacToe game board.
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
public class TicTacToe_Field extends Actor
{
    protected double value = 0;
    protected char occupancy ='-';
    
    public TicTacToe_Field()
    {
        super();
        setImage("leer.png");
    } 
    
    public void setOccupancy(char c)
    {
        this.occupancy = c;
        if (c=='-'){
            this.setImage("leer.png");
        }
        else{
            this.setImage(c+".png");
        }
    }
    
    public char getOccupancy()
    {
        return occupancy;
    }
    
    public void setValue(double value)
    {
        this.value = value;
        int x = this.getX();
        int y = this.getY();
        this.getWorld().showText(""+value, x, y);
    }
    
    public double getValue()
    {
        return this.value;
    }
}
