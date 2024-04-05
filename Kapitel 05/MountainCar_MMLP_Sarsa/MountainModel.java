import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)
//import greenfoot.Color;

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
public class MountainModel extends World
{
    public final static double leftBorder = -1.2;
    public final static double rightBorder = 0.5;
    
    public final static int abstandObenUnten = 100;
    public final static int abstandLinksRechts = 50;
    
    //protected MountainCar agent = new MountainCar();
    
    public MountainModel() 
    {
        super(800,600, 1);
        setBackground("MountainCar_Berg.jpg");
    }
   
    /**
     * Liefert die Höhe in Abhängigkeit von x.
     */
    public static double getYPos(double x){
        return Math.sin(3*x);
    }
    
    /**
     * Liefert das Gefälle des Bergs in Abhängigkeit von x.
     */
    public static double gefaelle(double x){
        return -Math.cos(3*x);
    }
    
    /**
     * Transformiert die Koordinaten aus dem Modell in Bildschirmkoordinaten.
     */
    public static int[] transformKoord(double x, double y, World w){
        int[] punkt2D = new int[2];
        punkt2D[0] = (int)(((x-leftBorder)/(rightBorder-leftBorder))*(w.getWidth()-2*abstandLinksRechts)+abstandLinksRechts);
        punkt2D[1] = w.getHeight()-(int)((y+1)/2*(w.getHeight()-2*abstandObenUnten)+abstandObenUnten);
        return punkt2D;
    }
    
    /**
     * Aktualisiert die Anzeige des Autos.
     */
    public void aktualisiereAnzeige(MountainCar auto){
        double x = auto.getXPos();
        auto.setRotation((int)(1.2*(Math.atan(MountainModel.gefaelle(x))*180)/3.14));
        int[] screenPos = MountainModel.transformKoord(x,MountainModel.getYPos(x),this);
        auto.setLocation(screenPos[0],screenPos[1]-auto.getImage().getHeight()/4);
    }
    
    /* Zeichnet die Kontur der Mountain-Car "Berglandschaft".
    private void drawMountain(){
        int[] screenXY = new int[2];
        int sx_alt=-1;
        int sy_alt=-1;
        for (double x=leftBorder;x<=rightBorder;x+=0.05){
            double y = getYPos(x);
            screenXY = transformKoord(x,y,this);
            
            GreenfootImage img = this.getBackground();
            img.setColor(Color.BLACK);
            if (sx_alt>=0) img.drawLine(sx_alt, sy_alt, screenXY[0], screenXY[1]);
            
            sx_alt=screenXY[0];
            sy_alt=screenXY[1];
        }
    }
    */
}