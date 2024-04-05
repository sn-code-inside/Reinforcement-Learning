import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;

/**
 * Environment for TicTacToe playing agents.
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
    public final static String x_player = "Human"; // Algorithmus für X
    public final static String o_player = "Arbitrary";  // Algorithmus für O
    
    public static boolean MOVE_DETAILS_TO_CONSOLE = true;
    public static boolean BOARDSTATE_TO_CONSOLE = true;
    public static boolean ACTIONVALUES_TO_CONSOLE = true;
    public static boolean DISPLAY_ACTIONVALUES = true;
 
    // -------------------------  
    
    // start state:
    protected char[] matrix = {'-','-','-',
                               '-','-','-',
                               '-','-','-'};

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
     * @param player Symbol of the player
     * @param ident Name of the algorithm used  ("NegaMax", "Arbitrary", "Human")
     */
    public TicTacToe_Agent produceAgent(char player, String ident)
    {
        if (ident.equals("NegaMax"))
        {
            return new NegaMax(player,this,ident);
        } else if (ident.equals("Arbitrary"))
        {
            return new Arbitrary(player,this,ident);
        }else return null;
    }
   
   /**
    * Prepares the playing field.
    */
    private void prepare()
    {
         for (int i=0;i<tttFields.length;i++)
         {
             TicTacToe_Field tttField = new TicTacToe_Field();
             tttField.setOccupancy(matrix[i]);
             addObject(tttField,i%3,i/3);
         }
    }
    
    /**
     * Greenfoot function: Execute a simulation step.
     */
    public void act()
    {   
        MouseInfo mouse = Greenfoot.getMouseInfo();
        // set cursor
        int x = 0; int y = 0;
        if (mouse!=null)
        {
           x = mouse.getX();
           y = mouse.getY();
           if (cursor==null){
               cursor = new Cursor();
               this.addObject(cursor,x,y);
           }else{
               cursor.setLocation(x,y);
           }
        }
        
        if (ttt_Agents[playerNum(player)]==null) // this means: human player
        {      
            if ((mouse!=null)&&(mouse.getClickCount())>0)
            {
                move(3*y+x,player);
                if (checkMatrixWon(matrix)==player){
                    System.out.println("Player "+player+" has won!");
                    Greenfoot.stop();
                }
             
            }
        }else{
            int a = ttt_Agents[playerNum(player)].policy(matrix);
            move(a,player);
        }
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
            System.out.println(matrixToString(matrix)+" Player="+c);
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
    
    public static char checkMatrixWon(char[] tttFeld)
    {
        for (int i=0;i<winCaseList.length;i++)
        {
            char z = checkMatrixLine(tttFeld, i);
            if ((z=='x')||(z=='o')) return z;
        }
        return '-';
    }
    
    public static char checkMatrixLine(char[] matrix, int i)  
    {       
        char[] f = winCaseList[i];
        {
            if (matrix[f[1]]==(matrix[f[0]]) && (matrix[f[2]]==matrix[f[0]])) return matrix[f[0]];
        }
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