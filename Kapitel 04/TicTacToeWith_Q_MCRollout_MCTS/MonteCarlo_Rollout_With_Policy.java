import java.util.ArrayList;

/**
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
public class MonteCarlo_Rollout_With_Policy extends MonteCarlo_Rollout 
{
    MonteCarlo_Rollout simulierterGegner = null; 
    char eigenesZeichen;
    
    /**
     * Konstruktor für Objekte der Klasse MonteCarlo_mit_RolloutPolicy
     */
    public MonteCarlo_Rollout_With_Policy(char player,TicTacToe_Env umgebung, String ident)
    {
        super(player,umgebung, ident);
        eigenesZeichen = player;
        simulierterGegner = new MonteCarlo_Rollout( (eigenesZeichen=='x') ? 'o':'x', umgebung, "Simulierter Gegner");
        simulierterGegner.setMaxRollouts(this.samplesNumber);
    }
    
    /**
      * Setzt die Anzahl der jeweils durchzuführenden Rollouts.
      * @param n Stichprobenumfang der MC-Rollout-Evaluation (auch für den simulierten Gegner).
      */
     @Override
     public void setMaxRollouts(int n){
         super.setMaxRollouts(n);
         simulierterGegner.setMaxRollouts(n);
     }
    
    /**
     * Führt ein Spiel auf der Basis des gegebenen Feldzustandes aus. 
     * Die Reaction des Gegners wird mit einer "hypothetischen" Policy simuliert.
     * 
     * @param action Nummer des Feldes in das gesetzt wird
     * @param player das Zeichen des aktiven Spielers ('x' oder 'o')
     * @return Bewertung der Aktion aus Sicht des angegebenen Spielers
     */
    public double rollout_evaluation( int action, char player ) 
    {
        // Aktion probeweise ausführen
        state[action]=player;
        
        // Falls Feldzustand für den Spieler eine Belohnung generiert, dann Rückgabe der Belohnung und fertig.
        double reward = getReward(state, player);
        if (reward!=0) {
            state[action]='-';
            return reward;
        }
        
        // Gegner setzt als nächstes
        player = (player=='o') ? 'x':'o';
        ArrayList <Integer> A_s = coursesOfAction(state);
        if (A_s.size()==0){
            state[action]='-';
            return 0; // Falls Feld voll, dann fertig und Rückgabe 0.  
        }
        
        // Aktion entsprechend der Simulation des Gegners auswählen
        int a =-1;
        if (player!=eigenesZeichen){
            a = simulierterGegner.policy(state);
        }else{
            a = A_s.get(random.nextInt(A_s.size()));
        }
        
        double wert=0;
        if (a>=0) wert = -rollout_evaluation( a, player ); // Ergebnis negativ bewerten
        
        // Testausführung rückgängig machen
        state[action]='-'; // Testausführung rückgängig machen
        
        return wert;
    }
}