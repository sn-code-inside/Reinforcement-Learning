import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * For marking Q values of states.
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
public class QValueMarker extends Actor
{
    private final static String[][] nameImg = {{"Q_marker_blau_0.png","Q_marker_blau_1.png","Q_marker_blau_2.png","Q_marker_blau_3.png"},
                                               {"Q_marker_rot_0.png","Q_marker_rot_1.png","Q_marker_rot_2.png","Q_marker_rot_3.png"}};
    public static double maxV=0;
    public static double minV=0;
    private double v = 0;
    private int a = 0;
    GreenfootImage image = null;
    
    public QValueMarker( double v, int a )
    {
        super();
        setQ(v);
        this.a=a;
        
        int idPositive = (v>0) ? 1:0;
        image = new GreenfootImage(nameImg[idPositive][a]);
        setImage(updateAlpha(image));
    }
    
    public void setA( int a )
    {
        this.a=a;
        int idPositive = (v>0) ? 1:0;
        image = new GreenfootImage(nameImg[idPositive][a]);
        setImage(updateAlpha(image));
    }
    
    public int getA()
    {
        return a;
    }
    
    public double getQ()
    {
        return v;
    }
    
    public void setQ(double v)
    {
        int idPositive = 1; 
        if (v>0){
            idPositive=1;
            if (v>maxV) maxV=v;
        }
        if (v<0){
            idPositive=0;
            if (v<minV) minV=v;
        }
        this.v=v;
        image = new GreenfootImage(nameImg[idPositive][a]);
        setImage(updateAlpha(image));
    }
    
    private GreenfootImage updateAlpha(GreenfootImage img)
    {   
        double e = 0;
        
        if (v==0){
            e=0;
        }else{ 
            if (v>0){
                e = v/maxV;
            }else{
                e = v/minV;
            }
        }
        
        if (e>1) e=1;
        
        for(int x = 0; x < img.getWidth(); x++)
        {
            for(int y = 0; y < img.getHeight(); y++)
            {
                Color c = img.getColorAt(x, y);
                int r=c.getRed();
                int g=c.getGreen();
                int b=c.getBlue();
                if ((r>g)||(b>g)){
                    img.setColorAt(x, y, new Color(r,g,b,(int)(220*e)));
                }else{
                    if ((r==42)&&(r==g)&&(g==b)){
                        img.setColorAt(x, y, new Color(r,g,b,(int)(e*50.0+75)));
                    }
                }
                
            }
        }
        return img;
    }
}
