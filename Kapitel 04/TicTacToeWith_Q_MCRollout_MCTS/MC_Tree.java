import java.util.Arrays;
/**
 * Monte-Carlo search tree
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
public class MC_Tree  
{
    MCT_Node root;
    public static boolean[] ebenenAusgabe = new boolean[20]; 
    
    public MC_Tree()
    {
        root = new MCT_Node();
    }
    
    public MC_Tree(char[] startzustand)
    {
        root = new MCT_Node(startzustand);
    }
    
    public MCT_Node getRoot()
    {
        return root;
    }
    
    public void setRoot(MCT_Node root)
    {
        this.root=root;
    }
    
    public void pruefAusgabe()
    {
        for (int i=0;i<ebenenAusgabe.length;i++) ebenenAusgabe[i]=false;
        rekursiveAusgabe(root,0,true);
    }
    
    public void rekursiveAusgabe(MCT_Node knoten, int tiefe, boolean letzterKnoten)
    {
        //System.out.print(TicTacToe_Umgebung.matrixToString(root.getZustand()));   
        for (int i=0;i<=tiefe;i++){
            if (ebenenAusgabe[i]) System.out.print("|");
            System.out.print("\t");
        }
        System.out.println();
        for (int i=0;i<tiefe;i++){
            
            if (ebenenAusgabe[i]) System.out.print("|");
            System.out.print("\t");
        }
        if (letzterKnoten){ 
            System.out.print("└─"+knoten.getActionFromFather()+"─");
            ebenenAusgabe[tiefe]=false;
        }else{
            System.out.print("├─"+knoten.getActionFromFather()+"─");
            ebenenAusgabe[tiefe]=true;
        }
        System.out.println(knoten.getPlayer()+"("+tiefe+"):["+((int)knoten.getScore())+"/"+knoten.getVisits()+"]");
        int i=0;
        for (MCT_Node kind: knoten.getChildren()){
            i++;
            ebenenAusgabe[tiefe+1]=true;
            rekursiveAusgabe(kind, tiefe+1,i==knoten.getChildren().size());
        }
    }
}
