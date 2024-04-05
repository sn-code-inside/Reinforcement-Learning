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
 * Lunar lander with MMLPs for state estimation and MonteCarlo evaluation of episodes.
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
public class RL_MMLP_MC_Lander extends RL_SemiGradientSarsa_Lander implements LearningEventListener 
{
    private LinkedList <Sar> episode_sar; 

    public static final NumberFormat console_nf = NumberFormat.getInstance(); 
    
    public RL_MMLP_MC_Lander() {
        super();
        ETA = 0.2;    // Lernrate (Schrittweite) 
        GAMMA = 0.99999;  // Entfernungsdiscount
        EPSILON = 0.05; // Anteil "greedy" Verhaltens bei "Epsilon-greedy" Aktionsauswahl
        maxIterations = 50;
        maxError = 0.000001;
        episode_sar = new LinkedList <Sar> ();
         
        for (int i=0;i<neuralNetwork.length;i++){
            neuralNetwork[i] = new MultiLayerPerceptron(TransferFunctionType.SIGMOID, layersMLP);
            neuralNetwork[i].setLabel("Q(s,"+i+")");
            neuralNetwork[i].randomizeWeights();
            
            BackPropagation learningRule = (BackPropagation)neuralNetwork[i].getLearningRule();
            learningRule.addListener(this);
            learningRule.setLearningRate(learningRate);
            learningRule.setMaxError(maxError);
            learningRule.setMaxIterations(maxIterations);
        }
        initTrainigSets(DIM_input,DIM_output);
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
          /*  System.out.println("episode "+cnt_episodes+
                               "  step="+cnt_steps+
                               "  reward="+console_nf.format(r_new)+
                               " (landing speed="+console_nf.format(Math.abs((double)speed))+")"
                               );*/
            episodeEnd = true;
        }else{ 
            List A_s = env.coursesOfAction(s_new);
            double[] P = this.P_Policy(SIZE_OF_ACTIONSPACE,A_s,s_new);
            a_new = selectAccordingToDistribution(P);
        }

        if (a_t>=0) episode_sar.add(new Sar(s_t, a_t, r_new));
        env.updateDisplay(this);  
        
        if (episodeEnd){
            update(episode_sar);
            episode_sar.clear();
            startNewEpisode();            
        }else{
            s_t=s_new;  
            a_t=a_new;
        }
    } 


    /**
     * MonteCarlo update using the recorded episode. The elements of the episode are randomly drawed for processing.
     */
    protected void update(LinkedList <Sar> episode) 
    { 
        trainingDataReset();
        double G=0;
        while (!episode.isEmpty()){
            Sar e = episode.removeLast();
            State s_e = e.s;
            G=GAMMA*G+e.reward;
            double[] target_output = new double[DIM_output];
            target_output[0]=State.range01(G,minimumQ,maximumQ);
            int size = trainingSet[e.action].size()+1;
            trainingSet[e.action].add(random.nextInt(size),new DataSetRow(s_e.getFeatureVector(), target_output));
        }
        adjustMLPs();
    } 
    
}
