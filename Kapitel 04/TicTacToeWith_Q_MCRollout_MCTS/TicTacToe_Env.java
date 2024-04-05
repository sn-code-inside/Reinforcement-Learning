import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * An environment for agents playing TicTacToe. 
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
public class TicTacToe_Env extends World
{
    // ----- settings -----
    /* algorithms
     - "MCTS" 
     - "Monte-Carlo Rollout"
     - "Q-Learning"
     - "NegaMax"
     - "Arbitrary" 
    */
    public final static String x_player = "NegaMax";  // algorithm for X
    public final static String o_player = "MCTS"; // algorithm for O
    
    public static boolean MOVE_DETAILS_TO_CONSOLE = true;
    public static boolean BOARDSTATE_TO_CONSOLE = true;
    public static boolean ACTIONVALUES_TO_CONSOLE = true;
    public static boolean DISPLAY_ACTIONVALUES = true;
    
    protected JfxChartLogger jfxLogger = null;
    protected String logFilePath = "data\\";
                     
    // start state:
    protected char[] startState = {'-','-','-',
                                   '-','-','-',
                                   '-','-','-'};
                                   
    protected char[] matrix = startState.clone();
                                   
    // Array with two TicTacToe_agents, zero for human player                                  
    protected TicTacToe_Agent[] ttt_Agents = new TicTacToe_Agent[2];
                               
    protected TicTacToe_Field[] tttFields = new TicTacToe_Field[9];  
    protected static int REWARD_WIN = 100;
    public static final char winCaseList[][] =
         {
                {2,4,6},
                {0,4,8},
                {0,3,6},
                {1,4,7},
                {2,5,8},
                {0,1,2},
                {3,4,5},
                {6,7,8}};
                
    protected int cnt_steps = 0;
    protected char player = 'x';
        
    protected Cursor cursor = null;

    public TicTacToe_Env()
    {
        super(3, 3, 150); 
        this.setBackground("tictactoespielfeld.png");
        prepare();
        
        ttt_Agents[0] = produceAgent('x',x_player);
        ttt_Agents[1] = produceAgent('o',o_player);
    }

    /**
     * Generates a TicTacToe agent.
     * available algorithms:
     * "MCTS", "Monte-Carlo Rollout", "Q-Learning", "NegaMax", "Arbitrary" 
     * 
     * @param player Symbol of the player
     * @param ident Name of the algorithm used  
     */
    public TicTacToe_Agent produceAgent(char player, String ident)
    {
        if (ident.equals("MCTS"))
        {
            return new MCTS(player,this,ident,1000); // Zeitlimit
        }else if (ident.equals("Monte-Carlo Rollout"))
        {
            return new MonteCarlo_Rollout(player, this, ident);
            
        }else if (ident.equals("Q-Learning"))
        {
            return new QLearning(player,this,ident);
        }else if (ident.equals("NegaMax"))
        {
            return new NegaMax(player,this,ident);
        }else if (ident.equals("Arbitrary"))
        {
            return new Arbitrary(player,this,ident);
        }else return null;
    }
   
    /**
    * Prepares the playing field.
    */
    private void prepare() {
        for (int i=0;i<tttFields.length;i++) {
             TicTacToe_Field tttField = new TicTacToe_Field();
             tttField.setOccupancy(matrix[i]);
             addObject(tttField,i%3,i/3);
        }
    }
    
    /**
     * This initializes the data logger depending on selected agent class.
     * @param games Number of games that the agents play against each other per parameterstep.
     * @param parameterMin Start of the parameter test interval (e.g. rollouts)
     * @param parameterMax End of the parameter test interval (e.g. rollouts)
     * @param parameterStep Step size of the parameter test.
     * @param countedPlayer Player whose results are counted.
     */
    private void initCompareJfxLogger(int games, int paramMin, int paramMax, int stepSize, char countedPlayer){
        if ((ttt_Agents==null)||((ttt_Agents[0]==null)||(ttt_Agents[1]==null))){
            System.out.println("[env.initJfxLogger()] Two AI agents needed!");
            return;
        }
        
        String competition_str=ttt_Agents[0].getIdent()+"(x)_vs_"+ttt_Agents[1].getIdent()+"(o)";
       
        jfxLogger = new JfxChartLogger(logFilePath+competition_str+"_","'"+competition_str+"'  Successes of '"+countedPlayer+"' in "+games+" test games.",
                                                      "successes "+ttt_Agents[playerNum(countedPlayer)].getIdent(),
                                                      "number of rollouts",
                                                      "successes");      
        jfxLogger.appendln("competion;"+competition_str);
        jfxLogger.appendln("start;"+JfxChartLogger.getTimeStamp());
      
    }
    
