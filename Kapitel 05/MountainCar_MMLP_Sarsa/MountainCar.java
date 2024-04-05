import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.Random;

/**
 * Java implementation of the "mountain landscape" from the Mountain Car environment by Sutton, Barto.
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
public class MountainCar extends Actor
{
    protected final static double V_min = -0.07;
    protected final static double V_max = 0.07;
    protected final static int MOTOR_default = 1;
    
    protected double x_position = 0;
    protected double geschwindigkeit = 0;
    protected int motoraktion = MOTOR_default; // default
    
    private GreenfootImage imgAuto = new GreenfootImage("car-4.png");    
    protected MountainModel berg = null;
    
    private Random zufall = new Random(); 
    
    public MountainCar(){
        super();
        this.setImage(imgAuto);
    }
    
    public void addedToWorld(World world) 
    {
       berg = (MountainModel) world; 
       setToStart();
       berg.aktualisiereAnzeige(this);
    }
    
    public void act(){
        processKeys();
        sim_step();
    }

    public void setToStart()
    {
       x_position = (0.2*zufall.nextDouble())-0.6; 
       geschwindigkeit = 0;
       motoraktion = MOTOR_default;
    }
    
    /**
     * Liefert die x-Position des Autos.
     */
    public double getXPos(){
        return x_position;
    }
    
    /** 
     * Errechnet den Folgezustand des Agenten, dabei wird die ausgewählte Aktion und die Dynamik der Umwelt 
     * berücksichtigt .
     */
    protected void sim_step()
    {
       geschwindigkeit = geschwindigkeit+motor()+0.0025*MountainModel.gefaelle(x_position);
       geschwindigkeit = bound(geschwindigkeit,V_min,V_max);
       x_position = x_position+geschwindigkeit;
       x_position = bound(x_position,MountainModel.leftBorder,MountainModel.rightBorder);
       motoraktion = MOTOR_default;
       berg.aktualisiereAnzeige(this);
    }
    
    /**
     * Sets an action ("decision").
     */
    protected void setAction(int a){
        this.motoraktion=a;
    }
    
    /**
     * Liefert die Beschleunigung durch den Motor in Abhängigkeit von der eingestellten 
     * Motoraktion.
     */
    protected double motor()
    {
        return 0.001*((double)motoraktion-1); // 0,1,2 -> -1,0,1
    }
    
    /**
     * Begrenzt einen double Wert.
     */
    public static double bound(double val, double val_min, double val_max ){
        if (val<val_min) return val_min;
        if (val>val_max) return val_max;
        return val;
    }
    
    /**
     * Handle keyboard input.
     */
    protected void processKeys() 
    {
        if(Greenfoot.isKeyDown("left")) 
        {
            motoraktion=0;
        } else if (Greenfoot.isKeyDown("right")) {
            motoraktion=2;;
        }
    }
}