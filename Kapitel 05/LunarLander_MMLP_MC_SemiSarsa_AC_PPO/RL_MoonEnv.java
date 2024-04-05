import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.ArrayList;
import java.util.Arrays;

/*
 * Environment for the LunarLander Reinforcement Learning scenario.
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

public class RL_MoonEnv extends Moon
{
    private Monitor textMonitor = null;
    public static int EVALUATION_INTERVAL = 100;   // interval for making an average and displaying a result.
    public static int MAX_NUMBER_EPISODES = 50000; // maximum number of learning episodes of this agent
    
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
    
    public static RL_SemiGradientSarsa_Lander agent = null;
    
    public RL_MoonEnv()
    {
        super();
        System.out.println("START!");
        // agent = new RL_SemiGradientSarsa_Lander();
        agent = new RL_MMLP_MC_Lander();
        // agent= new RL_MMLP_ActorCritic_Lander();
        // agent= new RL_MLP_ActorCritic_Lander();
        // agent= new RL_LunarLander_MLP_PPO();
        addObject(agent, landerStartX, landerStartY);
        
        textMonitor = new Monitor("episode=0  s_t=");
        addObject(textMonitor, textMonitor.getImage().getWidth()/2+10, 20);
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
        if ((agent_class==RL_SemiGradientSarsa_Lander.class)||(agent_class==RL_MMLP_MC_Lander.class)){
            jfxLogger.appendln("ETA;"+agent.ETA);
            jfxLogger.appendln("GAMMA;"+agent.GAMMA);    
            jfxLogger.appendln("EPSILON;"+agent.EPSILON);
            jfxLogger.appendln("MLP-layers:"+Arrays.toString(agent.getLayerSizesMLP()));
            jfxLogger.appendln("MLP-learningRate:"+agent.getLearningRate());
        }else if (agent_class==RL_MMLP_ActorCritic_Lander.class){
            jfxLogger.appendln("ETA_theta;"+((RL_MMLP_ActorCritic_Lander)agent).ETA_theta);
            jfxLogger.appendln("ETA_V;"+((RL_MMLP_ActorCritic_Lander)agent).ETA_V);
            jfxLogger.appendln("GAMMA;"+((RL_MMLP_ActorCritic_Lander)agent).GAMMA);    
            jfxLogger.appendln("T;"+((RL_MMLP_ActorCritic_Lander)agent).T);
            jfxLogger.appendln("MLP_V-Layers:"+Arrays.toString(((RL_MMLP_ActorCritic_Lander)agent).layersMLP_V));
            jfxLogger.appendln("MLP_h-Layers:"+Arrays.toString(((RL_MMLP_ActorCritic_Lander)agent).layersMLP_h));
            jfxLogger.appendln("MLP-Lernrate:"+agent.getLearningRate());
            
        }else if (agent_class==RL_LunarLander_MLP_PPO.class){
            jfxLogger.appendln("EPSILON_ppo;"+((RL_LunarLander_MLP_PPO)agent).ETA_ppo);
            jfxLogger.appendln("ETA_ppo;"+((RL_LunarLander_MLP_PPO)agent).ETA_ppo);
            jfxLogger.appendln("ETA_V;"+((RL_LunarLander_MLP_PPO)agent).ETA_V);
            jfxLogger.appendln("GAMMA;"+((RL_LunarLander_MLP_PPO)agent).GAMMA);    
            jfxLogger.appendln("T;"+((RL_LunarLander_MLP_PPO)agent).T);
            jfxLogger.appendln("MLP_V-Layers:"+Arrays.toString(((RL_LunarLander_MLP_PPO)agent).layersMLP_V));
            jfxLogger.appendln("MLP_h-Layers:"+Arrays.toString(((RL_LunarLander_MLP_PPO)agent).layersMLP_h));
            jfxLogger.appendln("MLP-Lernrate:"+agent.getLearningRate());
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
    public double getReward(RL_SemiGradientSarsa_Lander agent)
    {
        return agent.getReward();
    } 
    
    /**
     * Checks if the state of an agent is terminal.
     * @param agent
     * @return agent in a terminal state? (true/false)
     */
    public boolean isTerminal(RL_SemiGradientSarsa_Lander agent){
        return (getReward(agent)!=0);
    }
    
    /**
     * Returns X-coordinate of start position (initial state). 
     * @return X-coordinate of start position (initial state). 
     */
    public int getStartX(){
        return this.landerStartX;
    }
    
    /**
     * Returns Y-coordinate of start position (initial state). 
     * @return Y-coordinate of start position (initial state). 
     */
    public int getStartY(){
        return this.landerStartY;
    }
    
    /**
     * Returns a list of possible actions for a given state.
     * @param s state
     * @return a list of possible actions for a given state
     */
    public ArrayList <Integer> coursesOfAction(State s)
    {
        ArrayList <Integer> ret = new <Integer> ArrayList();
        ret.add(0);
        double[] s_vals = s.getValues();
        if ((s_vals[2]>0)||(s_vals[0]>0)) ret.add(1); // Action 1 is only available in limited flight altitude and if some fuel is left.
        return ret;
    }
    
    /**
     * Updates visualisation and information details of the agent.
     * @param 
     */
    public void updateDisplay(RL_SemiGradientSarsa_Lander agent){
        textMonitor.setText("episode="+agent.episode()+" s="+agent.getState().toString());
    }

}