    @Override
    public void act()
    {   
        MouseInfo mouse = Greenfoot.getMouseInfo();
        int x = 0; int y = 0;
        if (mouse!=null)
        {  // Auswahlrahmen setzen
           x = mouse.getX();
           y = mouse.getY();
           if (cursor==null){
               cursor = new Cursor();
               this.addObject(cursor,x,y);
           }else{
               cursor.setLocation(x,y);
           }
        }
        
        if (ttt_Agents[playerNum(player)]==null) 
        {   // human player  
            if ((mouse!=null)&&(mouse.getClickCount())>0){
                move(3*y+x,player);
            }
        }else
        {   // AI move
            if (cnt_steps<9) {
                int a = ttt_Agents[playerNum(player)].policy(matrix);
                move(a,player);
            }else{
                System.out.println("Board state is draw!");
                Greenfoot.stop();
            }
        }
    }
    
    /**
     * Function with which one can let agents compete against each other and evaluate the results, iterates a 
     * parameter if necessary.
     * @param games Number of games that the agents play against each other per parameterstep.
     * @param parameterMin Start of the parameter test interval (e.g. rollouts)
     * @param parameterMax End of the parameter test interval (e.g. rollouts)
     * @param parameterStep Step size of the parameter test.
     * @param countedPlayer Player whose results are counted.
     */
    public void agentComparison(int games,int parameterMin, int parameterMax, int parameterStep, char countedPlayer)
    {
        char notCounted = (countedPlayer=='x') ? 'o' : 'x';
        
        ttt_Agents[0] = produceAgent('x',x_player);
        ttt_Agents[1] = produceAgent('o',o_player);
        
        ACTIONVALUES_TO_CONSOLE=false;
        initCompareJfxLogger(games, parameterMin, parameterMax, parameterStep,countedPlayer); 
        
        System.out.println("rollouts; games; x-won; o-won; draw; winning rate");
        for (int n=parameterMin;n<=parameterMax;n+=parameterStep)
        {  
            if (ttt_Agents[playerNum(notCounted)] instanceof MonteCarlo_Rollout){
                ((MonteCarlo_Rollout)ttt_Agents[playerNum(notCounted)]).setMaxRollouts(n); // The rollout algorithm has the role of an ever-improving "opponent".
            }
            
            if (ttt_Agents[playerNum(countedPlayer)] instanceof MonteCarlo_Rollout){
                ((MonteCarlo_Rollout)ttt_Agents[playerNum(countedPlayer)]).setMaxRollouts(n); // The rollout algorithm has the role of an ever-improving "opponent".
            }
             
            System.out.print(n+"; ");
            int won = comparisonWonLost(games,playerNum(countedPlayer),startState);
            jfxLogger.append(n,won);
        }
        if (jfxLogger!=null) {
            jfxLogger.save(true);
        }
    }
      
    
    /**
     * Helper function that runs a specified number of AI games and determines the average reward for the specified player.
     * @param numGames Number of test games
     * @param countedPlayer Player whose results are counted.
     * @param startState Starting position on the board
     * @return The average reward for the specified player.
     */
    private double averageReward(int numGames, int countedPlayer, char[] startState)
    {
        char[] start = startState.clone(); 
        if ((ttt_Agents[0]==null) || (ttt_Agents[1]==null)){
            System.out.println("For the test run, both players must be AIs.");
            return 0;
        }
        int[] cnt_won = new int[2];
        int cnt_draw = 0;
        int cnt_game = 0;
        int cnt_moves = 0;
        for (cnt_game = 0; cnt_game<numGames; cnt_game++){
            matrix=start.clone();
            boolean spielende = false;
            cnt_moves=0;
            player='x';
            while (!spielende){
                int a = ttt_Agents[playerNum(player)].policy(matrix);
                if (((a>=0)&&(a<=8))&&(matrix[a]=='-')){
                    matrix[a]=player;
                }else{
                    System.out.println("Error! Action "+a+" is not possible!");
                }
                
                if (checkMatrixWon(matrix)==player){
                    cnt_won[playerNum(player)]++;
                    spielende=true;
                } else if (countOpenFields(matrix)==0) {
                    cnt_draw++;
                    spielende=true;
                }
                
                player = (player=='x') ? 'o' : 'x';
                cnt_moves++;
            }
        }

        double reward = ((cnt_won[countedPlayer]-cnt_won[(countedPlayer==1) ? 0 : 1])*TicTacToe_Env.REWARD_WIN)/numGames;
        System.out.println(cnt_game+","+reward);
        
        matrix=start.clone();
        return reward;
    }
   
