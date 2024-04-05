import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Java implementation of the "mountain landscape" from the 'mountain car' environment by Sutton, Barto.
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
public class RL_MountainEnv extends MountainModel
{
    private Monitor textMonitor;
   
    public static int EVALUATION_INTERVAL = 10;   // interval for making an average and displaying a result.
    public static int MAX_NUMBER_EPISODES = 25000; // maximum number of learning episodes of this agent
    public static int MAX_LENGTH_EPISODE = 5000;
    public static final int REWARD_GOAL = 0;
    
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
    
    public static RL_MountainCar agent = null; 
    
    /**
     * Konstruktor für Objekte der Klasse RL_MountainModel
     */
    public RL_MountainEnv()
    {
        super();
        
        agent=new RL_MountainCar();
        
        this.addObject(agent, 0, 0);     
        agent.setToStart();
        
        textMonitor = new Monitor("Episode=eee  t=ttttt  s_t=[x.xxx;y.yyy]");
        addObject(textMonitor, textMonitor.getImage().getWidth()/2+20, 20);
    }
    
        /**
     * This initializes the data logger depending on selected agent class.
     */
    private void initJfxLogger(){
        if (agent==null){
            System.out.println("[env.initJfxLogger()] No agent intanciated !");
            return;
        }
        Class agent_class = agent.getClass();
        String sAgentClass = agent_class.toString();
        sAgentClass=sAgentClass.replaceFirst("class ", "");
        jfxLogger = new JfxChartLogger(logFilePath+sAgentClass+"_","Efficiency of the "+sAgentClass+"-policy","reward","episodes","collected reward");
        jfxLogger.appendln("agent class;"+sAgentClass);
        jfxLogger.appendln("learning paramter:");
        if ((agent_class==RL_MountainCar.class)){
            jfxLogger.appendln("ETA;"+agent.ETA);
            jfxLogger.appendln("GAMMA;"+agent.GAMMA);    
            jfxLogger.appendln("EPSILON;"+agent.EPSILON);
            jfxLogger.appendln("MLP-layers:"+Arrays.toString(agent.getLayerSizesMLP()));
            jfxLogger.appendln("MLP-learningRate:"+agent.getLearningRate());
        }
        jfxLogger.append("start;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n"); 
    }
    
    @Override
    public void started() {
        System.out.println("greenfoot started ("+JfxChartLogger.getTimeStamp()+")");
        if (jfxLogger==null) {
            initJfxLogger();
        } 
    }
    
    @Override
    public void stopped() {
        System.out.println("greenfoot stopped ("+JfxChartLogger.getTimeStamp()+")");
        if (jfxLogger!=null) {
            jfxLogger.save(false); // store cached data to disc, remain file open.
        }
    }
    
    /**
     * Calculates the rewards of an agent.
     * @param agent
     * @return reward
     */
    public double getReward(RL_MountainCar rl_agent)
    {
        if (rl_agent.getXPos()<this.rightBorder){
            return -1;
        }
        return REWARD_GOAL;
    } 
    
    /**
     * Checks if the state of an agent is terminal.
     * @param agent
     * @return agent in a terminal state? (true/false)
     */
    public boolean isTerminal(RL_MountainCar agent){
        return (getReward(agent)>=0);
    }
    
    /**
     * Set text output.
     */
    public void setMonitorText(String text)
    {
        textMonitor.setText(text);
    }
    
    /**
     * Returns a list of possible actions for a given state.
     * @param s state
     * @return a list of possible actions for a given state
     */
    public ArrayList <Integer> coursesOfAction(State s)
    {
        ArrayList <Integer> ret = new <Integer> ArrayList();
        if (!isTerminal(agent)){ 
            ret.add(0);
            ret.add(1);
            ret.add(2);
        }
        return ret;
    }
}
