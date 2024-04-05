import java.util.List;

/**
 * TicTacToe agent that evaluates actions using Monte Carlo tree search (MCTS).
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
public class MCTS extends MonteCarlo_Rollout 
{
    protected long timelimit = 50;
    public boolean konsolenausgabe_mct = false;
    public JfxMCTreeVisualizer jfxMCTreeVisualizer;
    
    /**
     * Konstruktor für Objekte der Klasse MCTS
     */
    public MCTS(char spieler,TicTacToe_Env umgebung, String ident, long timelimit)
    {
        super(spieler,umgebung, ident);
        this.timelimit = timelimit;
        setMaxRollouts(100);
    }
    
    @Override
    public int policy(char[] state) 
    {
        char[] backup = getState();
        char opponent = (ownSign=='o') ? 'x':'o';
        if (TicTacToe_Agent.coursesOfAction(state).size()==0) return -1;
        MC_Tree mct = new MC_Tree(state);
        MCT_Node root = mct.getRoot();
        root.setState(state);
        root.setPlayer(opponent);
        long endTime = System.currentTimeMillis()+timelimit; int c=0;
        while ((System.currentTimeMillis() < endTime) && (c<getMaxRollouts())){
            MCT_Node selectedNode = selection(root);
            if (TicTacToe_Env.checkMatrixWon(selectedNode.getState())=='-') {
                expand(selectedNode);
            }
            MCT_Node nodeToBeEvaluated = selectedNode; 
            if (selectedNode.getNumberOfChildren() > 0) { // falls der Knoten Kinder hat
                nodeToBeEvaluated = selectedNode.selectChildRandomly();
            }
            
            setState(nodeToBeEvaluated.getState());
            double v = rollout_evaluation(nodeToBeEvaluated.getActionFromFather(),nodeToBeEvaluated.getPlayer());
           
            backpropagation(nodeToBeEvaluated, v);
            c++; 
        }
        
        if (konsolenausgabe_mct){
            visualize(mct); // ggf Ausgabe des Monte Carlo Baums
            System.out.println("ROOT:"+root.toString()+TicTacToe_Env.matrixToString(root.getState()));
        }
        MCT_Node bestNode = root.childWithMaxScore();
        
        setState(backup);
        return bestNode.getActionFromFather();
    }
    
    private MCT_Node selection(MCT_Node root)
    {
        MCT_Node node = root;
        while (node.getNumberOfChildren() != 0) {
            node = node.childWithMaxUCT();
        }
        return node;
    }
    
    private void expand(MCT_Node node){
        char[] fatherState = node.getState();
        char childNodePlayer = (node.getPlayer()=='o') ? 'x':'o';
        List <Integer> moeglicheAktionen = coursesOfAction(fatherState);
        for (int a : moeglicheAktionen){
            char[] childState = fatherState.clone();
            childState[a]=childNodePlayer; // perform action 
            MCT_Node newNode = new MCT_Node(childState);
            newNode.setPlayer(childNodePlayer);
            newNode.setFather(node);
            node.addChild(newNode,a);
        }
    }
    
    private void backpropagation(MCT_Node node, double score) 
    {
        MCT_Node temp = node;
        score = Math.round(score);
        char winner = '-';
        if (score>0) winner=node.getPlayer();
        if (score<0) winner=node.getOpponent();
        if (score==0) winner=ownSign;
        while (temp != null) {
            temp.incVisits();
            if (temp.getPlayer() == winner) {
                temp.addScore(1);
            }
            temp = temp.getFather();
        }
    }
    
    public static double uct(int N, double w_a, int N_a) 
    {
        if (N_a == 0) {
            return Integer.MAX_VALUE;
        }
        return (w_a/(double)N_a) + 1.414*Math.sqrt(Math.log(N)/(double)N_a);
    }
    
    public void setZeitlimit(long timelimit)
    {
        this.timelimit=timelimit;
    }
    
    public long getZeitlimit()
    {
        return this.timelimit;
    }
    
    public void visualize(MC_Tree mct)
    {
        // MCT_Node root = this.root;
        // if (jfxMCTreeVisualizer==null) 
        // jfxMCTreeVisualizer = new JfxMCTreeVisualizer(mct,"Monte-Carlo Tree");
        // jfxMCTreeVisualizer.updateVisualisation(mct);
    }
 
}
    