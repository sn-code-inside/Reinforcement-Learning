import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Genome {
    private static int nextID; 
    public int ID;
    public double fitness;
    public ArrayList<Map.Entry<String, double[]>> policy_table;
    
    public Genome(){
        this.fitness=0.0;
        this.policy_table = new ArrayList<Map.Entry<String, double[]>> ();
        this.ID = nextID;
        nextID++;
    }
    
    public Genome(ArrayList<Map.Entry<String, double[]>> policy_table){
        this.fitness=0.0;
        this.policy_table = policy_table;
        this.ID = nextID;
        nextID++;
    }
    
    
    public Genome(int ID, ArrayList<Map.Entry<String, double[]>> policy_table){
        this.ID = ID;
        this.fitness=0.0;
        this.policy_table = policy_table;
    }
    
    /**
     * Clones a genome. A new ID number is assigned to the clone.
     */
    public Genome clone(){
        Genome ret = new Genome((ArrayList<Map.Entry<String, double[]>>)policy_table.clone());
        ret.fitness = 0.0;
        ret.ID = nextID;
        nextID++;
        return ret;
    }

}