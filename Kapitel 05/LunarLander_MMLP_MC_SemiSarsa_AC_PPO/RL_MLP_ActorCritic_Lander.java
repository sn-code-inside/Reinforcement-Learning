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
 * Lunar lander with ActorCritic and MLPs for state valuation and action preferences.
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
public class RL_MLP_ActorCritic_Lander extends RL_SemiGradientSarsa_Lander implements LearningEventListener 
{
    public double ETA_V = 0.2;    
    public double ETA_theta = 0.1;
    public double GAMMA = 0.99999; 
    public double T=1; // softmax temperature
    
    protected double I_gamma =1 ;
   
    protected final double maximumTheta= 10;   // upper bound for theta
    protected final double minimumTheta = -10; // lower bound for theta
      
    protected final int dimInput_V = State.VECTORSIZE;
    protected final int dimOutput_V = 1;
    protected int[] layersMLP_V = {dimInput_V,dimInput_V/3,dimOutput_V};
    protected NeuralNetwork network_V = null; 
    protected DataSet trainingSet_V = new  DataSet(dimInput_V, dimOutput_V); 
   
    protected final int dimInput_h = State.VECTORSIZE; 
    protected final int dimOutput_h = SIZE_OF_ACTIONSPACE;
    protected int[] layersMLP_h = {dimInput_h,dimInput_h/2,dimOutput_h};
    protected NeuralNetwork network_h = null;
    protected DataSet trainingSet_h = new  DataSet(dimInput_h,dimOutput_h);
    
    protected double delta_T = 0; //-(0.99*T)/max_episodes; // This reduces T during learning "annealing", (0.0 holds epsilon constant).
    protected double current_T = T;   // current value of T
    
    protected int mini_batch_size = 4;
    protected int cnt_updates = 0;
    
    protected int cnt_Vupdates = 0;
    protected int cnt_Hupdates = 0;
      
    public RL_MLP_ActorCritic_Lander() {
        super();
        episode = new LinkedList <Sarsa> ();
        
        learningRate = 0.02;
        maxIterations = 100;
        maxError = 0.00001;
        
        network_V= new MultiLayerPerceptron(TransferFunctionType.SIGMOID, layersMLP_V);
        network_V.setLabel("V(s)");
        network_V.randomizeWeights(-0.001,0.001);
        BackPropagation learningRule = (BackPropagation)network_V.getLearningRule();
        learningRule.addListener(this);
        learningRule.setLearningRate(learningRate);
        learningRule.setMaxError(maxError);
        learningRule.setMaxIterations(maxIterations);
        
        double learningRate_h = 0.02;
        int maxIterations_h = 100;
        double maxError_h = 0.00001;
       
        network_h = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, layersMLP_h);
        network_h.setLabel("h(xs,a)");
        network_h.randomizeWeights(-0.001,0.001);
            
