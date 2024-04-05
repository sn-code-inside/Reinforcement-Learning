import greenfoot.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.*;
import java.sql.Timestamp;

/**
 * Q-Learning TicTacToe agent in Greenfoot.
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
public class QLearning extends TicTacToe_Agent 
{ 
    public static final double GAMMA = 0.9;
    protected double ETA = 0.3;
    protected double EPSILON = 0.0; //0.1;
    
    // Wir geben für ein "Unentschieden" eine Belohnung, da sonst
    // kein Trainingseffekt gegen einen optimalen Spieler vorhanden ist.
    protected static final double belohnungFuerUnentschieden = TicTacToe_Env.REWARD_WIN/2; 
    
    protected Random random = new Random();
    protected Map<Integer, HashMap<Integer, Double>> Q; 
    
    // Anzahl zu spielender Episoden
    protected static final int numberTraininggames = 2500; 
    protected static final int lengthTaininginterval = 5;
    protected static final int lengthEvaluationphase = 1000;
    
    // Gegner in Lernenv
    public final static String algorithm_trainingOpponent = "self";         // Algorithm for the opponent on which this algorithm learns.
    public final static String algorithm_evaluationOpponent = "Arbitrary";  // Algorithm for the opponent on which this algorithm is to be tested.
   
    // Counter
    protected int cnt_gamesPlayed = 0;
    protected int cnt_won = 0;
    protected int cnt_lost = 0;
    protected int cnt_draw = 0;
    protected int cnt_testgames = 0;
    protected int cnt_games_x = 0;
    protected int cnt_games_o = 0;
    
    protected TicTacToe_Agent tttTrainingOpponent = null; 
    protected TicTacToe_Agent tttTestOpponent = null;
    
    protected boolean testphase = false;
    protected char currentPlayer = 'x';
    
    protected String QlogFilePath = "data\\";
   
    /**
     * Produces a TicTacToe agent with Q-Learning and runs a training session.
     * @param player Zeichen des Spielers für den der Agente erzeugt wird.
     * @param env TicTacToe-Umgebung
     * @param ident Bezeichnung des Algorithmsu
     */
    public QLearning(char player, TicTacToe_Env env, String ident)
    {
        super(player,env, ident);
       
        if (!ident.equals("Q-Learning")) return;
        
        // Abschalten der Konsolenausgaben beim Lernprozess
        boolean backup_sza = TicTacToe_Env.MOVE_DETAILS_TO_CONSOLE;
        boolean backup_sf = TicTacToe_Env.BOARDSTATE_TO_CONSOLE;
        boolean backup_ab = TicTacToe_Env.ACTIONVALUES_TO_CONSOLE;
        boolean backup_sfab = TicTacToe_Env.DISPLAY_ACTIONVALUES;
        TicTacToe_Env.MOVE_DETAILS_TO_CONSOLE = false;
        TicTacToe_Env.BOARDSTATE_TO_CONSOLE = false;
        TicTacToe_Env.ACTIONVALUES_TO_CONSOLE = false;
        TicTacToe_Env.DISPLAY_ACTIONVALUES = false;
        
        char opponentSymbol = (player=='x') ? 'o':'x';
        ownSign=player;
       
        if (algorithm_trainingOpponent.equals("self")){
            tttTrainingOpponent = this;
        }else{
            tttTrainingOpponent = env.produceAgent(opponentSymbol,algorithm_trainingOpponent);
        }
        
        if (algorithm_evaluationOpponent.equals("self")){
            tttTestOpponent = this;
        }else{
            tttTestOpponent = env.produceAgent(opponentSymbol,algorithm_evaluationOpponent);
        }
        
        Q = new HashMap <Integer, HashMap<Integer, Double>>(); 
        
        // Ausgaben vorbereiten
        JfxChartLogger jfxLogger = new JfxChartLogger(QlogFilePath+"ttt_QLearning_"+tttTrainingOpponent.getAlgIdent()+"_eval_"+tttTestOpponent.getAlgIdent()+"_",
                                                      "training opponent  '"+tttTrainingOpponent.getAlgIdent()+
                                                      "' evaluation opponent '"+tttTestOpponent.getAlgIdent()+
                                                      "' training interval size="+lengthTaininginterval+
                                                      " test games="+lengthEvaluationphase,
                                                      "Lost games by Q-Learning epsilon="+EPSILON,
                                                      "number of training games",
                                                      "lost games"); 
        jfxLogger.appendln("learning parameter:");
        jfxLogger.appendln("GAMMA;"+GAMMA); 
        jfxLogger.appendln("ETA;"+ETA);
        jfxLogger.appendln("EPSILON;"+EPSILON);
        jfxLogger.append("training opponent '"+tttTrainingOpponent.getAlgIdent()+
                         "'; evaluation opponent '"+tttTestOpponent.getAlgIdent()+"'\n"+
                         "training games;gewonnen;won %;lost;lost %;draw;draw %; winning rate in %;\n");
        System.out.println("training opponent '"+tttTrainingOpponent.getAlgIdent()+
                         "'; evaluation opponent '"+tttTestOpponent.getAlgIdent()+"'\n"+
                         "training games;gewonnen;won %;lost;lost %;draw;draw %; winning rate in %;\n");        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        jfxLogger.appendln("training start:;"+timestamp);
        System.out.println("training start:;"+timestamp); 
        
        System.out.println("Init2 player="+currentPlayer+" eig Zeichen="+ownSign+"\n"+TicTacToe_Env.matrixToString(state));
        
        while( cnt_gamesPlayed<=numberTraininggames ) 
        {
            currentPlayer = 'x';
            
            if (testphase){
                if (cnt_testgames>=lengthEvaluationphase)
                {
                    // switch off evaluation
                    System.out.println(counterToString(testphase));
                    jfxLogger.append(counterToString(testphase)+"\n");
                    jfxLogger.append(cnt_gamesPlayed,cnt_lost);
                    resultCounterReset();
                    testphase=false;
                }   
            }else{
                if ((cnt_gamesPlayed>0)&&(cnt_gamesPlayed%lengthTaininginterval==0))
                {
                    //switch on evaluation
                    resultCounterReset();
                    testphase=true;
                    cnt_testgames=0;
                }
            }
            
            // perform a training game
            boolean gameEnd=false;
            int moves = 0;
            do { 
                int keyState = getState( state, currentPlayer );
                int a = -1;
                if (currentPlayer==ownSign)
                    a = this.policy(state); 
                else{
                    if (testphase) {
                        if (algorithm_evaluationOpponent.equals("self")){
                            // play optimally
                            int s=getState(state, opponentSymbol);
                            a = getActionWithMaxQ(s);
                        }else{
                            a = tttTestOpponent.policy(state,currentPlayer);
                        }  
                    }else{
                        if (algorithm_trainingOpponent.equals("self")){
                            a = this.policy(state,currentPlayer);
                        }else{
                            a = tttTrainingOpponent.policy(state,currentPlayer);
                        }
                    }
                }
                state[a] = currentPlayer; 
                moves++;     
                double reward = this.getReward(state, currentPlayer);
                if(reward!=0 || moves>=8) { 
                   gameEnd = true;  
                   updateCounter(reward,currentPlayer);
                   if (testphase) 
                   {  
                       cnt_testgames++;
                   }else{
                       cnt_gamesPlayed++; 
                   }
                } 
                
                char nextPlayer = (currentPlayer=='x') ? 'o' : 'x'; 
                int keyNewState= getState(state, nextPlayer);
                if (!testphase)
                {   // aktualisieren der Q-Tabelle
                    if (gameEnd && (reward==0)){
                        if (currentPlayer==ownSign){
                            reward=this.belohnungFuerUnentschieden;
                        }else{
                            reward=-this.belohnungFuerUnentschieden;
                        }
                    }
                    update(keyState,a,reward, keyNewState, gameEnd); 
                }
                currentPlayer=nextPlayer;
            }while(!gameEnd);
            // Spielfeld reset
            for (int i=0;i<state.length; i++) state[i]='-';
            moves=0;       
        }
        timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("training end:;"+timestamp);
        jfxLogger.appendln("training end:;"+timestamp);
        jfxLogger.save(false);
        testphase=true;
        this.ownSign=player;
        TicTacToe_Env.MOVE_DETAILS_TO_CONSOLE =backup_sza ;
        TicTacToe_Env.BOARDSTATE_TO_CONSOLE=backup_sf ;
        TicTacToe_Env.ACTIONVALUES_TO_CONSOLE=backup_ab ;
        TicTacToe_Env.DISPLAY_ACTIONVALUES= backup_sfab;
    }
    
    /**
     * Update of Q(s,a)  ("Q-learning" approach)
     * @param s_key state key
     * @param a action
     * @param reward Reward
     * @param new_s_key Successor state
     * @param end Has a terminal state or the step limit been reached?
     */
    protected void update( int s_key, int a, double reward, int new_s_key, boolean end ) 
    { 
        double observation = 0.0;
        if (end) {
            observation = reward; 
        } else {
            observation = reward+(GAMMA * maxQ(new_s_key));
            observation = - observation; // Invert expectation because the opponent moves.
            
        }  
        
        double q = getQ(s_key, a); 
        q = q + ETA * (observation - q); 
        setQ(s_key,a, q); 
    }
    
    /**
     * Creates the key for accessing the Q-table. If it does not exist, then the corresponding 
     * record is created.
     * @param board board state
     * @param player Sign of the player in question
     * @return Key for the given state
     */
    protected int getState(char[] board, char player) 
    { 
        int key = QLearning.calculateStateKey(board,player);
        if(!Q.containsKey(key)){ 
            HashMap<Integer, Double> vals = new HashMap<Integer, Double>(); 
            for(int i = 0; i < board.length; i++){ 
                if(board[i] == '-'){ 
                    vals.put(i, 0.0);//(random.nextDouble()*2)-1);
                }
            } 
            Q.put(key, vals);
        }
        return key;
    } 

    @Override
    public int policy( char[] board ) 
    {
        return policy(board, ownSign);
    }
    
    /**
     * Epsilon-Greedy Policy.
     * @param s_key state key
     * @param
     */
    @Override
    public int policy(char[] board, char player) 
    { 
        int s_key = getState(board, player);
        if (testphase) {
            return getActionWithMaxQ(s_key); 
        }else {
            if(random.nextDouble()<EPSILON) { 
                // exploration
                ArrayList <Integer> A = coursesOfAction(board);
                return A.get(random.nextInt(A.size())); 
            } else {
                // exploitation
                return getActionWithMaxQ(s_key);   
            }
        }
    } 
    
    /**
     * Fetches the action with the largest Q value for a given state. If there
     * are several equal valued Q_max actions, then is randomly selected. If 
     * all Q-values are 0, then -1 is returned.
     * @param stateKey state key
     * @return Action with the highest Q value.
     */
    protected int getActionWithMaxQ(int stateKey) 
    { 
        if (env.ACTIONVALUES_TO_CONSOLE) calculateBoardFromStateKey(stateKey);
        double maxQValue = -9999.0; 
        int bestAction = -1; 
        ArrayList <Integer> lstW = new <Integer> ArrayList();
        ArrayList <Integer> lstA = new <Integer> ArrayList();
        Map<Integer, Double> vals = Q.get(stateKey); 
        for(Map.Entry<Integer, Double> entry : vals.entrySet()) { 
            lstW.add((int)Math.round(entry.getValue()));
            lstA.add(entry.getKey());
            if(entry.getValue() > maxQValue) { 
                maxQValue = entry.getValue(); 
                bestAction = entry.getKey(); 
            }   
        } 
        if (env.ACTIONVALUES_TO_CONSOLE) System.out.println("action values by "+ident);
        if ((env.ACTIONVALUES_TO_CONSOLE)||(env.DISPLAY_ACTIONVALUES)) {
            calculateBoardFromStateKey(stateKey);
            for (int i=0;i<lstA.size();i++) {
                if (env.DISPLAY_ACTIONVALUES) env.showText(""+lstW.get(i),lstA.get(i)%3,lstA.get(i)/3);
                if (env.ACTIONVALUES_TO_CONSOLE) System.out.println("Q(s,"+lstA.get(i)+") = "+lstW.get(i));
            }
        }
        return bestAction; 
    } 
    
    /**
     * Get the largest Q value for a given state. If no entry exists, then return zero.
     * @param s state key
     * @return Largest Q value present.
     */
    public Double maxQ(int s) 
    { 
        double maxQValue = Double.NEGATIVE_INFINITY; 
        Map<Integer, Double> vals = Q.get(s); 
        if (vals!=null) {
            for(Map.Entry<Integer, Double> entry : vals.entrySet()) { 
                if(entry.getValue()>maxQValue) { 
                    maxQValue = entry.getValue(); 
                }
            } 
            return maxQValue;
        }else{
            return null;
        }
    }  
    
    /**
     * Sets a Q-value for a specific state-action pair.
     * @param s state key
     * @param a action
     * @param v Q-value
     */
    protected void setQ(int s, int a, Double v) 
    { 
        Q.get(s).put(a,v); 
    } 
 
    /**
     * Gets the Q value of a given state action pair.
     * @param s state key
     * @param a action
     * @return Q-value
     */
    protected double getQ(int s, int a) 
    { 
        return Q.get(s).get(a); 
    } 
   
    /**
     * Helper function for updating the statistical result counters.
     * @param reward Reward
     * @param currentPlayer player
     */
    protected void updateCounter(double reward, char currentPlayer )
    {
        if (reward==0)
        {
            cnt_draw++; 
        }else{ 
            if (currentPlayer==ownSign){
                if (reward>0){
                    cnt_won++; 
                
                }else{ 
                    cnt_lost++;
                }
            }else{
                if (reward>0){
                    cnt_lost++; 
                }else{
                    cnt_won++; 
                }
            }          
        }
    }
    
    /**
     * Converts the counters into a string for console output.
     * @param testphase switch for evaluation phases
     * @return a string for console output of the statistical counters
     */
    protected String counterToString(boolean testphase)
    {
        double gamesNumber = 0;
        if (testphase) {
            gamesNumber = lengthEvaluationphase;
        }else{
            gamesNumber = lengthTaininginterval;
        }
        String ret = cnt_gamesPlayed+";"+cnt_won+";";
               ret+= String.format("%.1f", cnt_won*100/gamesNumber);
               ret+= "%;"+cnt_lost+";";
               ret+= String.format("%.1f", cnt_lost*100/gamesNumber);
               ret+= "%;"+cnt_draw+";";
               ret+= String.format("%.1f",cnt_draw*100/gamesNumber);
               ret+= "%;";
        if (testphase){
            double quote = (double)(cnt_won-cnt_lost)/(double)cnt_testgames;
            ret+= String.format("%.1f",(100*quote));
            ret+="%;";
        }
        return ret;
    }
    
    /**
     * Reset the counters for the exercise statistics.
     */
    protected void resultCounterReset()
    {
        cnt_won = 0;
        cnt_lost = 0;
        cnt_draw = 0; 
    }
    
    /**
     * Converts a game state into an integer number. Each field is interpreted as a digit in the ternary system (base 3).
     * @param board board state
     * @param player player associated
     * @return state key
     */
    public static int calculateStateKey( char[] board, char player ) 
    {
        int stellenwert = 1;
        int ziffernwert = 0;
        int zNr = 0;
        char c;
        for (int i=0;i<=board.length;i++)
        {
            if (i<board.length) {
                c = board[i];
            }else{
                c = player;
            }
            
            switch (c) {
                case 'x': ziffernwert=0; break;
                case 'o': ziffernwert=1; break;
                default : ziffernwert=2;
            }
           
            zNr += stellenwert*ziffernwert;
            stellenwert*=3;
        }
        return zNr;
    }
    
    /**
     * Converts an integer state key into a game array. The key is interpreted as a nine-digit
     * number in the ternary system with the three digit values 'x','o','-'.
     * @param z state key
     * @return board state char array
     */
    public static char[] calculateBoardFromStateKey( int z )
    {
        char[] feld = new char[9];
        char play;
        int ziffer=0;
        for (int i=0; i<=8; i++) {
            ziffer = z%3;
            if (ziffer==0) {
                feld[i]='x';
            }else if (ziffer==1) {
                feld[i]='o';
            }else {
                feld[i]='-';
            }
            z/=3;
        }
        if (z==0) {
             play='x';
        }else if (z==1) {
             play='o';
        }else {
             play='-';
        }      
        return feld;
    }
}
