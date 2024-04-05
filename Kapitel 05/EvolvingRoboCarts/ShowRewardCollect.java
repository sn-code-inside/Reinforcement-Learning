import greenfoot.*;
import java.awt.Color;
import java.text.*;

/**
 * Displays collected reward.
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
 *
 * The sounds are from
 * "SOUND JAY - free sound effects"
 * https://www.soundjay.com/tos.html
 */
public class ShowRewardCollect extends Actor
{
    private static final greenfoot.Color positiveTextColor = greenfoot.Color.YELLOW;
    private static final greenfoot.Color negativeTextColor = greenfoot.Color.BLUE;
    private GreenfootSound sound = null;
    private static final int upMoveSpeed = 1;
    private static final int max_move = 120;
    private static final NumberFormat nf = NumberFormat.getInstance();
    
    int cnt_move = 0;
    
    /**
     * Create a new Plus1 reading.
     */
    public ShowRewardCollect(double reward)
    {
        nf.setMaximumFractionDigits(1);
        setImage(new GreenfootImage(60, 20));
        GreenfootImage image = getImage();
        String out =nf.format(reward);
        if (reward>0) out="+"+out;
        updateImage(out);
        cnt_move=max_move;
        if (reward>0){
            sound = new GreenfootSound("coin-drop-1.wav");
            sound.setVolume(50); 
            sound.play();
        }
        if (reward<0){
            sound = new GreenfootSound("fail-buzzer-02.mp3");
            sound.setVolume(30);    
            sound.play();
        }
    }

    /**
     * Calculate the Plus1 based on the mode, and
     * update the reading.
     */
    public void act()
    {
        if (cnt_move>0){
            cnt_move--;
            this.setLocation(getX(), getY()-upMoveSpeed);
        }else{
            getWorld().removeObject(this);
        }
    }
    
    /**
     * Draw the image.<p>
     * Sven van Nigtevecht and Uwe Lorenz slightly changed this,
     * not so special though.
     * @param value the value to display
     */
    private final void updateImage(String value)
    {
        getImage().clear();
        greenfoot.Color textColor = positiveTextColor;
        if (value.charAt(0)=='-') textColor=negativeTextColor;
        GreenfootImage txt = new GreenfootImage(value, 18,textColor , null);
        getImage().drawImage(txt, 1, 1);
    }
}
