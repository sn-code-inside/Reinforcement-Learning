import greenfoot.*;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;

/**
 * Abstract class for robot components that supply sensory input.
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
public abstract class Sensors extends Actor
{
    public final static NumberFormat sensor_nf = NumberFormat.getInstance(); 
    
    public String[] sensornames; 
    
    protected SmoothMover actor = null;
    
    public Sensors(){
        super();
        sensor_nf.setMaximumFractionDigits(2);
        setImage(new GreenfootImage(1,1));
    }
    
    public void connectWith(SmoothMover actor){
        this.actor = actor;
    }
    
    /**
     * Gives the number of the registered robot sensors.
     */
    public int getNumber(){
        return sensornames.length;
    }
    
    public String[] getSensorNames(){
        return sensornames;
    }
    
    abstract public void produceSensors();
    
    abstract public IntegerVec2D[] detectCollisions();
    
    abstract public IntegerVec2D[] detectDistances();
    
    /**
     * Checks if in the given bitmap a specific color (or color change) is present in the given line between points p0 and p1 (uses the Bresenham algorithm).
     * @param img background image.
     * @param p0
     * @param p1
     * @param RGB
     * @param detectChange
     */
    public static IntegerVec2D sensorLine(BufferedImage img, IntegerVec2D p0, IntegerVec2D p1, int RGB, boolean detectChange){
        int x0 = p0.x;
        int y0 = p0.y;
        int x1 = p1.x;
        int y1 = p1.y;        
        int dx =  abs(x1-x0), sx = x0<x1 ? 1 : -1;
        int dy = -abs(y1-y0), sy = y0<y1 ? 1 : -1;
        int err = dx+dy, e2;
        int counter=0;
        try{
            while (true) {
                int rgbFromPic = img.getRGB(x0,y0);
                
                if (detectChange){
                    if (RGB!=rgbFromPic) return new IntegerVec2D(x0-p0.x,y0-p0.y);  
                }else{
                    if (RGB==rgbFromPic) return new IntegerVec2D(x0-p0.x,y0-p0.y); 
                }
                 
                if (x0==x1 && y0==y1) break;
                
                e2 = 2*err;
                if (e2 > dy) { err += dy; x0 += sx; } 
                if (e2 < dx) { err += dx; y0 += sy; } 
                counter++;
            }
        }catch(Exception e){}
        return null;
    }
    
    protected static int round(double x){
        return (int)(x+0.5);
    }
    
    protected static int abs(int x){
        if (x<0)
            return -x;
        else
            return x;
    }
}
