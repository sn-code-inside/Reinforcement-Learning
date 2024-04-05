/**
 * Element of an episode (s_t,a_t,r_t+1)
 * 
 * @author Uwe Lorenz
 * @version 1.0 (23.4.2021)
 */
public class Experience  
{
    private String s;
    private int a;
    private double r;

    public Experience(String s, int a, double r)
    {
        this.s=s;
        this.a=a;
        this.r=r;
    }

    public String getS(){
        return s;
    }
    
    public int getA(){
        return a;
    }
    
    public double getR(){
        return r;
    }
    
    public String toString(){
        return "("+s+","+a+","+r+")";
    }
}