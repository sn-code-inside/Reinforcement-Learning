import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
import java.util.HashMap;

/**
 * An environment for "online" learning hamster agents that evaluate TD error for state assessment.
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
public class TD_AgentEnv extends RL_GridEnv
{
    public static RL_GridWorldAgent hamster = null;
    
    public static boolean DISPLAY_AGENT = true;       // switch off for speed up
    public static boolean DISPLAY_UPDATE = true;      // switch off for speed up
    public static int DISPLAY_UPDATE_INTERVAL = 1;    // steps interval for making a update of the markers that visualize data
                                                      //   structures (N,V,Policy, ...) on the greenfoot screen (increase for speedup).
    public static int EVALUATION_INTERVAL = 5;  // interval for making an average and displaying a result.
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
    
    public final static int MAX_NUMBER_EPISODES = 100000; // maximum number of learning episodes
    
    protected long nano_starttime = 0;  // contains simulation start time (nanoseconds)
    protected long sim_time = 0;        // contains simulation time in total (stops when greenfoot is paused)
    
    /**
     * Constructor for objects of class TD_AgentEnv.
     * 
     */
    public TD_AgentEnv()
    {
        /*# Here you can change the arena: */
        super(mapFlat); 
        //super(mapWithTrap2);
        //super(mapWindyCliff);
        //super(mapDynaMaze); 
        
        /*# Here you can change the type of the hamster agent: */
        hamster = new QHamster();
        //hamster = new SarsaHamster();
        //hamster = new SarsaLambdaHamster();
        //hamster = new DynaHamster();
        
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
        jfxLogger = new JfxChartLogger(logFilePath+sAgentClass+"_","Efficiency of the "+sAgentClass+"-policy","reward","episodes","collected reward");
        
        jfxLogger.appendln("environment attributes:");
        jfxLogger.appendln("reward for a grain;"+rewardForAGrain);
        jfxLogger.appendln("reward trap;"+rewardTrap);
        jfxLogger.appendln("transition effort;"+rewardPerTransition);
        jfxLogger.appendln("arena:");
        for (String line : fieldDescription) jfxLogger.appendln(line);
        
        jfxLogger.appendln("agent class;"+sAgentClass);
        if (agent_class==QHamster.class){
            jfxLogger.appendln("learning parameter:");  
            jfxLogger.appendln("GAMMA;"+((QHamster)hamster).GAMMA); 
            jfxLogger.appendln("EPSILON;"+((QHamster)hamster).EPSILON);
        }
 
        jfxLogger.append("start;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n"); 
    }
    
    /**
     * Updates the display of the state value objects in the territory.
     * @param qHamster Hamster agent with Q algorithm.
     */
    public void updateDisplay(QHamster qHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
               // updateCounter(qHamster,i,j);
                updateQMarker(qHamster,i,j);
                updateValue(qHamster,i,j);
            }
        }
    }
       
    /**
     * Updates the display of the state value objects in the territory.
     * @param slHamster Hamster agent with SARSA(lambda).
     */
    public void updateDisplay(SarsaLambdaHamster slHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateQMarker(slHamster,i,j);
                updateValue(slHamster,i,j);
                updatePolicymarker(slHamster,i,j);
            }
        }
    }
    
    /**
    * Updates the display of the state value objects in the territory according to Q(s,a) ( V(s) = maxQ ).
    * @param qHamster Hamster-agent with Q-Learning.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
    public void updateValue(QHamster qHamster, int x, int y)
    {
        String s_key="["+x+","+y+","+qHamster.getGrainsInJaws()+"]";    
        Double val = qHamster.maxQ(s_key);
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
    * @param qHamster Hamster-agent with Q-Learning.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
    public void updateCounter(QHamster qHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)) return;
        String s_key="["+x+","+y+","+qHamster.getGrainsInJaws()+"]";    
        List <Visitcounter> cObjects = this.getObjectsAt(x,y,Visitcounter.class);
        Visitcounter co = null;
        if ( cObjects.size()>0 ) {
            co = ((Visitcounter)cObjects.get(0));                          
        }else{
            co = new Visitcounter(0);
            super.addObject(co,x,y);
        }
        Integer c = qHamster.getN(s_key);
        co.setC(c);
    }
    
    /**
     * Updates the objects that display the Q-values for a state.( Q(s,a) )
     * @param qHamster agent with value based policy (tabular).
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updateQMarker(QHamster qHamster, int x, int y)
    {
         if (!this.isStatePossible(x,y)||isTerminal(x,y)) return;
         String s_key="["+x+","+y+","+qHamster.getGrainsInJaws()+"]";    
         Double[] qValues = qHamster.getQValues(s_key);
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
     * Updates the markers for displaying the eligibility trace.
     * @param sarsaLambdaHamster Sarsa Hamster-Agent mit Eignungspfad.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */ 
    public void updatePolicymarker(SarsaLambdaHamster sarsaLambdaHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)||isTerminal(x,y)) return;
        
        String s_key="["+x+","+y+","+sarsaLambdaHamster.getGrainsInJaws()+"]";    
        HashMap <Integer, Double> eValues = sarsaLambdaHamster.getEValues(s_key);
        if (eValues!=null) {
            for (HashMap.Entry <Integer,Double> valItem : eValues.entrySet()) {
                Integer a = valItem.getKey();
                Double ev = valItem.getValue(); // Eignungswert
                List <Policymarker> objects = getObjectsAt(x,y,Policymarker.class);
                boolean update = false;
                for (Policymarker tm : objects) {
                    int a_tm = tm.getA();
                    if (a_tm==a){
                        tm.setE(ev);
                        update = true;
                        break;
                    }
                }
                if (update==false) {
                    addObject(new Policymarker(ev,a),x,y);
                }
            }
        }
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
     * Removes all policymarkers.
     */
    public void removePolicymarkers()
    {
        List objects = getObjects(Policymarker.class);
        removeObjects(objects);
    }
    
    /**
     * Puts a tracemarker to the specified tile.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @param a action
     * @param e intensity (1.0 for most intensive and 0.0 for invisible)
     */
    public void putTracemarker(int x, int y, int a, double e)
    {
        if (!this.isStatePossible(x,y)||this.isTerminal(x,y)) return;
         super.addObject(new Tracemarker(e,a),x,y);
    }
    
    /**
     * Puts a policymarker to the specified tile.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     * @param a action
     * @param e intensity (1.0 for black and 0.0 for invisible)
     */
    public void putPolicymarker(int x, int y, int a, double e)
    {
        if (!this.isStatePossible(x,y)||this.isTerminal(x,y)) return;
         super.addObject(new Policymarker(e,a),x,y);
    }
}
