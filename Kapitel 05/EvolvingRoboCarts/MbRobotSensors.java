import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.awt.image.BufferedImage;

/**
 * Definition of the MbRobotSensors.
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
public class MbRobotSensors extends Sensors
{ 
    protected boolean drawDistanceSensorBeams = false;
    protected boolean drawCollisionSensors = false;
    
    /* This discribes the corner points of the collision detectors and between
     * which points a collision will be created ( see produceSensors() 
     * and detectCollisions() ). */
    protected IntegerVec2D[] sp_col_points = null;
    protected int[][] col_sensors_from_to = null;
    protected IntegerVec2D[] collisions = null;
    
    /* This discribes the location (point) of the distance sensor and the direction (angle)
     * in which it points. 
     */
    protected static final int range_distance_sensor = 400;
    protected IntegerVec2D[] sp_dist_points = null;
    protected int[][] dist_sensors_direction = null;
    protected IntegerVec2D[] distances = null;
    
    public MbRobotSensors()
    {
        super();
        produceSensors();
    }
    
    public void setDrawDistanceSensorBeams(boolean on){
        this.drawDistanceSensorBeams = on;
    }
    
    public void setDrawCollisionSensors(boolean on){
        this.drawCollisionSensors = on;
    }
    
    @Override
    public void produceSensors()
    { 
        sensornames = new String[]{"left front distance sensor","right front distance sensor side"};
        
        sp_col_points = new IntegerVec2D[12]; // "corner points" for "laser" beams between them (like a photoelectric sensor) 
        sp_col_points[0] = new IntegerVec2D(14,8);  
        sp_col_points[1] = new IntegerVec2D(5,9);  
        sp_col_points[2] = new IntegerVec2D(1,10);  
        sp_col_points[3] = new IntegerVec2D(-7,10); 
        sp_col_points[4] = new IntegerVec2D(-9,8); 
        sp_col_points[5] = new IntegerVec2D(-15,8); 
        sp_col_points[6] = new IntegerVec2D(-15,-8);  
        sp_col_points[7] = new IntegerVec2D(-8,-8);  
        sp_col_points[8] = new IntegerVec2D(-7,-10); 
        sp_col_points[9] = new IntegerVec2D(1,-10); 
        sp_col_points[10] = new IntegerVec2D(5,-9); 
        sp_col_points[11] = new IntegerVec2D(14,-8); 
        
        // points with sensor "laser" beams between them
        col_sensors_from_to = new int[][] {{ 0, 1},
                                           { 2, 3}, // wheel right [1]
                                           { 4, 5},
                                           { 5, 6},
                                           { 6, 7},
                                           { 8, 9}, // wheel left [5]
                                           {10,11},
                                           {11, 0}};
        
        // like ultrasonic distance sensors
        sp_dist_points = new IntegerVec2D[sensornames.length]; // number of distance sensors                                 
        sp_dist_points[0] = new IntegerVec2D(14,4); 
        sp_dist_points[1] = new IntegerVec2D(14,-4); 
        dist_sensors_direction = new int[][]{{ range_distance_sensor, 0},  // { sensor dir_x, dir_y},
                                             { range_distance_sensor, 0}};
    }
    
    /** 
     * Utility method, that updates the coordinates of a component (rotation, translation).
     * @param p_rel location vector relative to the center of the agent
     * @return updated relative position
     */
    private IntegerVec2D updateRelativePosition(IntegerVec2D p_rel){
        double exact_rotation = actor.getExactRotation();
        double x_rel = (double)p_rel.x;
        double y_rel = (double)p_rel.y;
        int ax = actor.getX();
        int ay = actor.getY();
        double x_new = x_rel*Math.cos(exact_rotation*0.017453)-y_rel*Math.sin(exact_rotation*0.017453)+ax;
        double y_new = x_rel*Math.sin(exact_rotation*0.017453)+y_rel*Math.cos(exact_rotation*0.017453)+ay;
        return new IntegerVec2D(round(x_new),round(y_new));
    }

    /**
     * Measurement results of the collision detectors.
     * @return array with the measurements of all sensors.
     */
    public IntegerVec2D[] detectCollisions(){
        BufferedImage img = getWorld().getBackground().getAwtImage();
        IntegerVec2D[] p_col_neu = new IntegerVec2D[sp_col_points.length];
        for (int i=0;i<sp_col_points.length;i++){
            p_col_neu[i] = updateRelativePosition(sp_col_points[i]);
        }
        int p0,p1;
        if (collisions==null ) collisions = new IntegerVec2D[col_sensors_from_to.length];
        for (int i=0;i<collisions.length;i++){
            p0=col_sensors_from_to[i][0];
            p1=col_sensors_from_to[i][1];
            collisions[i]=sensorLine(img,p_col_neu[p0], p_col_neu[p1], RaceTrack.ROAD_RGB,true);
            if ((drawCollisionSensors)&&(collisions[i]!=null)){
                GlassPane.enqueueLine(p_col_neu[p0].x,p_col_neu[p0].y,p_col_neu[p1].x,p_col_neu[p1].y);
            }
        }
        return collisions;
    }
    
    @Override
    public IntegerVec2D[] detectDistances(){
        BufferedImage img = getWorld().getBackground().getAwtImage();
        if (distances==null) distances = new IntegerVec2D[dist_sensors_direction.length];
        IntegerVec2D p_neu,p_dir;
        int dx,dy;
        for (int i=0;i<distances.length;i++){
            p_neu = updateRelativePosition(sp_dist_points[i]);
            dx = dist_sensors_direction[i][0];
            dy = dist_sensors_direction[i][1];
            p_dir = updateRelativePosition(new IntegerVec2D(sp_dist_points[i].x+dx,sp_dist_points[i].y+dy));
            distances[i]=sensorLine(img,p_neu,p_dir,RaceTrack.ROAD_RGB,true);
            if ((drawDistanceSensorBeams)&&(distances[i]!=null)){
                GlassPane.enqueueLine(p_neu.x,p_neu.y,p_neu.x+distances[i].x,p_neu.y+distances[i].y);
            }
        }
        return distances;
    }
    
    public double getRangeDistanceSensors(){
        return range_distance_sensor;
    }
    
    public String toString(){
        String str_out = "active collision sensors=[";
                         
        for (int i=0;i<collisions.length;i++){
            if (collisions[i]!=null) 
                str_out+="+ ";
            else
                str_out+="- ";
        }
        str_out+="]\n";
        
        str_out +="distance sensor values=[ ";
        for (IntegerVec2D vec : distances){
            if (vec!=null)
                str_out+=vec.toString()+" ";
            else
                str_out+="[ - ]";
        }
        str_out+="]\n";
        return str_out;
    }
}
