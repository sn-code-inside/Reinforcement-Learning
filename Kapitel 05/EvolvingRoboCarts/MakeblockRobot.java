import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.Arrays;

/**
 * A robot with differential drive that looks like a Makeblock-robot.
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
public class MakeblockRobot extends SmoothMover
{
    private GreenfootImage imgMakeblockRobot= new GreenfootImage("makeblock_robot_dif_drive_40x40.png");
    
    public static int MAX_BATTERY_CHARGE = 3000;
    protected int battery_charge = MAX_BATTERY_CHARGE;

    protected Sensors sensors = null;
    protected double[] s = null;
    
    protected Motors motors = null;
    protected double[] a = null;
    
    protected int cnt_steps = 0;

    // -----
    private IntegerVec2D[] collisions = null; // Needed to detect if the robot has hit the edge. Does not belong to 
                                              // the sensor equipment here. 
    protected boolean crashed = false;
    
    protected Cursor connectedCursor = null;
    
    // fun stuff
    protected static final int gunReloadTime = 5; // The minimum delay between firing the gun.
    protected int reloadDelayCount;               // How long ago we fired the gun the last time.
    protected int explosion_timeout=0;

    public MakeblockRobot(Sensors sensors_mb, Motors motors_mb){
        super();
        this.setImage(imgMakeblockRobot);
        
        sensors = sensors_mb;
        sensors.connectWith(this);
        s = new double[sensors.getNumber()];
        
        motors = motors_mb;
        motors.connectWith(this);
        a = new double[motors.getNumber()];
        
        reloadDelayCount = 5;
    }
    
    @Override
    public void move(){
        if (battery_charge>0){
            super.move();
            battery_charge--;
        }  
    }
    
    /**
     * Set actions to the agent.
     * @param a array that decribes actions
     */
    public void setAction(double[] a){
        int right_wheel_sensor_nr = 5;
        int left_wheel_sensor_nr = 1;
        
        // Braking when wheel on grass.
        double[] m = new double[2];
        this.collisions = sensors.detectCollisions();
        int num_colls_front = collisionsInFront();
        int num_colls_rear = collisionsRear();
        boolean isCollisionInFront = (num_colls_front>0)&&(num_colls_front>=num_colls_rear);
        boolean isCollisionRear = (num_colls_rear>0)&&(num_colls_front<=num_colls_rear);
        
        m[1]= a[1];
        if (collisions[right_wheel_sensor_nr]!=null){
            if ((a[1]>0) && (isCollisionInFront)){
                m[0]=a[0]/2;
                m[1]=0;
            }else if ((a[1]<0) && (isCollisionRear)){
                m[0]=a[0]/2;
                m[1]=0;
            }
            
        }
        
        m[0]= a[0];
        if (collisions[left_wheel_sensor_nr]!=null){
            if ((a[0]>0) && (isCollisionInFront)){
                m[1]=a[1]/2;
                m[0]=0;
            }else if ((a[0]<0) && (isCollisionRear)){
                m[1]=a[1]/2;
                m[0]=0;
            }
        }
        
        if (isCollisionInFront&&isCollisionRear){
            m[0]=0;
            m[1]=0;
            explosion_timeout++;
            if (explosion_timeout==20){
                setCrashed(true);
                getWorld().addObject( new Explosion(), getX(), getY() );
            }
        }else{
            explosion_timeout = 0;
        }
        
        Vector v = motors.kinematics(m);
        setMovement(v);
        setRotation(v.getDirection());
    }
    
    public void setCrashed( boolean crashed ){
        this.crashed=crashed;
    }
    
    public boolean isCrashed(){
        return this.crashed;
    }
    
    public int getBatteryCharge(){
        return this.battery_charge;
    }
    
    /**
     * Tests if the robot is off road at the front.
     * @return number of active collision sensors in front of the robot.
     */
    private int collisionsInFront(){
        int num = 0;
        if (collisions[6]!=null) num++;
        if (collisions[7]!=null) num++;
        if (collisions[0]!=null) num++;
        return num;
    }
    
    /**
     * Tests if the robot is off road rear.
     * @return number of active collision sensors rear.
     */ 
    private int collisionsRear(){
        int num = 0;
        if (collisions[2]!=null) num++;
        if (collisions[3]!=null) num++;
        if (collisions[4]!=null) num++;
        return num;
    }
    
    /**
     * Gets the number of off-road "collisions".
     */
    public int collisionsTotal(){
        int num = 0;
        for (IntegerVec2D dist : collisions){
            if (collisions!=null) num++;
        }
        return num;
    }
   
    /**
     * Get the agent's sensoric state.
     */
    public double[] getState(){
        IntegerVec2D[] distances = sensors.detectDistances();
        double[] vals = new double[distances.length];
        for (int i=0;i<distances.length;i++){
            if (distances[i]!=null){
                vals[i]=distances[i].length();
            }else{
                vals[i]=((MbRobotSensors)sensors).getRangeDistanceSensors();
            }
        }
        return vals;
    }
    
    public Sensors getSensors(){
        return sensors;
    }
    
    public Motors getMotors(){
        return motors;
    }
    
    public void drawDistanceSensorBeams(boolean on){
        ((MbRobotSensors)getSensors()).setDrawDistanceSensorBeams(on);        
    }
    
    public void drawCollisionSensors(boolean on){
        ((MbRobotSensors)getSensors()).setDrawCollisionSensors(on);    
    }
    
    // just for fun
    /**
     * Fire a bullet if the gun is ready.
     */
    protected void fire() 
    {
        if (reloadDelayCount >= gunReloadTime){
            Bullet bullet = new Bullet (getMovement().copy(), getRotation());
            getWorld().addObject (bullet, getX(), getY());
            bullet.move();
            reloadDelayCount = 0;
        }
    }
}