        learningRule = (BackPropagation)network_h.getLearningRule();
        learningRule.addListener(this);
        learningRule.setLearningRate(learningRate_h);
        learningRule.setMaxError(maxError_h);
        learningRule.setMaxIterations(maxIterations_h);
        
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
            episodeEnd = true;
        }else{ 
            double[] P = this.P_Policy(s_new);
            a_new = selectAccordingToDistribution(P);
        }
        episode.add(new Sarsa(s_t,a_t,r_new,s_new,a_new,episodeEnd));
        
        env.updateDisplay(this); 
        
        update(s_t,a_t,r_new,s_new,episodeEnd);      
        if (episodeEnd){
            startNewEpisode();    
        }else{
            s_t=s_new; a_t=a_new;
        }
    }  
    
    protected void startNewEpisode() {
        cnt_episodes++;
        if (cnt_episodes%RL_MoonEnv.EVALUATION_INTERVAL==0){
            env.jfxLogger.append(cnt_episodes,rewardSum/RL_MoonEnv.EVALUATION_INTERVAL);
            System.out.println("episode="+cnt_episodes+
                               "  steps="+cnt_steps+
                               "  avg reward="+console_nf.format(rewardSum/RL_MoonEnv.EVALUATION_INTERVAL)+
                               " (landing speed="+console_nf.format(Math.abs((double)speed))+")"
                               );
            rewardSum=0;
        }
        
        if (cnt_episodes>=max_episodes) Greenfoot.stop();
        
        current_epsilon+=this.delta_epsilon; // reduces epsilon during learning
        episode.clear();
        I_gamma=1;
        cnt_steps=0;
        r_new=0;
        a_t=0;
        landerOnStart();
    } 

    /**
     * Actor-critic Update
     * @param s state
     * @param a action
     * @param reward Reward
     * @param s_new Successor state
     * @param end Has a terminal state or the step limit been reached?
     */
    protected void update( State s, int a, double reward, State s_new, boolean episodeEnd ) 
    { 
        double[] x_s = s.getFeatureVector();
  
        double observation = 0.0; 
        if (episodeEnd) {
            observation = reward;    
        }
        else {
            observation = reward + (GAMMA * getV(s_new));  
        } 
        
        // TD-error
        double V_is = getV(s);
        double delta=observation-V_is;
      
        // Update "critic"
        setV_MLP(s,observation);
        
        // Update "actor"
        double[] pi_sa = new double[SIZE_OF_ACTIONSPACE];     
        pi_sa = P_Policy(s);
        List <Integer> A_s = env.coursesOfAction(s);
        double[] h = new double[SIZE_OF_ACTIONSPACE];
        h = get_h(x_s);
        double gradient_ai = 0;
        for (int a_i : A_s){
            gradient_ai=-pi_sa[a_i];
            if (a_i==a) gradient_ai=gradient_ai+1;
            h[a_i] += ETA_theta*I_gamma*delta*gradient_ai;
           
        }
        setH_MLP(x_s,h);

        I_gamma = GAMMA*I_gamma;
        
                // batch update
     /*   cnt_updates++;
        if ((cnt_updates%mini_batch_size==0)){
             network_h.learn(trainingSet_h);
             trainingSet_h.clear();
             network_V.learn(trainingSet_V);
             trainingSet_V.clear();
             cnt_updates=0;
        } */
    } 
   
    /**
     * Stochastic policy of the agent. Assigns a probability distribution to a state over 
     * the set of possible actions.
     * @param xs_key state key
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_Policy(State s)
    {
        // if (env.isTerminal(this)) return new double[SIZE_OF_ACTIONSPACE];
        List <Integer> A_s = env.coursesOfAction(s);
        double[] retP = P_SoftMax(A_s,s);
        return retP;
    }
    
    /**
     * Assigns a probability distribution to a feature vector over the set of possible actions
     * according to softmax action selection strategy.
     * @param n number of sucessor states
     * @param A_s List of action options available to the agent at the given time in s.
     * @param s_key key for given state s
     * @return probability distribution for actions a in [0,1,...,n-1].
     */
    public double[] P_SoftMax(List <Integer> A_s, State s )
    {
        //double[][] x_sa = new double[A_s.size()][State.VECTORSIZE+SIZE_OF_ACTIONSPACE];        
        double[] x_s = s.getFeatureVector();
       /* int i=0;
        for (int a_i : A_s){
            x_sa[i]=get_x_sa(s.getFeatureVector(),a_i);
            i++;
        } */
        
        double[] P = new double[SIZE_OF_ACTIONSPACE];
        Arrays.fill(P,0.0);
        double sum = 0; int i=0;
        for (int a_i : A_s){
            double h = this.actionpreference(x_s, a_i);
            sum+=Math.exp(h/T);
            i++;
        }
        i=0;
        for (int a_i : A_s){
            double h = this.actionpreference(x_s, a_i);
            P[a_i]=Math.exp(h/T)/sum;
            i++;
        }
        return P;
    }
    
    /**
     * Calculates h(x(s,a),theta)
     * @param x_sa state-action feature vector
     * @return action preference
     */
    private double actionpreference(double[] x_s, int a){
        return get_h(x_s)[a];
    }
    
    /**
     * Gets value for state s from the estimator.
     * @param s state key
     * @return value for state s.
     */
    protected double getV(State s) 
    {
        double[] input = s.getFeatureVector();
        network_V.setInput(input);
        network_V.calculate();
        double[] output = network_V.getOutput();
        return State.restoreValue01(output[0], minimumQ, maximumQ);
    }
    
    /**
     * Sets a value for state s to the estimator.
     * @param s 
     * @param observation Observed evaluation of the state
     */
    protected void setV_MLP(State s, double observation) 
    { 
        double[] input = s.getFeatureVector(); 
        DataSet trainingSet = new  DataSet(dimInput_V, dimOutput_V); 
        double[] soll_output = new double[dimOutput_V];
        soll_output[0]= State.range01(observation,minimumQ,maximumQ);
        trainingSet.add(new DataSetRow(input, soll_output));
        network_V.learn(trainingSet);
    }
    
    /**
     * Adds a record for state-value function to batch.
     * @param state features 
     * @param observation Observed evaluation of the state
     */
    protected void addV_MLP(double[] input, double observation) 
    { 
        double[] soll_output = new double[dimOutput_V];
        soll_output[0]= State.range01(observation,minimumQ,maximumQ);
        trainingSet_V.add(random.nextInt(trainingSet_V.size()+1),new DataSetRow(input, soll_output));
        cnt_Vupdates++;
        if ((cnt_Vupdates>=mini_batch_size)){
            network_V.learn(trainingSet_V);
            trainingSet_V.clear();
            cnt_Vupdates=0;
        }
    }
    
    /**
     * Get action preference for given features.
     */
    protected double[] get_h(double[] x_s) 
    {
        network_h.setInput(x_s);
        network_h.calculate();
        double[] output = network_h.getOutput();
        output[0]=State.restoreValue01(output[0], minimumTheta, maximumTheta);
        output[1]=State.restoreValue01(output[1], minimumTheta, maximumTheta);
        return output;
    }
    
    protected void setH_MLP(double[] x_s, double[] h) 
    { 
        DataSet trainingSet = new  DataSet(dimInput_h,dimOutput_h); 
        double[] soll_h = new double[dimOutput_h];
        soll_h[0] = State.range01(h[0],minimumTheta,maximumTheta);
        soll_h[1] = State.range01(h[1],minimumTheta,maximumTheta);
        trainingSet.add(new DataSetRow(x_s, soll_h));
        network_h.learn(trainingSet);
    }
    
    protected void addH_MLP(double[] x_s, double[] h) 
    { 
        double[] soll_h = new double[h.length];
        for (int i=0;i<h.length;i++)
            soll_h[i] = State.range01(h[i],minimumTheta,maximumTheta);
            
        trainingSet_h.add(random.nextInt(trainingSet_h.size()+1),new DataSetRow(x_s, soll_h));
        cnt_Hupdates++;
        if ((cnt_Hupdates>=mini_batch_size)){
            network_h.learn(trainingSet_h);
            trainingSet_h.clear();
            cnt_Hupdates=0;
        }
    }
    
     public int[] getLayerSizesMLP_V(){
        return layersMLP_V;
    }
    
     public int[] getLayerSizesMLP_h(){
        return layersMLP_h;
    }

}
