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
public class PolicySearch_Environment extends RL_GridEnv_FV
{
    public static int EVALUATION_INTERVAL = 1; // interval for making an average and displaying a result.

    public static boolean DO_EVALUATION_PHASE = false; // show the best known path
    public static boolean DISPLAY_AGENT = true;  // switch off for speed up
    public static boolean DISPLAY_UPDATE = true; // switch off for speed up
    public static int DISPLAY_UPDATE_INTERVAL = 1; // interval for making a update of the marker that visualize 
                                                      // data structures (N,V,Policy, ...) on the greenfoot screen.
                                                      // (increase for speedup)
    public final static int MAX_NUMBER_EPISODES = 1000; // maximum number of learning episodes
    
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";

    protected long sim_time = 0;
    protected long nano_starttime = 0;
    
    /**
     * Constructor of this environment.
     */
    public PolicySearch_Environment()
    {
        //super(mapDynaMaze);
        //super(mapFlat);
        super(mapFeatureFlat1);
        //super(mapWithTrap1); 
        //super(mapFrozenLake);
        //super(mapLabyrinth1);
        //super(mapSilver);

        //hamster = new ActorCritic_Hamster_FV();
        //hamster = new AC_DynaHamster_FV();
      
        //hamster = new QHamster_FV();
        //hamster = new QHamster_CM_FV();
  
        //hamster = new SarsaHamster_FV();
        hamster = new SarsaHamster_CM_FV();
        
   
        //hamster = new DynaHamster_FV();
   
        
        this.addObject(hamster,getHamsterStartX(),getHamsterStartY()); 
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
    }
    
    /**
     * This initializes the data logger depending on the selected agent class.
     */
    private void initJfxLogger(){
        if (hamster==null){
            System.out.println("[env.initJfxLogger()] No hamster agent intanciated !");
            return;
        }
        
        Class agent_class = hamster.getClass();
        String sAgentClass = agent_class.toString();
        sAgentClass=sAgentClass.replaceFirst("class ", "");
        jfxLogger = new JfxChartLogger(logFilePath+sAgentClass+"_","Efficiency of the "+sAgentClass+"-policy",sAgentClass+"-reward","episodes","collected reward");
        jfxLogger.appendln("environment attributes:");
        jfxLogger.appendln("reward for a grain;"+rewardForAGrain);
        jfxLogger.appendln("reward trap;"+rewardTrap);
        jfxLogger.appendln("transition effort;"+rewardPerTransition);
        jfxLogger.appendln("arena:");
        for (String line : fieldDescription) jfxLogger.appendln(line);
        
        agent_class = hamster.getClass();
              
        jfxLogger.appendln("interval;"+EVALUATION_INTERVAL);
        jfxLogger.append("start;");
        jfxLogger.appendTimeStamp();
        jfxLogger.append("\n");   
    }
    
