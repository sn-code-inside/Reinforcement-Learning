import greenfoot.Greenfoot;
import java.text.*;
/**
 * A 2D vector.
 * 
 * @author Poul Henriksen
 * @author Michael Kolling
 * 
 * @version 2.0
 */
public final class Vector
{
    public static final NumberFormat nf = NumberFormat.getInstance();
    
    double dx;
    double dy;
    double direction;
    double length;
    
    /**
     * Create a new, neutral vector.
     */
    public Vector()
    {
        nf.setMaximumFractionDigits(2);
    }

    /**
     * Create a vector with given direction and length. The direction should be in
     * the range [0..359], where 0 is EAST, and degrees increase clockwise.
     */
    public Vector(int direction, double length)
    {
        this.length = length;
        this.direction = (double)direction;
        updateCartesian();   
        nf.setMaximumFractionDigits(2); // for String output
    }

    /**
     * Create a vector by specifying the x and y offsets from start to end points.
     */
    public Vector(double dx, double dy)
    {
        this.dx = dx;
        this.dy = dy;
        updatePolar();
    }
    
    /**
     * Create a vector with given direction and length or by 
     * specifying the x and y offsets from start to end points.
     * In the case of polar coordinates, the direction should be 
     * in the range [0..360[ , where 0 is EAST, and degrees increase
     * clockwise.
     */
    public Vector(double x0, double x1, boolean polar)
    {
        if (polar) {
            this.direction = x0;
            this.length = x1;
            updateCartesian();  
        }else{
            this.dx = dx;
            this.dy = dy;
            updatePolar();
        }
    }

    /**
     * Set the direction of this vector, leaving the length intact.
     */
    public void setDirection(double direction) 
    {
        this.direction = direction;
        updateCartesian();
    }
    
    /**
    * Set the direction of this vector, leaving the length intact.
    */
    public void addDirection(int direction) 
    {
        this.direction += direction;
        updateCartesian();
    }
   
    /**
     * Add another vector to this vector.
     */
    public void add(Vector other) 
    {
        dx += other.dx;
        dy += other.dy;
        updatePolar();
    }
    
    /**
     * Set the length of this vector, leaving the direction intact.
     */
    public void setLength(double length) 
    {
        this.length = length;
        updateCartesian();
    }
    
    /**
     * Scale this vector up (factor > 1) or down (factor < 1). The direction
     * remains unchanged.
     */
    public void scale(double factor) 
    {
        length = length * factor;
        updateCartesian();
    }
    
    /**
     * Set this vector to the neutral vector (length 0).
     */
    public void setNeutral() {
        dx = 0.0;
        dy = 0.0;
        length = 0.0;
        direction = 0;
    }
    
    /**
     * Revert to horizontal component of this movement vector.
     */
    public void revertHorizontal() {
        dx = -dx;
        updatePolar();
    }
    
    /**
     * Revert to vertical component of this movement vector.
     */
    public void revertVertical() {
        dy = -dy;
        updatePolar();
    }
    
    /**
     * Return the x offset of this vector (start to end point).
     */
    public double getX() {
        return dx;
    }
     
    /**
     * Return the y offset of this vector (start to end point).
     */
    public double getY() {
        return  dy;
    }
    
    /**
     * Return the direction of this vector (in degrees). 0 is EAST.
     */
    public double getDirection() {
        return direction;
    }
    
    /**
     * Return the length of this vector.
     */
    public double getLength() {
        return length;
    }

    /**
     * Update the direction and length fom the current dx, dy.
     */
    private void updatePolar() 
    {
        this.direction = Math.toDegrees(Math.atan2(dy, dx));
        this.length = Math.sqrt(dx*dx+dy*dy);
    }   
    
    /**
     * Update dx and dy from the current direction and length.
     */
    private void updateCartesian() 
    {
        dx = length * Math.cos(Math.toRadians(direction));
        dy = length * Math.sin(Math.toRadians(direction));   
    }
    
    /**
     * Return a copy of this vector.
     */
    public Vector copy() 
    {
        Vector copy = new Vector();
        copy.dx = dx;
        copy.dy = dy;
        copy.direction = direction;
        copy.length = length;
        return copy;
    } 
    
    public String toString()
    {
        return "["+nf.format(dx)+","+nf.format(dy)+"]=["+direction+"°,"+nf.format(length)+"]";
    }
    
}