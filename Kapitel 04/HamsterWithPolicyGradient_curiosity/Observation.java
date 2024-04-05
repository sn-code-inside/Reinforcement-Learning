/**
 * Observation of a transition result (s,r)
 * 
 * @author Uwe Lorenz
 * @version 1.0 (23.4.2021)
 */
public class Observation  
{
    private String s;
    private double r;

    public Observation(String s, double r)
    {
        this.s=s;
        this.r=r;
    }

    public String getS(){
        return s;
    }
       
    public double getR(){
        return r;
    }
    
    public void setR(double r){
        this.r=r;
    }
    
    public String toString(){
        return "("+s+","+r+")";
    }
}
