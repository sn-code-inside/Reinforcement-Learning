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
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.util.TransferFunctionType;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.core.learning.LearningRule;

/**
 * MountainCar with Semigradient Sarsa and MMLPs.
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
public class RL_MountainCar extends MountainCar implements LearningEventListener 
{    
    protected double ETA = 0.05;     // learning rate (step size)
    protected double GAMMA = 0.9999;  // discount factor
    protected double EPSILON = 0.05; // Startrate of "exploratory behavior" in epsilon-greedy action selection.
   
    protected final double minQValue = 0.00001; // Minimum amount for Q(s,a). Should help against side effects due to rounding or truncated decimal places. 
    
    protected final double maximumQ = RL_MountainEnv.REWARD_GOAL;
    protected final double minimumQ = -RL_MountainEnv.MAX_LENGTH_EPISODE;

    protected final static int SIZE_OF_ACTIONSPACE = 3;
    protected final static int DIM_input = State.getVectorSize();
    protected final static int DIM_output = 1;
    
    protected double[] minima;
    protected double[] maxima;
   
    protected RL_MountainEnv env = null;
    
    protected int a_t = MountainCar.MOTOR_default;
    protected double r_new = 0;
    protected double rewardSum=0;
    protected State s_t = null;

    int[] layersMLP = {DIM_input,DIM_input/3, DIM_output};
    protected double learningRate = 0.05;
    protected double maxError = 0.0001;
    protected int maxIterations = 50;
    protected int cnt_trainingEpoch = 0;

    NeuralNetwork[] neuralNetwork = new MultiLayerPerceptron[SIZE_OF_ACTIONSPACE];     
    int cnt_MLP_trainingEpoch = 0;
   
    protected DataSet[] trainingSet = new DataSet[SIZE_OF_ACTIONSPACE]; 
    protected int[] cnt_Qupdates = new int[SIZE_OF_ACTIONSPACE];
    protected int mini_batch_size = 2;
   
    protected int cnt_steps=0;
    protected int max_steps = RL_MountainEnv.MAX_LENGTH_EPISODE;
    protected int cnt_episodes = 0;
    protected int max_episodes =RL_MountainEnv.MAX_NUMBER_EPISODES;
    
    protected double delta_epsilon = -EPSILON/max_episodes; // This reduces epsilon during learning (0.0 holds epsilon constant).
    protected double current_epsilon = EPSILON;   // current value of epsilon
  
    public static final NumberFormat console_nf = NumberFormat.getInstance();
    protected static Random random = new Random(); 
    
    protected LinkedList <Sarsa> episode = new LinkedList <Sarsa>(); // episode record
      
    public RL_MountainCar()
    {
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
            
            trainingSet[i] = new  DataSet(DIM_input, DIM_output);
        }      
    }
    
    public void addedToWorld(World world) 
    {
        super.addedToWorld(world);
        berg = (RL_MountainEnv)world; 
        env = (RL_MountainEnv)world;
        
        minima=new double[]{MountainModel.leftBorder,V_min};
        maxima=new double[]{MountainModel.rightBorder,V_max};
        State.setMinMax(minima,maxima);
        
        setToStart();
    }
    
    public void act()
    {
        //processKeys();
        if (s_t==null){
            s_t = getState();
            List A_s = env.coursesOfAction(s_t);
            double[] P = this.P_Policy(SIZE_OF_ACTIONSPACE,A_s,s_t);
            a_t = selectAccordingToDistribution(P);
        }
        setAction(a_t);
        sim_step();
        
        r_new = env.getReward(this);
        rewardSum += r_new;
        State s_new = getState(); 
        
        boolean episodeEnd = false;
        int a_new = -1;
        if ((env.isTerminal(this)) ||(cnt_steps>max_steps)) {  
            episodeEnd = true;
        }else{
            List A_s = env.coursesOfAction(s_new);
            double[] P = this.P_Policy(SIZE_OF_ACTIONSPACE,A_s,s_new);
            a_new = selectAccordingToDistribution(P);
        }
        
        update(s_t,a_t,r_new,s_new,a_new,episodeEnd);      // for immediate update, without batch mechanism
        //env.updateDisplay(this); 
        env.setMonitorText("episode="+cnt_episodes+" t="+cnt_steps+" s_t = "+getState());
        
        if (episodeEnd){
            /* semigradient sarsa updates with shuffled batch
            while (!episode.isEmpty()){
                Sarsa e=episode.remove(random.nextInt(episode.size()));
                update(e.s,e.action,e.reward,e.s_new,e.action_new,e.episodeEnd);      
            }*/
            startNewEpisode();    
        }else{
            s_t=s_new; a_t=a_new;
        }
    }
    
    protected void sim_step()
    {
        super.sim_step();
        cnt_steps++;
    }
 
    /**
     * Sarsa update of the approximation of Q(s,a)
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
            observation = reward+1;    
        } else {  // Gets approx. Q from MLP
            observation = reward + (GAMMA * getQ(s_new, a_new));  
        }
        addQ_toMLP(s, a, observation);   
    } 
    
    protected double getQ(State s, int a) 
    {
        if ((a<0)||(a>SIZE_OF_ACTIONSPACE)) {
            System.out.println("Warning [getQ]: action a="+a);
            return 0;
        }
        double[] input = s.getFeatureVector();
        neuralNetwork[a].setInput(input);
        neuralNetwork[a].calculate();
        double[] output = neuralNetwork[a].getOutput();
        return State.restoreValue01(output[0], minimumQ, maximumQ);
    }
    
    /**
     * Adjusts the MLP belonging to a.
     * @param s State
     * @param a action
     * @param observation observation (target) 
     */
  /*  private void setQ_MLP(Zustand z, int a, double beobachtung) 
    { 
        double[] input = z.holeWerteArrayNormiert(minima,maxima); 
        DataSet trainingSet = new  DataSet(DIM_input, DIM_output); 
        double[] soll_output = new double[DIM_output];
        soll_output[0]= beobachtung;
        trainingSet.add(new DataSetRow(input, soll_output));
        neuralNetwork[a].learn(trainingSet);
    }*/
    protected void setQ_toMLP(State s, int a, double observation) 
    { 
        double[] input = s.getFeatureVector(); 
        double[] target_output = new double[DIM_output];
        target_output[0]= State.range01(observation,minimumQ,maximumQ);
        DataSet trainingSet = new  DataSet(DIM_input, DIM_output);
        trainingSet.add(new DataSetRow(input, target_output));
        neuralNetwork[a].learn(trainingSet);
    }
    
    /**
     * Adds a record for state-value function to batch.
     * @param state features 
     * @param observation Observed evaluation of the state
     */
    protected void addQ_toMLP(State s, int a, double observation) 
    { 
        double[] input = s.getFeatureVector(); 
        double[] target_output = new double[DIM_output];
        target_output[0]= State.range01(observation,minimumQ,maximumQ);
        trainingSet[a].add(random.nextInt(trainingSet[a].size()+1),new DataSetRow(input, target_output));
        cnt_Qupdates[a]++;
        if ((cnt_Qupdates[a]>=mini_batch_size)){
            neuralNetwork[a].learn(trainingSet[a]);
            trainingSet[a].clear();
            cnt_Qupdates[a]=0;
        }
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
     * A new episode is started. Update logging, reset counters and set
     * agent to start position. Perform an evaluation period if necessary.
     */
    protected void startNewEpisode() {
        cnt_episodes++;
        if (cnt_episodes%RL_MountainEnv.EVALUATION_INTERVAL==0){
            env.jfxLogger.append(cnt_episodes,rewardSum/RL_MountainEnv.EVALUATION_INTERVAL);
            rewardSum=0;
        }
        
        //if (cnt_episodes>=400) EPSILON=0;
        if (cnt_episodes>=max_episodes) Greenfoot.stop();
        
       /* reset networks 
          if (cnt_steps>=max_steps){
            for (int i=0;i<neuralNetwork.length;i++){
                neuralNetwork[i].randomizeWeights();
            }
        }*/
        
        current_epsilon+=this.delta_epsilon; // reduces epsilon during learning
        episode.clear();
        cnt_steps=0;
        r_new=0;
        a_t=0;
        setToStart();
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
    
    public State getState(){
        return new State(new double[]{x_position,geschwindigkeit} );
    }
    
    public int getEpisode(){
        return this.cnt_episodes;
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
    
    @Override
    public void handleLearningEvent(LearningEvent event) {
        BackPropagation lr = (BackPropagation)event.getSource();
        if (event.getEventType()==LearningEvent.Type.EPOCH_ENDED){
            //if (cnt_MLP_trainingEpoch%dispInterval==0) System.out.println("MLP-Trainingsepoche:"+cnt_MLP_trainingsEpoche+" "+lr.getNeuralNetwork().getLabel()+":"+lr.getTotalNetworkError());
            cnt_MLP_trainingEpoch ++;
        }
    }
    
    public int[] getLayerSizesMLP(){
        return layersMLP;
    }
    
    public double getLearningRate(){
        return learningRate;
    }
    
}
