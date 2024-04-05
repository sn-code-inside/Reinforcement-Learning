import java.util.ArrayList;
import java.util.Random;

/**
 * Node of a Monte-Carlo search tree.
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
public class MCT_Node  
{
    private static Random zufall = new Random();
    
    // Zustand
    protected char[] feld;
    
    // Statistik
    char player;
    int besuche;
    double score;
    
    int aktionVonFather = -1; 
    MCT_Node fatherNode = null;
    
    ArrayList <MCT_Node> children = new ArrayList<MCT_Node>();
    
    public MCT_Node()
    {
        this.feld =  new char[] {'-','-','-','-','-','-','-','-','-'};
    }
    
    public MCT_Node(char[] s)
    {
        this.feld=s;
    }
    
    public MCT_Node selectChildRandomly()
    {
        if (children.isEmpty()) return null;
        return children.get(zufall.nextInt(children.size()));
    }
    
    public MCT_Node childWithMaxScore()
    {
        MCT_Node ret = null;
        double max = Double.NEGATIVE_INFINITY;
        for (MCT_Node knoten : children){
            if (knoten.getScore()>max){
                max = knoten.getScore();
                ret = knoten;
            }
        }
        return ret;
    }
    
    public MCT_Node childWithMaxUCT() 
    {
        double maxUCT = Double.NEGATIVE_INFINITY;  
        MCT_Node maxChild = null;
        for (MCT_Node kind : children){
            double bewertung = MCTS.uct(besuche,kind.getScore(),kind.getVisits());
            if (bewertung>maxUCT){
                maxUCT=bewertung;
                maxChild=kind;
            }
        }
        return maxChild;
    }
    
    public ArrayList <MCT_Node> getChildren(){
        return children;
    }
     
    public char[] getState()
    {
        return feld;
    }
    
    public void setState(char[] s)
    {
        this.feld=s;
    }
    
    public void setPlayer(char player)
    {
        this.player=player;
    }
    
    public char getPlayer()
    {
        return this.player;
    }
    
    public char getOpponent()
    {
        return (this.player=='x') ? 'o':'x';
    }
    
    public int getNumberOfChildren()
    {
        return children.size();
    }
    
    public void setActionFromFather(int aktion){
        this.aktionVonFather = aktion;
    }
    
    public int getActionFromFather()
    {
        return this.aktionVonFather;
    }
    
    public void addChild(MCT_Node kindknoten, int aktion)
    {
        kindknoten.setActionFromFather(aktion);
        this.children.add(kindknoten);
    }
    
    public void setFather(MCT_Node father)
    {
        this.fatherNode = father;
    }
    
    public MCT_Node getFather()
    {
        return this.fatherNode;
    }
    
    public void addScore(double delta)
    {
        this.score+=delta;
    }
    
    public double getScore()
    {
        return this.score;
    }
    
    public void incVisits()
    {
        this.besuche++;
    }
    
    public int getVisits()
    {
        return this.besuche;
    }
    
    @Override
    public String toString()  {
        return getPlayer()+":["+((int)getScore())+"/"+getVisits()+"]";
    }
}
