import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.*;
import javafx.stage.Window;
import javafx.stage.Stage;

/**
 * An environment for policy searching hamster agents.
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
public class PolicySearch_Environment extends RL_GridEnv
{
    public static RL_GridWorldAgent hamster = null;
    
    public static int NUM_A3C_AGENTS = 8;
    public static A3C_Hamster[] hamsters = new A3C_Hamster[NUM_A3C_AGENTS];
    public static int EVALUATION_INTERVAL = 1; // interval for making an average and displaying a result.
                                                   
    public static boolean DISPLAY_AGENT = true;  // switch off for speed up
    public static boolean DISPLAY_UPDATE = false; // switch off for speed up
    public static int DISPLAY_UPDATE_INTERVAL = 1; // interval for making a update of the marker that visualize 
                                                      // data structures (N,V,Policy, ...) on the greenfoot screen.
                                                      // (increase for speedup)
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";

    protected long sim_time = 0;
    protected long nano_starttime = 0;
    
    /**
     * Constructor of this environment.
     */
    public PolicySearch_Environment()
    {
        // super(mapDynaMaze);
        super(mapFlat);
        //super(mapWithTrap3); 
        //super(mapFrozenLake);
        //super(mapLabyrinth1);
        
        //hamster = new REINFORCE_Hamster();
        //hamster = new REINFORCE_Hamster_withBaseline();
        //hamster = new ActorCritic_Hamster();
        hamster = new PPO_Hamster();
        //for (int i=0;i<NUM_A3C_AGENTS; i++) hamsters[i] = new A3C_Hamster();  // A3C
        
        if (hamsters[0]!=null){
            // put all agents to start state
            for (int i=0;i<NUM_A3C_AGENTS; i++)
                this.addObject(hamsters[i],getHamsterStartX(),getHamsterStartY());
            
            // reset shared static variables
            A3C_Hamster.thetas_global.clear(); 
            A3C_Hamster.V_global.clear(); 
            A3C_Hamster.cnt_steps_global=0;
            A3C_Hamster.cnt_episodes_global=0;
            A3C_Hamster.sum_reward_global=0.0;
            A3C_Hamster.lastID=0;    
        }else{
            this.addObject(hamster,getHamsterStartX(),getHamsterStartY());
        }
        sim_time=0;
    }

    @Override
    public void started() {
        nano_starttime = System.nanoTime();
        System.out.println("greenfoot started ("+JfxChartLogger.getTimeStamp()+")");
        if (jfxLogger==null) {
            initJfxLogger();
        } 
    }
    
    @Override
    public void stopped() {
        sim_time += System.nanoTime()-nano_starttime;
        System.out.println("greenfoot stopped ("+JfxChartLogger.getTimeStamp()+") running time ="+((double)sim_time)/1000000000+" sec");
        if (jfxLogger!=null) {
            jfxLogger.save(false); // store cached data to disc, remain file open.
        }
        if (hamsters[0]!=null){
            for (int i=0;i<NUM_A3C_AGENTS; i++) hamsters[i].stopWorker();
        }
    }
    
    /**
     * This initializes the data logger depending on the selected agent class.
     */
    private void initJfxLogger(){
        if ((hamster==null)&&(hamsters[0]==null)){
            System.out.println("[env.initJfxLogger()] No hamster agent intanciated !");
            return;
        }
        Class agent_class = null;
        if (hamsters[0]!=null){
            agent_class = hamsters[0].getClass();
        }else{
            agent_class = hamster.getClass();
        }
        String sAgentClass = agent_class.toString();
        sAgentClass=sAgentClass.replaceFirst("class ", "");      
        jfxLogger = new JfxChartLogger(logFilePath+sAgentClass+"_","Efficiency of the "+sAgentClass+"-policy",sAgentClass,"episodes","collected reward");
        jfxLogger.appendln("environment attributes:");
        jfxLogger.appendln("reward for a grain;"+rewardForAGrain);
        jfxLogger.appendln("reward trap;"+rewardTrap);
        jfxLogger.appendln("transition effort;"+rewardPerTransition);
        jfxLogger.appendln("arena:");
        for (String line : fieldDescription) jfxLogger.appendln(line);

        if (agent_class==REINFORCE_Hamster.class){
            jfxLogger.appendln("REINFORCE learning parameter:");  
            jfxLogger.appendln("ETA_theta;"+((REINFORCE_Hamster)hamster).ETA_theta);
            jfxLogger.appendln("GAMMA;"+((REINFORCE_Hamster)hamster).GAMMA);
            jfxLogger.appendln("T;"+((REINFORCE_Hamster)hamster).T);  // 'temperature' in SoftMax 
        }else if (agent_class==REINFORCE_Hamster_withBaseline.class){
            jfxLogger.appendln("REINFORCE with baseline learning parameter:"); 
            jfxLogger.appendln("ETA_V;"+((REINFORCE_Hamster_withBaseline)hamster).ETA_V);
            jfxLogger.appendln("ETA_theta;"+((REINFORCE_Hamster_withBaseline)hamster).ETA_theta);
            jfxLogger.appendln("GAMMA;"+((REINFORCE_Hamster_withBaseline)hamster).GAMMA);
            jfxLogger.appendln("T;"+((REINFORCE_Hamster_withBaseline)hamster).T);  // 'temperature' in SoftMax 
        }else if (agent_class==ActorCritic_Hamster.class){
            jfxLogger.appendln("actor critic learning parameter:");
            jfxLogger.appendln("ETA_V;"+((ActorCritic_Hamster)hamster).ETA_V);
            jfxLogger.appendln("ETA_theta;"+((ActorCritic_Hamster)hamster).ETA_theta);
            jfxLogger.appendln("GAMMA;"+((ActorCritic_Hamster)hamster).GAMMA);
            jfxLogger.appendln("T;"+((ActorCritic_Hamster)hamster).T);  // 'temperature' in SoftMax 
        }else if (agent_class==A3C_Hamster.class){
            jfxLogger.appendln("A3C learning parameter:");
            jfxLogger.appendln("number of A3C agents;"+this.NUM_A3C_AGENTS);
            jfxLogger.appendln("nsteps (reward);"+((A3C_Hamster)hamsters[0]).nsteps);
            jfxLogger.appendln("ETA_V;"+((A3C_Hamster)hamsters[0]).ETA_V);
            jfxLogger.appendln("ETA_theta;"+((A3C_Hamster)hamsters[0]).ETA_theta);
            jfxLogger.appendln("GAMMA;"+((A3C_Hamster)hamsters[0]).GAMMA);
            jfxLogger.appendln("T;"+((A3C_Hamster)hamsters[0]).T);  // 'temperature' in SoftMax 
        }
        
        jfxLogger.appendln("interval;"+EVALUATION_INTERVAL);
        jfxLogger.append("start;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n");   
    }
   
    /**
     * Updates the display of the state value objects in the territory according to V(s).
     * @param pgHamster hamster agent with policy gradiend algorithm
     */
    public void updateDisplay(ActorCritic_Hamster pgHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
               // updateQMarker(pgHamster,i,j);
                updateValue(pgHamster,i,j);
                updateCounter(pgHamster,i,j);
                updatePolicyMarker(pgHamster,i,j);
            }
        }
    }
    
    /**
     * Updates the display of the state value objects in the territory according to V(s).
     * @param pgHamster hamster agent with policy gradiend algorithm
     */
    public void updateDisplay(A3C_Hamster pgHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                //updateQMarker(pgHamster,i,j);
                updateValue(pgHamster,i,j);
                updatePolicyMarker(pgHamster,i,j);
            }
        }
    }
    
   /**
    * Updates the display of the state value objects in the territory according to V(s).
    */
    public void updateDisplay(REINFORCE_Hamster pgHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                //updateQMarker(pgHamster,i,j);
                updateCounter(pgHamster,i,j);
                updatePolicyMarker(pgHamster,i,j);
            }
        }
    }
    
   /**
    * Updates the display of the state value objects in the territory according to V(s).
    */
    public void updateDisplay(REINFORCE_Hamster_withBaseline pgHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateCounter(pgHamster,i,j);
                updateValue(pgHamster,i,j);
                updatePolicyMarker(pgHamster,i,j);
            }
        }
    }
    
    /**
    * Updates the display of the state value objects in the territory according to V(s).
    */
    public void updateValue(REINFORCE_Hamster_withBaseline pgHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+pgHamster.getGrainsInJaws()+"]";    
        Double val = pgHamster.getV(s_key);
        if (val!=null) {
            List <Value> valueObjects = this.getObjectsAt(x,y,Value.class);
            Value v = null;
            if ( valueObjects.size()>0 ) {
                v=((Value)valueObjects.get(0));
            }else{
                v = new Value(isTerminal(x,y));
                super.addObject(v,x,y);
            }
            v.update(val);
        }
    }
    
    /**
     * Updates the visit count.
     */
    public void updateCounter(Hamster pgHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+pgHamster.getGrainsInJaws()+"]";    
        List <Visitcounter> cObjects = this.getObjectsAt(x,y,Visitcounter.class);
        Visitcounter co = null;
        if ( cObjects.size()>0 ) {
            co = ((Visitcounter)cObjects.get(0));                          
        }else{
            co = new Visitcounter(0);
            super.addObject(co,x,y);
        }
        int c = hamster.getN(s_key);
        co.setC(c);
    }
    
    /**
     * Updates the display of the state value objects in the territory according to V(s).
     */
    public void updateValue(ActorCritic_Hamster pgHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+pgHamster.getGrainsInJaws()+"]";    
        Double val = pgHamster.getV(s_key);
        if (val!=null) {
            List <Value> valueObjects = this.getObjectsAt(x,y,Value.class);
            Value v = null;
            if ( valueObjects.size()>0 ) {
                v=((Value)valueObjects.get(0));
            }else{
                v = new Value(isTerminal(x,y));
                super.addObject(v,x,y);
            }
            v.update(val);
        }
    }
    
    /**
     * Updates the objects that display the Q-Values in the territory for the specific type of agent. ( Q(s,a) )
     */
    public void updateQMarker(ActorCritic_Hamster qHamster, int x, int y)
    {
         List <QValueMarker> qValueMarkerObjects;
         String key="["+x+","+y+","+qHamster.getGrainsInJaws()+"]";    
         double[] theta = qHamster.getTheta(key);
         if (theta!=null){
             //System.out.println(key+"->"+Arrays.toString(theta));
             for (int a=0;a<theta.length;a++){
                 double theta_a = theta[a];               
                 qValueMarkerObjects = this.getObjectsAt(x,y,QValueMarker.class);
                 boolean update = false;
                 for (QValueMarker qvm : qValueMarkerObjects){
                     int a_qvm = qvm.getA();
                     if (a_qvm==a){
                         qvm.setQ(theta_a);
                         update = true;
                         break;
                     }
                 }
                 if (update==false) {
                     addObject(new QValueMarker(theta_a,a),x,y);
                 }        
             }
         }
    }
   
    /**
     * Updates the objects that display the policy in the territory for the specific type of agent.
     */
    public void updatePolicyMarker(ActorCritic_Hamster pgHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+pgHamster.getGrainsInJaws()+"]";
        double[] theta = pgHamster.getTheta(s_key);
        if (theta!=null){
            double[] pi_s = pgHamster.P_Policy(s_key);
            if (pi_s!=null){
                List <Policymarker> objects = getObjectsAt(x,y,Policymarker.class);
                removeObjects(objects);
                for (int a=0;a<pi_s.length;a++){
                    boolean update = false;
                    addObject(new Policymarker(pi_s[a],a),x,y);
                }
            }
        }
    }
    
    /**
     * Updates the objects that display the policy in the territory for the specific type of agent.
     */
    public void updatePolicyMarker(REINFORCE_Hamster pgHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+pgHamster.getGrainsInJaws()+"]";
        double[] theta = pgHamster.getTheta(s_key);
        if (theta!=null){
            double[] pi_s = pgHamster.P_Policy(s_key);
            if (pi_s!=null){
                List <Policymarker> objects = getObjectsAt(x,y,Policymarker.class);
                removeObjects(objects);
                for (int a=0;a<pi_s.length;a++){
                    boolean update = false;
                    addObject(new Policymarker(pi_s[a],a),x,y);
                }
            }
        }
    }
    
    /**
     *  Updates the objects that display the Q-Values in the territory for the specific type of agent. ( Q(s,a) )
     */
    public void updateQMarker(REINFORCE_Hamster qHamster, int x, int y)
    {
         List <QValueMarker> qValueMarkerObjects;
         String key="["+x+","+y+","+qHamster.getGrainsInJaws()+"]";    
         double[] theta = qHamster.getTheta(key);
         if (theta!=null){
             //System.out.println(key+"->"+Arrays.toString(theta));
             for (int a=0;a<theta.length;a++){
                 double theta_a = theta[a];               
                 qValueMarkerObjects = this.getObjectsAt(x,y,QValueMarker.class);
                 boolean update = false;
                 for (QValueMarker qvm : qValueMarkerObjects){
                     int a_qvm = qvm.getA();
                     if (a_qvm==a){
                         qvm.setQ(theta_a);
                         update = true;
                         break;
                     }
                 }
                 if (update==false) {
                     addObject(new QValueMarker(theta_a,a),x,y);
                 }        
             }
         }
    }
   
    /**
     * Remove all policy marker.
     */ 
    public void removeTracemarker()
    {
        List objects = getObjects(Policymarker.class);
        removeObjects(objects);
    }
}