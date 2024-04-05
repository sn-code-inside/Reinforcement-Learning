import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.*;

/**
 * An environment for "Monte-Carlo" hamster agents that, for state estimation, evaluate the results 
 * of complete episodes.
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
public class MC_AgentEnv extends RL_GridEnv
{
    public static RL_GridWorldAgent hamster = null;
    
    public static boolean DISPLAY_AGENT = true;       // switch off for speed up
    public static boolean DISPLAY_UPDATE = true;      // switch off for speed up
    public static int DISPLAY_UPDATE_INTERVAL = 5000; // steps interval for making a update of the markers that visualize data
                                                      //   structures (N,V,Policy, ...) on the greenfoot screen (increase for speedup).
    public static int EVALUATION_INTERVAL = 50;  // interval for making an average and displaying a result.
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
    
    public final static int MAX_NUMBER_EPISODES = 50000; // maximum number of learning episodes
    
    protected long nano_starttime = 0;  // contains simulation start time (nanoseconds)
    protected long sim_time = 0;        // contains simulation time in total (stops when greenfoot is paused)
    
    /**
     * Konstruktor für die MC_AgentenUmwelt.
     */
    public MC_AgentEnv()
    {
        /*# Here you can change the arena: */
        super(mapFlat); 
        //super(mapDynaMaze); 
        
        /*# Here you can change the type of the hamster agent: */
        hamster = new MC_Hamster();  
        //hamster = new MC_PolicySearch_Hamster();
        
        this.addObject(hamster,getHamsterStartX(),getHamsterStartY());
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
    }
    
    /**
     * This initializes the data logger depending on selected agent class.
     */
    private void initJfxLogger(){
        if (hamster==null){
            System.out.println("[env.initJfxLogger()] No hamster agent intanciated !");
            return;
        }
        Class agent_class = hamster.getClass();
        String sAgentClass = agent_class.toString();
        sAgentClass=sAgentClass.replaceFirst("class ", "");
        jfxLogger = new JfxChartLogger(logFilePath+sAgentClass+"_","Efficiency of the hamster policy","reward","episodes","collected reward");
        
        jfxLogger.appendln("environment attributes:");
        jfxLogger.appendln("reward for a grain;"+rewardForAGrain);
        jfxLogger.appendln("reward trap;"+rewardTrap);
        jfxLogger.appendln("transition effort;"+rewardPerTransition);
        jfxLogger.appendln("arena:");
        for (String line : fieldDescription) jfxLogger.appendln(line);
        
        jfxLogger.appendln("agent class;"+sAgentClass);
        if (agent_class==MC_Hamster.class){
            jfxLogger.appendln("learning parameter:");  
            jfxLogger.appendln("GAMMA;"+((MC_Hamster)hamster).GAMMA); 
            jfxLogger.appendln("EPSILON;"+((MC_Hamster)hamster).EPSILON);
        }
 
        jfxLogger.append("start;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n"); 
    }
        
    /**
     * Updates the display of the state value objects in the territory.
     * @param mcHamster Hamster agent with Monte-Carlo evaluation algorithm.
     */
    public void updateDisplay(MC_Hamster mcHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateValue(mcHamster,i,j);
                updateCounter(mcHamster,i,j);
            }
        }
    }
   
   /**
    * Updates the display of the state value and policy visualization objects in the territory.
    * @param mcpsHamster Hamster agent with Monte-Carlo policy-search algorithm.
    */
    public void updateDisplay(MC_PolicySearch_Hamster mcpsHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateQMarker(mcpsHamster,i,j);
                //updateValue(mcpsHamster,i,j);
                //updateCounter(mcpsHamster,i,j);
                updatePolicyMarker(mcpsHamster,i,j);
            }
        }
    } 
    
   /**
    * Updates the display of the state value objects in the territory according to V(s).
    * @param mcHamster Hamster agent with Monte Carlo evaluation.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
    public void updateValue(MC_Hamster mcHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+mcHamster.getGrainsInJaws()+"]";    
        Double val = mcHamster.getV(s_key);
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
    * Updates the visit count display for one state in the grid.
    * @param mcHamster Hamster-Agent with Monte-Carlo Evaluation.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
    public void updateCounter(MC_Hamster mcHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)) return;
        String s_key="["+x+","+y+","+mcHamster.getGrainsInJaws()+"]";    
        List <Visitcounter> cObjects = this.getObjectsAt(x,y,Visitcounter.class);
        Visitcounter co = null;
        if ( cObjects.size()>0 ) {
            co = ((Visitcounter)cObjects.get(0));                          
        }else{
            co = new Visitcounter(0);
            super.addObject(co,x,y);
        }
        Integer c = mcHamster.getN(s_key);
        co.setC(c);
    }
    
    /**
     * Removes all tracemarkers.
     */
    public void removeTracemarkers()
    {
        List objects = getObjects(Tracemarker.class);
        removeObjects(objects);
    }
    
    /**
     * Puts a tracemarker to the specified tile.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @param a action
     * @param e intensity (1.0 for black and 0.0 for invisible)
     */
    public void putTracemarker( int x, int y, int a, double e)
    {
        if (!this.isStatePossible(x,y)||this.isTerminal(x,y)) return;
         super.addObject(new Tracemarker(e,a),x,y);
    }
    
   /**
    * Updates the display of the state value objects in the territory according to V(s).
    * @param mcHamster Hamster agent with Monte Carlo evaluation.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
 /*   public void updateValue(MC_PolicySearch_Hamster mcpsHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)||isTerminal(x,y)) return;
        String s_key="["+x+","+y+","+mcpsHamster.getGrainsInJaws()+"]";    
        Double val = mcpsHamster.maxQ(s_key);
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
    }  */
    
    /**
     * Updates the objects that display the Q-values for a state.( Q(s,a) )
     * @param mcpsHamster Hamster agent with Monte-Carlo policy-search algorithm.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updateQMarker(MC_PolicySearch_Hamster mcpsHamster, int x, int y)
    {
         if (!isStatePossible(x,y)||isTerminal(x,y)) return;
         String s_key="["+x+","+y+","+mcpsHamster.getGrainsInJaws()+"]";    
         Double[] qValues = mcpsHamster.getQValues(s_key);
         if (qValues!=null){
             for (int a=0;a<qValues.length;a++){
                 Double qv=qValues[a];
                 List <QValueMarker> qValueMarkerObjects = this.getObjectsAt(x,y,QValueMarker.class);
                 boolean update = false;
                 for (QValueMarker qvm : qValueMarkerObjects){
                     int a_qvm = qvm.getA();
                     if (a_qvm==a){
                         if (qv!=qvm.getQ()){
                             qvm.setQ(qv);
                         }
                         update = true;
                         break;
                     }
                 }
                 if (update==false){
                     addObject(new QValueMarker(qv,a),x,y);
                 }        
             }
         }
    }
    
    /**
     * Updates the objects that display the policy in the territory.
     * @param mcpsHamster Hamster agent with Monte-Carlo policy-search algorithm.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updatePolicyMarker(MC_PolicySearch_Hamster mcpsHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)||isTerminal(x,y)) return;
        String s_key="["+x+","+y+","+mcpsHamster.getGrainsInJaws()+"]"; 
        double[] pi_s = mcpsHamster.getPi(s_key);
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