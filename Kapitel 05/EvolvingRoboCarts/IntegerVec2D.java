import java.text.NumberFormat;

/**
 * Helps to process 2D integer vectors.
 */
public class IntegerVec2D  
{
    public int x;
    public int y;
    private static final NumberFormat nf = NumberFormat.getInstance(new java.util.Locale("en", "US"));
  
    /**
     * Constructor for objects of class IntegerPoint2D
     */
    public IntegerVec2D(int x, int y)
    {
        this.x=x;
        this.y=y;
        nf.setMaximumFractionDigits(3);
    }
    
    public double length(){
        return Math.sqrt(x*x+y*y);
    }
    
    public int sqrLength(){
        return x*x+y*y;
    }
    
    public String toString(){
        return "["+nf.format(length())+"("+x+";"+y+")]";
    }
}