    /**
     * Helperfunction that runs a specified number of AI games and counts the games won for the specified player.
     * @param numGames  - Number of test games
     * @param countedPlayer - Spieler, für den die Anzahl der Gewinne gezählt werden soll.
     * @param startState Ausgangsstellung auf dem Brett
     * @return number of games won by the counted player.
     */
    private int comparisonWonLost(int numGames, int countedPlayer, char[] startState)
    {
        char[] start = startState.clone();    
        if ((ttt_Agents[0]==null) || (ttt_Agents[1]==null)){
            System.out.println("For the test run, both players must be AIs.");
            return 0;
        }
        int[] cnt_won = new int[2];
        int cnt_draw = 0;
        int cnt_game = 0;
        int cnt_moves = 0;
        for (cnt_game = 0; cnt_game<numGames; cnt_game++){
            matrix=start.clone();
            boolean gameEnd = false;
            cnt_moves=0;
            player='x';
            while (!gameEnd){
                int a = ttt_Agents[playerNum(player)].policy(matrix);
                if (((a>=0)&&(a<=8))&&(matrix[a]=='-')){
                    matrix[a]=player;
                }else{
                    System.out.println("Error! Action "+a+" is not possible!");
                }                
                if (checkMatrixWon(matrix)==player){
                    cnt_won[playerNum(player)]++;
                    gameEnd=true;
                } else if (countOpenFields(matrix)==0) {
                    cnt_draw++;
                    gameEnd=true;
                }   
                player = (player=='x') ? 'o' : 'x';
                cnt_moves++;
            }
        }
        System.out.println(cnt_game+";"+cnt_won[0]+";"+cnt_won[1]+";"+cnt_draw+";"+((cnt_won[countedPlayer]*100)/cnt_game)+"%");
        matrix=start.clone();
        return cnt_won[countedPlayer];
    }
    
    /**
     * Performs move a for player c.
     * @param a action
     * @param c Symbol of the player.
     * @return true if move execution was successful, false otherwise.
     */
    public boolean move(int a, char c)
    {
        if (checkMatrixWon(matrix)==player)
        {
            System.out.println("Player "+player+" has already won!");
            Greenfoot.stop();
            return false;
        }else if (cnt_steps>=9) {
            System.out.println("Board state is a draw!");
            Greenfoot.stop();
            return false;
        }
        
        if (MOVE_DETAILS_TO_CONSOLE){
            String playertyp = (c=='x') ? x_player : o_player;
            System.out.println("----- move "+cnt_steps+": player:'"+c+"'("+playertyp+") action="+a+" ------");
        }
        if ((a<0) || (a>matrix.length)){
            System.out.println("TicTacToe_Env.move("+a+","+c+") : Error! Action "+a+" is not possible.");
            return false;
        }
        List l = this.getObjectsAt(a%3,a/3,TicTacToe_Field.class);       
        if (l!=null)
        {
           TicTacToe_Field tttFeld = (TicTacToe_Field)l.get(0);
           if (tttFeld.getOccupancy()=='-')
           {
               tttFeld.setOccupancy(c);
               matrix[a]=c;
           }else{
               System.out.println("Field "+a+" is already occupied!");
               return false;
           }
        }
        
        if (BOARDSTATE_TO_CONSOLE){;
            System.out.println(matrixToString(matrix));
        }
        
        if (checkMatrixWon(matrix)==player)
        {
            System.out.println("Player "+player+" has won!");
            Greenfoot.stop();
            return true;
        }else if (cnt_steps>=9) {
            System.out.println("Draw!");
            Greenfoot.stop();
            return true;
        }
        
        player = (player=='x') ? 'o' : 'x';
        cnt_steps++;
        return true;
    }
    
    // Funktionen für die TicTacToe-Spielfeldauswertung:
    
    public static char checkMatrixWon(char[] tttBoard)
    {
        for (int i=0;i<winCaseList.length;i++)
        {
            char z = checkMatrixLine(tttBoard, i);
            if ((z=='x')||(z=='o')) return z;
        }
        return '-';
    }
    
    public static int countOpenFields(char[] tttBoard)
    {
        int num = 0;
        for (int i=0;i<9;i++) {
            if (tttBoard[i]=='-'){
                num++;
            }
        }
        return num;
    }
    
    public static char checkMatrixLine(char[] matrix, int i)  
    {       
        char[] f = winCaseList[i];
        if (matrix[f[1]]==(matrix[f[0]]) && (matrix[f[2]]==matrix[f[0]])) return matrix[f[0]];
        return '-';   
    }

    public static String matrixToString(char[] sfmatrix)
    {
        String ret ="";
        for (int i=0; i<3; i++)
        {
            for (int j=0;j<3; j++)
            {
                ret+=sfmatrix[i*3+j]; 
            }
            ret+='\n';
        }
        return ret;
    }
    
    private int playerNum(char c)
    {
        return (c=='x') ? 0 : 1;
    }
    
}