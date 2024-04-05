import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot und MouseInfo)
import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.Perceptron;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.LearningRule;

/**
 * Lunar-Lander with Semigradient Sarsa and MMLPs for approximating the value function.
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
public class RL_SemiGradientSarsa_Lander extends Lander implements LearningEventListener 
{
    public double ETA = 0.1;      // learning rate (step size)
    public double GAMMA = 0.999;  // discount factor
    public double EPSILON = 0.05; // Startrate of "exploratory behavior" in epsilon-greedy action selection.
     
    protected final double minQValue = 0.00001;// Minimum amount for Q(s,a). Should help against side effects due to rounding or truncated decimal places. 
    
    protected final double maximumQ = 25;   // upper bound for Q values
    protected final double minimumQ = -100; // lower bound für Q values
    
    protected int SIZE_OF_ACTIONSPACE = 2;
    
    protected final static int DIM_input = State.VECTORSIZE; // partions of the continuous actionspace
    protected final static int DIM_output = 1;
    protected int[] layersMLP = {DIM_input,DIM_input/3, DIM_output};
    
    protected double learningRate = 0.05;
    protected double maxError = 0.0001;
    protected int maxIterations = 50;
    protected int cnt_trainingEpoch = 0;
   
    NeuralNetwork[] neuralNetwork = new MultiLayerPerceptron[SIZE_OF_ACTIONSPACE]; // array of neural networks
    int cnt_trainingsEpoche = 0;
       
    //private DataSet trainingData = null;
    protected int cnt_steps=0;
    protected int cnt_episodes=0;
    protected int max_episodes=RL_MoonEnv.MAX_NUMBER_EPISODES;
    
    protected double delta_epsilon = 0.0;       //-EPSILON/max_episodes; // This reduces epsilon during learning (0.0 holds epsilon constant).
    protected double current_epsilon = EPSILON;   // current value of epsilon
 
    protected double[] minima;
    protected double[] maxima;
   
    protected State s_t = null;
    protected int a_t = 0;
    protected double r_new = 0;
    protected double rewardSum=0;

    protected RL_MoonEnv env;    
    
    protected static Random random = new Random(); 
    public static final NumberFormat console_nf = NumberFormat.getInstance(); 
    
    protected DataSet[] trainingSet = new DataSet[SIZE_OF_ACTIONSPACE]; 
   
    protected LinkedList <Sarsa> episode = new LinkedList <Sarsa>(); 
    
    public RL_SemiGradientSarsa_Lander() {
        super();    
        for (int i=0;i<neuralNetwork.length;i++){
            neuralNetwork[i] = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, layersMLP);
            neuralNetwork[i].setLabel("Q(s,"+i+")");
            neuralNetwork[i].randomizeWeights(-0.001,0.001);
            
            BackPropagation learningRule = (BackPropagation)neuralNetwork[i].getLearningRule();
            learningRule.addListener(this);
            learningRule.setLearningRate(learningRate);
            learningRule.setMaxError(maxError);
            learningRule.setMaxIterations(maxIterations);
        }
        
       
    }
    
    public void initTrainigSets(int input_dim, int output_dim){
         // init trainingsdata batches
        for (int i=0;i<SIZE_OF_ACTIONSPACE;i++){
            
            trainingSet[i] = new  DataSet(input_dim, output_dim); 
            trainingSet[i].setLabel("MLP_"+i);
        }
    }
    
    /**
     * Lander has been added to the world.
     */
    public void addedToWorld(World world) 
    {
        moon = (RL_MoonEnv)world; 
        env = (RL_MoonEnv)world;
           
        ALTITUDE_MAX = world.getHeight();
        ALTITUDE_MIN = 0;
        SPEED_MAX=world.getHeight()/3;
        SPEED_MIN=-SPEED_MAX;
        
        minima=new double[]{ALTITUDE_MIN,SPEED_MIN,0.0};
        maxima=new double[]{ALTITUDE_MAX,SPEED_MAX,TANKVOLUMEN};
        State.setMinMax(minima,maxima);
        
        landerOnStart();
    }
    
    @Override
    public void act() 
    {
        if (s_t==null){
            s_t = getState();
            List A_s = env.coursesOfAction(s_t);
            double[] P = this.P_Policy(SIZE_OF_ACTIONSPACE,A_s,s_t);
            a_t = selectAccordingToDistribution(P);
        }
        setAction(a_t);
        sim_step();
        if (tankInhalt()==0){
            setAction(0);
            while (!env.isTerminal(this)){                
                sim_step();
            }
        }
        
        r_new = env.getReward(this);
        rewardSum += r_new;
        State s_new = getState(); 
        
        boolean episodeEnd = false;
        int a_new = -1;
        if (env.isTerminal(this)) {   
            System.out.println("episode "+cnt_episodes+
                               "  step="+cnt_steps+
                               "  reward="+console_nf.format(r_new)+
                               " (landing speed="+console_nf.format(Math.abs((double)speed))+")"
                               );
            episodeEnd = true;
        }else{ 
            List A_s = env.coursesOfAction(s_new);
            double[] P = this.P_Policy(SIZE_OF_ACTIONSPACE,A_s,s_new);
            a_new = selectAccordingToDistribution(P);
        }

        episode.add(new Sarsa(s_t,a_t,r_new,s_new,a_new,episodeEnd));
        env.updateDisplay(this);  
        
        if (episodeEnd){
            // semigradient sarsa updates with shuffled batch
            while (!episode.isEmpty()){
                Sarsa e=episode.remove(random.nextInt(episode.size()));
                update(e.s,e.action,e.reward,e.s_new,e.action_new,e.episodeEnd);      
            }
            startNewEpisode();    
        }else{
            s_t=s_new; a_t=a_new;
        }
    }    
    
    public void setAction(int action){
        triebwerkEin(action!=0);
    }

    protected void startNewEpisode() {
        cnt_episodes++;
        if (cnt_episodes%RL_MoonEnv.EVALUATION_INTERVAL==0){
            env.jfxLogger.append(cnt_episodes,rewardSum/RL_MoonEnv.EVALUATION_INTERVAL);
            rewardSum=0;
        }
        
        if (cnt_episodes>=max_episodes) Greenfoot.stop();
        
        current_epsilon+=this.delta_epsilon; // reduces epsilon during learning
        episode.clear();
        cnt_steps=0;
        r_new=0;
        a_t=0;
        landerOnStart();
    } 
    
    /** 
     * Adjusts the MLPs with the recorded training data.
     */
    protected void adjustMLPs()
    {
        for (int net_a=0;net_a<SIZE_OF_ACTIONSPACE;net_a++){  
            if (!trainingSet[net_a].isEmpty()) neuralNetwork[net_a].learn(trainingSet[net_a]);
        }
    }
    
    protected void trainingDataReset()
    {
      for (int i=0;i<SIZE_OF_ACTIONSPACE;i++){
          trainingSet[i].clear();
      }
    }
    
    protected void landerOnStart()
    {
        setLocation(env.getStartX(),env.getStartY());
        speed = 0;
        altitude=env.getStartY();
        addTreibstoff(TANKVOLUMEN);
    }
     
    /**
     * Assigns a probability distribution to a state over the set of possible actions
     * according to epsilon-greedy action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s state
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(int n, List <Integer> A_s, State s )
    {
        double[] P = new double[n];
        Arrays.fill(P,0.0);
        int a_max = getActionWithMaxQ(A_s,s);
        // epsilon-greedy otherwise
        int k = A_s.size();
        for (int a_i : A_s){
            P[a_i] =  current_epsilon/k;
        }
        P[a_max]+=(1-current_epsilon);  // For a_max add the probability 1-epsilon.
        return P;
    }
    
    /**
     * Gets the action with the largest Q value for a given state. If there are several Q_max actions
     * with the same value, they are selected randomly. If there are no Q-values, then -1 is returned.
     * @param s_key state key
     * @return Action with greatest Q-value stored for the state s. Null if state is unknown.
     */
    protected Integer getActionWithMaxQ(List <Integer> A_s, State s) 
    { 
        double maxQv = Double.NEGATIVE_INFINITY; 
        int a_max = -1;
        for (int action : A_s){ 
            double q = getQ(s,action);
            if (q>maxQv){
                maxQv=q;
                a_max=action;
            }
        }
        return a_max; 
    } 
   
    /**
     * Update of the approximation of Q(s,a)
     * @param s state
     * @param a action
     * @param reward Reward
     * @param s_new Successor state
     * @param a_new next action
     * @param end Has a terminal state or the step limit been reached?
     */
    protected void update( State s, int a, double reward, State s_new, int a_new, boolean episodeEnd ) 
    { 
        // Semigradient Sarsa
        double observation = 0.0; 
        if (episodeEnd) {
            observation = reward;    
        }
        else {  // Gets approx. Q from MLP
            observation = reward + (GAMMA * getQ(s_new, a_new));  
        }
        setQ_toMLP(s, a, observation);   
    } 
    
    /**
     * Gets the estimate of the Q value from the MLP associated with a using s.
     * @param s state
     * @param a action
     * @return approx. Q-value
     */
    protected double getQ(State s, int a) 
    {
        double[] input = s.getFeatureVector();
        neuralNetwork[a].setInput(input);
        neuralNetwork[a].calculate();
        double[] output = neuralNetwork[a].getOutput();
        return State.restoreValue01(output[0], minimumQ, maximumQ);//(output[0]*(maximumQ-minimumQ));
    }
    
    /**
     * Adjusts the MLP belonging to a.
     * @param s state
     * @param a action
     * @param observation target value
     */
    protected void setQ_toMLP(State s, int a, double observation) 
    { 
        double[] input = s.getFeatureVector(); 
        double[] target_output = new double[DIM_output];
        target_output[0]= State.range01(observation,minimumQ,maximumQ);
        DataSet trainingSet = new  DataSet(DIM_input, DIM_output);
        trainingSet.add(new DataSetRow(input, target_output));
        neuralNetwork[a].learn(trainingSet);
    }
   
    @Override
    public void handleLearningEvent(LearningEvent event) {
        BackPropagation lr = (BackPropagation)event.getSource();
        if (event.getEventType()==LearningEvent.Type.EPOCH_ENDED){
            cnt_trainingsEpoche++;
        }
    }
    
    public double maxQ(State s) 
    { 
        int a = getActionWithMaxQ(env.coursesOfAction(s),s);
        return getQ(s,a); 
    } 
    
    public State getState(){
        return new State(new double[]{altitude,speed,tankInhalt()} );
    }
    
    public int episode(){
        return this.cnt_episodes;
    }

    public double getReward(){
        return checkReward();
    }
    
    protected void sim_step()
    {
        super.sim_step();
        cnt_steps++;
    }
    
    /**
     * Calculates the reward from the agent's state.
     * @return reward
     */
    private double checkReward() 
    {
        double reward = 0;
        if (isLanding()||isExploding()){
            reward= MAX_LANDING_SPEED-((double)speed);
            if (reward==0) reward=0.1;
        }
        
     /*   if (isLanding()){
            System.out.println("LANDED!");
        } */
        
      
        // Bounds
        if (reward>maximumQ) reward = maximumQ;
        if (reward<minimumQ) reward = minimumQ;

        return reward;
    }
    
    public int[] getLayerSizesMLP(){
        return layersMLP;
    }
    
    public double getLearningRate(){
        return learningRate;
    }
        
    /**
     * Selection according to a given probability distribution P.
     * @param P probability distribution over a discrete set of options (maybe actions).
     * @return selected option, -1 if no selection (error) 
     */
    public int selectAccordingToDistribution(double P[])
    {
        double sum = 0;
        for (double p_i:P) sum+=p_i;
        if (sum>0){   
            double f = 1.0/sum; // normalize sum to 1
            int k=0;
            double e = random.nextDouble();
            double p=0.0;
            do{
                p+=f*P[k];
                if (e<p){
                    return k;
                }
                k++;
            }while(k<P.length);
        }
        return -1;
    }
}
