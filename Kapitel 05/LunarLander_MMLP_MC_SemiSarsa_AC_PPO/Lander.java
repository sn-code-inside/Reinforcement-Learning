import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * A lunar lander
 *
 * @author Poul Henriksen
 * @author Michael Kölling
 * 
 * @version 1.1
 */
public class Lander extends Actor
{
    /** Current speed */
    protected double speed = 0;
     
    /** Allowed speed for landing speed */
    protected double MAX_LANDING_SPEED = 25;
    
    /** Power of the rocket */
    private double thrust = -3;
    
    /** The location */
    protected double altitude;
    
    /** The speed is divided by this. */
    protected double speedFactor = 10;
    
    /** Rocket image without thrust */
    protected GreenfootImage rocket;
   
    /** Rocket image with thrust */
    protected GreenfootImage rocketWithThrust;
    
    /** Moon we are trying to land on */
    protected Moon moon;    
    
    /** Bottom of lander (offset in pixels from centre) */
    private int bottom = 15;
    
    /** Zustand des Triebwerks */
    private boolean triebwerkEin = false;
    
    /** Treibstoffvorrat */
    protected int TANKVOLUMEN = 100;
    private int tankInhalt = TANKVOLUMEN;
     
    protected static double ALTITUDE_MIN; 
    protected static double ALTITUDE_MAX; 
    
    protected static double SPEED_MIN; 
    protected static double SPEED_MAX; 
    
    public Lander()
    {
        rocket = getImage();
        rocketWithThrust = new GreenfootImage("thrust.png");
        rocketWithThrust.drawImage(rocket, 0, 0);
    }       

    public void act()
    {
        processKeys();
        sim_step();
    }
    
    protected void sim_step()
    {
        triebwerk();
        double deltaY = speed / speedFactor;
        if ((altitude+deltaY<ALTITUDE_MAX)&&(altitude+deltaY>ALTITUDE_MIN)) altitude += deltaY;
        applyGravity();
        setLocation(getX(), (int) (altitude));
    }
   
    /**
     * Lander has been added to the world.
     */
    public void addedToWorld(World world) 
    {
        moon = (Moon) world;        
        altitude = getY();
     
    }
    
    /**
     * Handle keyboard input.
     */
    private void processKeys() 
    {
        if(Greenfoot.isKeyDown("down")) 
        {
            triebwerkEin(true);
        } else {
            triebwerkEin(false);
        }
    }
    
    public void triebwerkEin(boolean an){
        this.triebwerkEin=an;
    }
    
    /**
     * Raketentriebwerk
     */
    protected void triebwerk() 
    {
        if (triebwerkEin){
            if (this.tankInhalt>0){
                incSpeed(thrust);
                setImage(rocketWithThrust);
                this.tankInhalt--; 
            }
        } else {
            setImage(rocket);
        }
    }
    
    private void incSpeed(double dv)
    {
        if ((speed+dv<SPEED_MAX)&&(speed+dv>SPEED_MIN))  speed+=dv;
    }
    
    public void addTreibstoff(double menge){
        this.tankInhalt+=menge;
        if (this.tankInhalt>=this.TANKVOLUMEN) this.tankInhalt=this.TANKVOLUMEN;
    }
    
    public double tankInhalt(){
        return this.tankInhalt;
    }
  
    /**
     * Let the gravity change the speed.
     */
    private void applyGravity() 
    {
        incSpeed(moon.getGravity());
    }
    
    /**
     * Whether we have touched the landing platform yet.
     */
    protected boolean isLanding() 
    {
        if ((getY()+bottom>moon.getHeight()) || getY()<=0) return false;
        Color colorBelow = moon.getColorAt(getX(), getY() + bottom);
        return (speed <= MAX_LANDING_SPEED) && !colorBelow.equals(moon.getSpaceColor());
    }
     
    /** 
     * Is the lander exploding?
     */
    protected boolean isExploding() 
    {
        if (getY()+bottom>moon.getHeight()){
            return true;
        }
        Color colorBelow = moon.getColorAt(getX(), getY() + bottom);
        return (speed > MAX_LANDING_SPEED) && !colorBelow.equals(moon.getSpaceColor());
    }
   
}