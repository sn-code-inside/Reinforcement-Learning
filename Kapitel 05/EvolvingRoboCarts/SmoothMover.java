import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * A variation of an actor that maintains precise location (using doubles for the co-ordinates
 * instead of ints). It also maintains a current movement in form of a movement vector.
 * 
 * This is a variation of the SmoothMover class presented ealier in the book (version 2.0).
 * This version implements wrap-around movement: when the actor moves out of the world at one
 * side, it enters it again at the opposite edge.
 * 
 * @author Poul Henriksen
 * @author Michael Kolling
 * 
 * @version 2.1
 */
public abstract class SmoothMover extends Actor
{
    private Vector movement;
    protected double exact_rotation=0;
    protected double dx_buffer;
    protected double dy_buffer;
    
    public SmoothMover()
    {
        this(new Vector());
    }
    
    /**
     * Create new thing initialised with given speed.
     */
    public SmoothMover(Vector movement)
    {
        this.movement = movement;
    }
    
    /**
     * Move in the current movement direction.
     */
    public void move() 
    {
        dx_buffer = dx_buffer+movement.getX();
        dy_buffer = dy_buffer+movement.getY();
        // Make a position a change, if dx (dy) buffer is greater then 1
        boolean change = false;
        int dx=0,dy=0;
        if (dx_buffer*dx_buffer>=1.0){
           dx=(int)dx_buffer;
           dx_buffer-=dx;
           change=true;     
        }
        if ((dy_buffer*dy_buffer>=1.0)){
           dy=(int)dy_buffer;
           dy_buffer-=dy;
           change=true;
        }    
        if (change) setLocation(getX()+dx,getY()+dy);   
    }
 
    /**
     * Moves the actor forward a specified amount.
     * 
     * @param dx X-component of the movement vector.
     * @param dy Y-component of the movement vector.
     */
    public void move(int dx, int dy)
    {
        setLocation(getX()+dx, getY()+dy);
    }
    
    private boolean inRange(double x, double lowerBound, double upperBound){
        return ((lowerBound<=x)&&(x<=upperBound));
    }
   
    /**
     * Return the exact x-coordinate (as a double).
     */
    public double getExactX() 
    {
        return this.getX()+dx_buffer;
    }

    /**
     * Return the exact y-coordinate (as a double).
     */
    public double getExactY() 
    {
        return this.getY()+dy_buffer;
    }
    
    /**
     * Return the exact rotation (as a double).
     */
    public double getExactRotation() 
    {
        return this.exact_rotation;
    }
    
    /**
     * Increase the speed with the given vector.
     */
    public void addForce(Vector force) 
    {
        movement.add(force);
    }
    
    /**
     * Accelerate the speed of this mover by the given factor. (Factors < 1 will
     * decelerate.)
     */
    public void accelerate(double factor)
    {
        movement.scale(factor);
        if (movement.getLength() < 0.15) {
            movement.setNeutral();
        }
    }
    
    /**
     * Return the speed of this actor.
     */
    public double getSpeed()
    {
        return movement.getLength();
    }
    
    /**
     * Stop movement of this actor.
     */
    public void stop()
    {
        movement.setNeutral();
    }

    /**
     * Sets the current speed.
     */
    public void setMovement(Vector v) 
    {
        this.movement=v;
    }
    
    /**
     * Sets an angle of rotation to the mover.
     * @param exact_rotation the angle
     */
    public void setRotation(double exact_rotation){
        super.setRotation((int)(exact_rotation+0.5));
        this.exact_rotation=exact_rotation;
        this.movement.setDirection(exact_rotation);
    }

    /**
     * Return the current speed.
     */
    public Vector getMovement() 
    {
        return movement.copy();
    }
    
}
