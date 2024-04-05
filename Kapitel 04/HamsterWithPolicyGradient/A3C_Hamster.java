import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Thread.State;
import java.util.Arrays;

/**
 * A3C hamster
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
public class A3C_Hamster extends ActorCritic_Hamster implements Runnable
{
    protected static int lastID = 0;
    protected static ConcurrentHashMap <String, double[]> thetas_global; 
    protected static ConcurrentHashMap <String, Double> V_global;
    protected static ConcurrentHashMap <String, Integer> N_global = new ConcurrentHashMap <String, Integer>(); // counter at state s, N(s),  for visit statistics
    private static Thread workers[] = new Thread[PolicySearch_Environment.NUM_A3C_AGENTS];
    
    protected static int cnt_steps_global=0;
    protected static int cnt_episodes_global=0;
    protected static int max_episodes_global=250000;
    protected static double sum_reward_global=0.0;
    
    public int nsteps = 5;
    private int steps_start = 0;
    private int ID = -1;
    protected LinkedList <Experience> nstep_sequence = new LinkedList <Experience> ();
    
    
    boolean doWork = false;
    boolean episodeEnd = false;
    
    LinkedList <StateThetas> delta_theta = new LinkedList <StateThetas> ();
    LinkedList <StateV> delta_V = new LinkedList <StateV> ();
    
    public void stopWorker(){
        workers[ID].interrupt();
    }
    
    public A3C_Hamster()
    {
        super();
        if (PolicySearch_Environment.DISPLAY_AGENT)
            setImage("hamster_orange.png");
        else
            setImage((GreenfootImage)null);

        if (thetas_global==null) thetas_global=new ConcurrentHashMap <String, double[]> ();
        if (V_global==null) V_global=new ConcurrentHashMap <String, Double> ();
        this.ID = getNextID(); 
    }
    
    @Override
    public void addedToWorld(World world){
        super.addedToWorld(world);
        workers[ID] = new Thread(this,"A3C-hamster "+ID+" work thread");
        // workers[ID].setPriority(Thread.MAX_PRIORITY); // "unfriendly", but maybe a little bit faster.
        workers[ID].start();
        System.out.println(workers[ID].getName()+": Hallo. Let's do some reinforcement learning!");
    }
    
    /**
     * Gets the shared visit counter status at the state s.
     * @param s state key
     * @return counter status at state s.
     */
    protected int getN_global(String s)
    { 
        Integer c = N_global.get(s);
        if (c==null) return 0;
        return c;
    }
         
    /**
     * Increases shared visit counter at state s. N_global(s) := N_global(s)+1
     * @param s state key
     * @return new counter status at state s.
     */
    protected int incN_global(String s)
    {
        Integer c = N_global.get(s);
        if (c==null){
            c=1;
        }else{
            c++;
        }
        N_global.put(s,c);
        return c;
    }
    
    /** 
     * Returns a new ID number for the created agent.
     */
    public int getNextID(){
         lastID++;
         return (lastID-1);
    }
    
    @Override
    public void act() 
    {
        if (env==null) return;
        
        if (!doWork) {
            // update global parameters
            updateGlobalTheta();
            updateGlobalV();
            if (episodeEnd) startNewEpisode();
            doWork = true;
        }  
        
        if ((PolicySearch_Environment.DISPLAY_UPDATE) &&
            (cnt_steps_global%PolicySearch_Environment.DISPLAY_UPDATE_INTERVAL==0)) env.updateDisplay(this);
    }
    
    @Override 
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            if (doWork) {
                act_task();
                doWork=false;
            }
        } 
        System.out.println(workers[ID].getName()+": Bye, bye.");
    }
    
    public void act_task() {
        V=(ConcurrentHashMap <String, Double>)V_global;
        thetas=(ConcurrentHashMap <String, double[]>)thetas_global;
        String s = getState();
        nstep_sequence.clear();
        steps_start=cnt_steps;
        do{ // n steps foreward view
           // System.out.println(worker.getName()+" at "+s);
            incN_global(s);
            incN(s); 
            double[]P = P_Policy(s);
            int a = selectAccordingToDistribution(P);
            //apply transition model (consider uncertainties in the result of an action)
            int dir = transitUncertainty(a);
            // execute action a
            if (dir<4){   
                try{
                    setDirectionOfView(dir);
                    goAhead();
                }catch (Exception e){
                    //System.out.println("bump!");
                }   
                cnt_steps++;
                cnt_steps_global++;
            } 
            // get new state
            String s_new = getState();
            // get the reward from the environment
            double r_new = env.getReward(s_new);
            sum_reward+=r_new;
            // log sequence for n step update
            nstep_sequence.add(new Experience(s,a,r_new));
            s=s_new; 
        }while(!env.isTerminal(s)&&(cnt_steps-steps_start)<nsteps);
        episodeEnd = false;
        if (env.isTerminal(s)||(cnt_steps>=max_steps)){
           episodeEnd = true;
        }
        update(nstep_sequence,s,episodeEnd);
    }
    
    /**
     * Asynchronous n-step update. Affects only the "delta-lists".
     * @param nstep_sequence nstep foreward sequence
     * @param s last reached state
     * @param terminal episode end reached?
     */
    protected void update( LinkedList <Experience> nstep_sequence, String s, boolean episodeEnd ){
        if (nstep_sequence.isEmpty()) return;
        delta_theta.clear();
        delta_V.clear();
        Double R = 0.0;
        if (!episodeEnd){
            R=getV(s);
        }
        while (!nstep_sequence.isEmpty()){
            Experience e = nstep_sequence.removeLast();
            String s_i = e.getS();
            int a_i = e.getA();   
            R=e.getR()+GAMMA*R;
            double advantage = R-getV(s_i);            
            // accumulate gradients
            double[] pi_sa = P_Policy(s_i); 
            double[] d_theta_s = new double[SIZE_OF_ACTIONSPACE];
            double d_v=0.0;
            ArrayList <Integer> A_s = env.coursesOfAction(s_i);
            for (int b : A_s){
                double gradient_b=-pi_sa[b];
                if (b==a_i) gradient_b=gradient_b+1;     
                d_theta_s[b] = ETA_theta*gradient_b*advantage;
                d_v = ETA_V*advantage;
            } 
            delta_theta.add(new StateThetas(s_i,d_theta_s));
            delta_V.add(new StateV(s_i,d_v));
        }
    }
    
    /**
     * Creates the key for accessing the table for V(s) an theta(s) . If it does not exist, the corresponding record is created.
     * @param x column in the gridworld
     * @param y row in the gridworld
     * @param score collected grains
     * @return state key
     */
    @Override
    public String getStateKey(int x, int y, int score) 
    { 
        String s_key="["+x+","+y+","+score+"]";
      
        if (!thetas.containsKey(s_key)){
            double[] theta = new double[SIZE_OF_ACTIONSPACE];
            Arrays.fill(theta,0.0);
            thetas.put(s_key,theta);  
        } 
        if (!V.containsKey(s_key)){
             V.put(s_key, 0.0);
        }

        return s_key;
    } 
    
    /**
     * Stochastic policy of the agent. Assigns a probability distribution to a state over the set of possible actions.
     * @param s_key state key
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(String s_key)
    {
        List <Integer> A_s = env.coursesOfAction(s_key);
        double[] retP = P_SoftMax(SIZE_OF_ACTIONSPACE,A_s,s_key);
        return retP;
    }
   
    /**
     * Assigns a probability distribution to a state over the set of possible actions
     * according to softmax action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_SoftMax(int n, List <Integer> A_s, String s )
    {
        double[] P = new double[n];
        Arrays.fill(P,0.0);
        if (A_s==null) return P;
        double [] theta = getTheta(s);
        double sum = 0;
        for (int a_i : A_s){
            sum+=Math.exp(theta[a_i]/T);
        }
        for (int a_i : A_s){
            P[a_i]=Math.exp(theta[a_i]/T)/sum;
        }
        return P;
    }
    
    @Override
    protected void startNewEpisode()
    {
        sum_reward_global+=sum_reward;
        
        if (cnt_episodes_global%PolicySearch_Environment.EVALUATION_INTERVAL==0) {
            double avg_reward = sum_reward_global/PolicySearch_Environment.EVALUATION_INTERVAL;
            if (avg_reward>1.0) System.out.println("avg reward "+avg_reward+" bigger then max of 1.0 ??");
            env.jfxLogger.append(cnt_episodes_global,avg_reward," worker"+ID);
            sum_reward_global=0;
        }
        if (cnt_episodes_global>=max_episodes_global) Greenfoot.stop();
        sum_reward = 0;
        setLocation(env.getHamsterStartX(),env.getHamsterStartY());
        cnt_steps=0;
        cnt_episodes++;
        cnt_episodes_global++;
        Visitcounter.resetCMax();
        //N.clear();
    }
    
    public void updateGlobalTheta(){
        if (delta_theta.isEmpty()) return;
        for (StateThetas d : delta_theta){
            double[] theta_s = A3C_Hamster.thetas_global.get(d.s);
            if (theta_s==null) theta_s = new double[SIZE_OF_ACTIONSPACE];
            for (int i=0;i<theta_s.length;i++) theta_s[i]+=d.thetas[i];
            A3C_Hamster.thetas_global.put(d.s,theta_s);
        }
    }
    
    public void updateGlobalV(){
        if (delta_V.isEmpty()) return;
        for (StateV d : delta_V){
            Double v = A3C_Hamster.V_global.get(d.s);
            if (v==null) v=0.0;
            v+=d.v;
            A3C_Hamster.V_global.put(d.s,v);
        }
    }
    
    /**
     * Returns the ID of this agent.
     */
    public int getID(){
        return this.ID;
    }
    
    class StateThetas{
        public String s;
        public double[] thetas;
        public StateThetas(String s,double[] thetas){
            this.s=s;
            this.thetas=thetas;
        }
    }
    
    class StateV{
        public String s;
        public double v;
        public StateV(String s,double v){
            this.s=s;
            this.v=v;
        }
    }

    @Override
    protected Double getV(String s) 
    {
        Double R = null;
        do{
            R=V.get(s);
            if (R==null) {
                if (getN_global(s)==0) {
                    return 0.0; // unknown state
                }
                System.out.println("Conflict at step:"+this.cnt_steps_global+" "+s+": got V(s)==null ("+workers[ID].getName()+") but global visit count="+getN_global(s)+" I'll try access again."); 
            }
        }while(R==null); 
        return R; 
    }
    
    public double[] getTheta(String s_key){
        double[] ret = null;
        do{
            ret = thetas.get(s_key);
            if (ret==null){
                if (getN_global(s_key)==0) {
                    return new double[SIZE_OF_ACTIONSPACE]; // unknown state
                }
                System.out.println(JfxChartLogger.getTimeStamp()+" Conflict at "+s_key+": got theta(s)==null ("+workers[ID].getName()+") but global visit count="+getN_global(s_key)+" I'll try access again."); 
            }
        }while(ret==null); 
        return ret;
    }  
}