    /**
     * Updates the display of the state value objects in the territory according to V(s).
     * @param pgHamster hamster agent with policy gradient algorithm
     */
    public void updateDisplay(ActorCritic_Hamster_FV acfHamster)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateValue(acfHamster,i,j);
                updatePolicyMarker(acfHamster,i,j);
            }
        }
        displayObservationArea();
    }
    
    /**
     * Updates the display of the state value objects in the territory.
     * @param qHamster Hamster agent with Q-algorithm.
     */
    public void updateDisplay(QHamster_FV qHamster_FV)
    {
        if (!DISPLAY_UPDATE) return;
        int worldWidth = getWidth();
        int worldHeight = getHeight();  
        for ( int i=0;i<worldWidth;i++ ){
            for( int j=0;j<worldHeight;j++ ){
                updateQMarker(qHamster_FV,i,j);
                updateValue(qHamster_FV,i,j);
                updatePolicyMarker(qHamster_FV,i,j);
            }
        }
         displayObservationArea();
    }
  
    /**
     * Updates the visit count display for one state in the grid.
     * @param pgHamster agent with policy gradient algorithm
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updateCounter(ActorCritic_Hamster_FV acHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)) return;
        String xs_key=produceFeatureKey(x,y);    
        List <Visitcounter> cObjects = this.getObjectsAt(x,y,Visitcounter.class);
        Visitcounter co = null;
        if ( cObjects.size()>0 ) {
            co = ((Visitcounter)cObjects.get(0));                          
        }else{
            co = new Visitcounter(0);
            super.addObject(co,x,y);
        }
        Integer c = acHamster.getN(xs_key);
        co.setC(c);
    }
    
    /**
     * Updates the display of the state value objects in the territory according to V(s).
     * @param pgHamster agent with policy gradient algorithm
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updateValue(ActorCritic_Hamster_FV pgHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)) return;
        String xs_key=produceFeatureKey(x,y);  
        Double val = pgHamster.getV(xs_key);
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
     * Updates the objects that display the policy in the territory for the specific type of agent.
     * @param acfHamster agent with policy gradient algorithm (actor-critic with feature vectors)
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updatePolicyMarker(RL_GridWorldAgent_FV rlHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)) return;
        String xs_key=produceFeatureKey(x,y);
        double[] pi_s = rlHamster.P_PolicyVisualization(xs_key,x,y);
        if (pi_s!=null){
            List <Policymarker> objects = getObjectsAt(x,y,Policymarker.class);
            removeObjects(objects);
            for (int a=0;a<pi_s.length;a++){
                boolean update = false;
                addObject(new Policymarker(pi_s[a],a),x,y);
            }
        }
    }
    
    /**
    * Updates the display of the state value objects in the territory according to V(s) ( V(s) = maxQ ).
    * @param qHamster Hamster-agent with Q-Learning.
    * @param x X-component of the state (column).
    * @param y Y-component of the state (row).
    */
    public void updateValue(QHamster_FV qHamster, int x, int y)
    {
        if (!this.isStatePossible(x,y)) return;
        String s_key=produceFeatureKey(x,y);  
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
     * Updates the objects that display the Q-values for a state.( Q(s,a) )
     * @param mcpsHamster Hamster agent with Monte-Carlo policy-search algorithm.
     * @param x X-component of the state (column).
     * @param y Y-component of the state (row).
     */
    public void updateQMarker(QHamster_FV qHamster_FV, int x, int y)
    {
         if (!this.isStatePossible(x,y)||isTerminal(x,y)) return;
         String s_key=produceFeatureKey(x,y);    
         Double[] qValues = qHamster_FV.getQValues(s_key);
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
     * Constructs a observation for the given world position.
     * @param x column
     * @param y row
     * @return vector with the observation
     */
    public String produceFeatureKey(int x, int y){
        if (isTerminal(x,y)) return "[T]";
        String xs_key="[";    
        for (int k=0;k<observationArea.length;k++){
            int neighborX = x+observationArea[k][0];
            int neighborY = y+observationArea[k][1];   
            char feature = getMapElementAt(neighborX,neighborY);
            switch (feature) {
                case 'M': xs_key+="[1,0,0,0,0]";
                      break;
                case 'N': xs_key+="[0,1,0,0,0]";
                      break;
                case 'O': xs_key+="[0,0,1,0,0]";
                      break;
                case 'P': xs_key+="[0,0,0,1,0]";
                      break;
                case 'Q': xs_key+="[0,0,0,0,1]";
                      break;
                default : xs_key+="[0,0,0,0,0]";
            }
            if (k==observationArea.length-1){
                xs_key+=']';
            }else{
                xs_key+=',';
            }
        }
        return xs_key;
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
     * Remove all policy marker.
     */ 
    public void removePolicymarker()
    {
        List objects = getObjects(Policymarker.class);
        removeObjects(objects);
    }
    
    public void displayObservationArea(){
        List objects = getObjects(TileMarker.class);
        removeObjects(objects);
        int x = hamster.getX(); int y = hamster.getY();
        for (int k=0;k<observationArea.length;k++){
            int neighborX = x+observationArea[k][0];
            int neighborY = y+observationArea[k][1]; 
            addObject(new TileMarker(50),neighborX,neighborY);
        }
    }
}