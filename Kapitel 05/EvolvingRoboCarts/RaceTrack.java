import greenfoot.*;
import java.util.ArrayList;
import java.util.List;

/**
 * World for robot cart races.
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
public class RaceTrack extends World
{
    public final static int POPULATION_SIZE = 60; 

    private GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm();

    protected int cnt_steps = 0;
    protected int max_steps = 5000;
    protected int cnt_episodes = 0;
    protected int max_episodes = 10000;
    protected double avg_fitness = 0;

    private int cnt_crashed = 0;

    protected int loggingInterval = 1;

    private AdaptiveMbRobot[] robotPopulation;

    public static final int ROAD_RGB = -9605779;

    private GlassPane glasspane;
    private Cursor cursor = null;
    public static JfxMonitor jfxMonitor = null;

    private int startX=200,startY=130;

    public RaceTrack() {    
        super(1200, 1200*9/16, 1); 
        setBackground(new GreenfootImage("background_wood_1200x675.png"));
        prepareWorld();
        
        if (AdaptiveMbRobot.neural_net_structure == null) {
            // If you change the number of sensors, the structure (at least the input layer) here must be adapted to this change. 
            AdaptiveMbRobot.neural_net_structure = new int[3];
            AdaptiveMbRobot.neural_net_structure[0] = 4; // number of sensors
            AdaptiveMbRobot.neural_net_structure[1] = 3; // number of hidden neurons
            AdaptiveMbRobot.neural_net_structure[2] = 2; // number of motors
        }

        geneticAlgorithm.generateNewGenePool(POPULATION_SIZE, NeuralNetwork.calcNumberOfWeights(AdaptiveMbRobot.neural_net_structure));
        geneticAlgorithm.breedPopulation();
        makeNextGeneration(startX,startY);

        prepareCursor();
        connectCursorWith(robotPopulation[0]);
        jfxMonitor = new JfxMonitor("agent",robotPopulation[0].getSensors().getSensorNames(), robotPopulation[0].getMotors().getMotorNames());

        Explosion.initializeImages();
    }
    
    /**
     * Constructs one robot from the give genome. It describes the weights of the neural network.
     * @param gene gene that describe the weights of the neural network.
     */
    private AdaptiveMbRobot produceRobot(Genome gene){
        //Sensors robot_sensors = new MbRobotSensors();
        Sensors robot_sensors = new MbRobotSensors4(); // Normally MakeBlock robots have only 2 ultrasonic sensors, but 4 works better...
        Motors robot_motors = new MbRobotMotors();
        addObject(robot_sensors,0,0);  
        addObject(robot_motors,0,0);       
        return new AdaptiveMbRobot(robot_sensors, robot_motors, gene);
    }

    /** 
     * Connects a cursor to the given robot.
     * @param robot The robot to which the cursor points.
     */
    public void connectCursorWith(AdaptiveMbRobot robot){
        cursor.setRobot(robot);
        robot.setCursor(cursor);
        glasspane.setNeuralNetImage(robot.getBrain());
    }

    /**
     * Deletes the connection between a robot and the cursor. 
     * @param robot The robot that the cursor points to at the beginning.
     */
    public void clearCursorConnection(){
        AdaptiveMbRobot robot = cursor.getRobot();
        cursor.setRobot(null);
        if (robot!=null){
            robot.setCursor(null);
        }
    }

    public void act(){
        if (cursor != null){
            MakeblockRobot mbRobot = cursor.getRobot();
            if (mbRobot!=null){
                int rx = mbRobot.getX();
                int ry = mbRobot.getY();
                int cx = cursor.getX();
                int cy = cursor.getY();            
                cursor.setLocation(rx, ry);
            }
        }; 

        checkKeys();

        if (isEndOfEpisode()){ 
            startNewEpisode();
        } 
        cnt_steps++;
    }

    public boolean isEndOfEpisode(){
        return (cnt_crashed>=robotPopulation.length); //  || (cnt_steps>=max_steps)); [Add this, if episodes not end.]
    }

    /**
     * Resets the scenario, the data loggers and the actors and gives them the next generation of neural networks as their "brains".
     */
    protected void startNewEpisode() {
        double topFitness = geneticAlgorithm.getBestGenomes(1).get(0).fitness;
        avg_fitness+=topFitness;

        if (cnt_episodes%loggingInterval==0){
            logEpisodeResult(cnt_episodes,avg_fitness/loggingInterval);
            avg_fitness=0;
        }

        if (cnt_episodes>=max_episodes) Greenfoot.stop();

        jfxMonitor.clearEpsiodeData();

        resetActors();
        makeNextGeneration(startX,startY);
        prepareCursor();
        connectCursorWith(robotPopulation[0]);

        cnt_episodes++;
        cnt_steps=0;
    } 

    /**
     * Removes all actors and recreates the initial state of the world.
     */
    public void resetActors(){
        removeObjects(getObjects(null));
        prepareWorld();
    }

    /**
     * Produces the next generation of robots with the new selected neural networks.
     * @param globalX  X-coordinate of the position where the created robot will be placed.
     * @param globalY  Y-coordinate of the position where the created robot will be placed.
     */
    public void makeNextGeneration(int globalX, int globalY){
        robotPopulation = new AdaptiveMbRobot[geneticAlgorithm.getGenePool().size()];
        geneticAlgorithm.breedPopulation();
        ArrayList <Genome> currGenePool = geneticAlgorithm.getGenePool(); 
        int i=0;
        for (Genome gene : currGenePool){
            robotPopulation[i] = produceRobot(gene);
            robotPopulation[i].setCrashed(false);
            addObject(robotPopulation[i],0,0);
            robotPopulation[i].setLocation(globalX, globalY); 
            i++;
        }
        cnt_crashed=0;
        if (jfxMonitor!=null) jfxMonitor.clearEpsiodeData();
    }
    
    /**
     * Removes a "crashed" robot from the world.
     * @param crashed_robot The robot to remove.
     */
    public void removeCrashed(AdaptiveMbRobot crashed_robot){
        if (crashed_robot==cursor.getRobot()) clearCursorConnection();
        removeObject(crashed_robot);
        cnt_crashed++;
    }

    /**
     * "Glasspane" for displaying the sensors rays.
     */
    public GlassPane getGlassPane(){
        return glasspane;
    }

    @Override
    public void started() {
        System.out.println("started");
    }

    @Override
    public void stopped() {
        System.out.println("stopped");
    }

    /**
     * Prints informations about the state of the given robot agent.
     * @param robot The robot agent whose information is to be printed.
     */
    protected void printAgentStateInfos(AdaptiveMbRobot robot){
        Motors motors = robot.getMotors();
        String[] motornames = motors.getMotorNames();
        String str_out = "robot ID:"+robot.getID()+"\n"+
            "step="+cnt_steps+"\n"+
            "X="+robot.getX()+" Y="+ robot.getY()+"\n"+
            "X_exact="+robot.getExactX()+" Y_exact="+robot.getExactY()+" direction_exact="+robot.getExactRotation()+"° \n"+
            "dx="+robot.getMovement().getX()+" dy="+robot.getMovement().getY()+"\n\n"+
            "battery charge="+robot.getBatteryCharge()+"\n"+
            robot.getMotors().toString()+"\n"+
            robot.getSensors().toString()+"\n";
        jfxMonitor.setAgentStateInfos(str_out);

        NeuralNetwork robotBrain = robot.getBrain();
        str_out = "genome.ID ="+robot.getGenome().ID+"\n";
        str_out += robotBrain.toString()+"\n";
        jfxMonitor.setAgentStateAdditionalInfos(str_out);
    }

    /**
     * Updates the monitor that displays the state curves.
     * @param robot
     * @param cnt_steps
     * @param s
     * @param a
     */
    public void updateStateCurveMonitors(AdaptiveMbRobot robot, int cnt_steps, double[] s, double[] a){
        printAgentStateInfos(robot);
        try{
            jfxMonitor.append(cnt_steps,s,a);
        }catch (Exception e){
            System.out.println(e);
        }  
    }

    /**
     * Logs the results of an episode for checking learning progress.
     * @param cnt_episode The current episode number.
     * @param reward Rewards collected (Y-axis, the dependent variable).
     */
    public void logEpisodeResult(int cnt_episode, double reward ){
        jfxMonitor.appendEpisodeResult(cnt_episode, reward);
    }

    /**
     * Creates the cursor.
     */
    private void prepareCursor(){   
        cursor = new Cursor();
        addObject(cursor, startX, startY);      
    }

    /**
     * Check whether there are any key pressed and react to them.
     */
    private void checkKeys() 
    {   
        double dx = 0.0;
        double dy = 0.0;
        AdaptiveMbRobot selected_robot = null;

        if (Greenfoot.mousePressed(null)){
            MouseInfo mouse = Greenfoot.getMouseInfo();
            int mx = mouse.getX();
            int my = mouse.getY();
            int cx = cursor.getX();
            int cy = cursor.getY();
            cursor.move(mx-cx,my-cy);
            selected_robot = cursor.getIntersectingRobot();
            if (selected_robot == null){
                clearCursorConnection();
            }else{
                connectCursorWith(selected_robot);
            };
        }

        if (Greenfoot.isKeyDown("LEFT")){
            dx = -Cursor.CURSOR_SPEED;
            clearCursorConnection();
        }

        if (Greenfoot.isKeyDown("RIGHT")){
            dx = Cursor.CURSOR_SPEED;
            clearCursorConnection();
        }

        if (Greenfoot.isKeyDown("UP")){
            dy = -Cursor.CURSOR_SPEED;
            clearCursorConnection();
        }

        if (Greenfoot.isKeyDown("DOWN")){
            dy = Cursor.CURSOR_SPEED;
            clearCursorConnection();
        }

        if (Greenfoot.isKeyDown("SPACE")){
            int cx = cursor.getX();
            int cy = cursor.getY();            
            selected_robot = cursor.getIntersectingRobot();
            if (selected_robot == null){
                clearCursorConnection();
            }else{
                connectCursorWith(selected_robot);
            };
        }
        cursor.setMovement(new Vector(dx,dy));
        cursor.move();
    }

    /**
     * Produces the "non-intelligent" elements of the world.
     */
    private void  prepareWorld(){  
        Apple apple = new Apple();
        addObject(apple,387,74);
        Apple apple1 = new Apple();
        addObject(apple1,300,90);
        Apple apple2 = new Apple();
        addObject(apple2,532,59);
        Apple apple3 = new Apple();
        addObject(apple3,663,85);
        Apple apple4 = new Apple();
        addObject(apple4,811,118);
        Apple apple5 = new Apple();
        addObject(apple5,943,153);
        Mushroom mushroom = new Mushroom();
        addObject(mushroom,865,170);
        Apple apple6 = new Apple();
        addObject(apple6,1076,270);
        Apple apple7 = new Apple();
        addObject(apple7,996,550);
        Apple apple8 = new Apple();
        addObject(apple8,1055,453);
        Apple apple9 = new Apple();
        addObject(apple9,752,470);
        Apple apple10 = new Apple();
        addObject(apple10,572,370);
        Apple apple12 = new Apple();
        addObject(apple12,665,389);
        Apple apple13 = new Apple();
        addObject(apple13,544,451);
        Apple apple14 = new Apple();
        addObject(apple14,524,620);
        Apple apple15 = new Apple();
        addObject(apple15,199,515);
        Apple apple16 = new Apple();
        addObject(apple16,66,281);
        Apple apple17 = new Apple();
        addObject(apple17,130,162);
        Apple apple18 = new Apple();
        addObject(apple12,868,530);

        Mushroom mushroom1 = new Mushroom();
        addObject(mushroom1,343,306);

        glasspane = new GlassPane(getWidth(),getHeight(),getWidth()/4,getHeight()/4);
        addObject(glasspane, getWidth()/2, getHeight()/2);
    }

}
