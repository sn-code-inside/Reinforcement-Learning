import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * A robot with a differential drive that looks like a Makeblock-robot (super class) and an artificial neural network as 
 * a "brain" that can be generated from a given "Genome".
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
public class AdaptiveMbRobot extends MakeblockRobot
{
    private static long lastID = 0;
    private long ID =-1;
    
    private static Class targetClass = Apple.class;
    private static Class negativeClass = Mushroom.class;
    
    public static int[] neural_net_structure = null; // number of neurons in the input-, the hidden- and the output-layer
    
    protected double sum_reward = 0;
    
    protected RaceTrack world = null;
    
    private Genome gene = null;
    
    private NeuralNetwork brain = null;
    
    public AdaptiveMbRobot(Sensors sensors, Motors motors, Genome gene ){
        super(sensors,motors);
        this.ID = AdaptiveMbRobot.lastID;
        AdaptiveMbRobot.lastID++;
        this.crashed=false;
        this.sum_reward = 0.0;
        this.battery_charge=MAX_BATTERY_CHARGE;
        this.gene = gene;
        this.brain = new NeuralNetwork(neural_net_structure);
        this.brain.createWeightsFromGenome(gene);
    }

    protected void addedToWorld(World world){
        this.world = (RaceTrack)world;
    }
    
    @Override
    public void act() {
        s = getState();
        a = policy(s);
        setAction(a);
        move();
        sum_reward += getReward();
        this.gene.fitness = sum_reward;
        if (this.isCrashed()) world.removeCrashed(this);
        if (connectedCursor!=null) world.updateStateCurveMonitors(this, cnt_steps,s,a); 
        cnt_steps++;                 
    }
    
    /**
     * Agent's policy. Returns actions to the given state.
     * @param state describes the sensoric state of the agent.
     */
    public double[] policy(double[] state){
        brain.setInput(state);
        brain.calculate();
        return brain.getOutput();
    }
    
    /**
     * This is the reward function. Returns the reward for the current state of the agent.
     */
    public double getReward(){
        if (crashed) return 0.0;
        double reward = 0;
        Actor target = this.getOneIntersectingObject(targetClass);
        if (target!=null){
            if (target.getClass() == Apple.class){
                Apple apple = (Apple)target;
                if (apple.take(this.ID)){
                     reward = apple.getValue();
                     battery_charge = MAX_BATTERY_CHARGE;
                     getWorld().addObject(new ShowRewardCollect(reward), target.getX(), target.getY());
                }
            }
        }
        
        Actor trap = this.getOneIntersectingObject(negativeClass);
        if (trap!=null){
            if (trap.getClass() == Mushroom.class){
                Mushroom mushroom = (Mushroom)trap;
                if (mushroom.take(this.ID)){
                     reward = mushroom.getValue();
                     getWorld().addObject(new ShowRewardCollect(reward), trap.getX(), trap.getY());
                }
            }
        }
        double sum_r = 0;
        
       // for (double d_a : a) sum_r += d_a*d_a; // a little reward for staying alive and moving.
       // reward+=0.0001*Math.sqrt(sum_r);

        if (battery_charge<=0) {
            setCrashed(true);
        }
        return reward;
    }
    
    /**
     * Sets a pointer to a given cursor. The connection between a cursor and a robot
     * should be made in the World class. This method is called from there.
     * @param cursor The cursor to be connected to the robot.
     */
    public void setCursor(Cursor cursor){
        this.connectedCursor = cursor;
        if (cursor!=null){
            if (cursor.getRobot()!=this) cursor.setRobot(this);
            drawDistanceSensorBeams(true);
            drawCollisionSensors(true);
        }else{
            drawDistanceSensorBeams(false);
            drawCollisionSensors(false);
        }
    }
    
    /**
     * Plays the "failbuzzer" sound (if battery charge is 0).
     * @param volume loudness of the sound
     */
    private void playFailBuzzer(int volume){
        GreenfootSound sound = new GreenfootSound("fail-buzzer-02.mp3");
        sound.setVolume(volume);    
        sound.play();
    }

    /**
     * Returns the sum of all rewards received.
     */
    public double getRewardSum(){
        return sum_reward;
    }
    
    /**
     * Resets the reward sum to 0.
     */
    public void resetRewardSum(){
        this.sum_reward=0.0;
    }
    
    /**
     * Returns the genome of the robot.
     */
    public Genome getGenome(){
        return this.gene;
    }
    
    /**
     * Gets the Neural Network of the robot.
     * @return The Neural Network that controls the robot.
     */
    public NeuralNetwork getBrain(){
        return this.brain;
    }
    
    public long getID(){
        return this.ID;
    }
}
