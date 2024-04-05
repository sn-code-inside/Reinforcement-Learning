import java.text.NumberFormat;
import java.util.Arrays;

/**
 * Class for state description with specific feature extractions.
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
public class State  
{
    public final static int DIM = 3; // dimension of the state space
    public final static int PARTS = 7;
    public static int VECTORSIZE = DIM*PARTS;
    
    private static double[] minima = null;
    private static double[] maxima = null;
    private final static NumberFormat nf = NumberFormat.getInstance(); 
    
    private double[] absVals = null;
    private double[] normVec = null;
    private double[] x_vektor = null;
    
    public State(double[] s_werte)
    {
        nf.setMaximumFractionDigits(3);
        absVals = s_werte.clone();
        normVec = calcNormvec();
        x_vektor = calcRbfFeaturevec();
    }
    
    public static void setMinMax(double[] min, double[] max){
        minima=min;
        maxima=max;
    }

    /**
     * Calculates an array of state values in the range [0;1].
     */
    private double[] calcNormvec()
    {
        normVec = new double[DIM];
        for (int i=0; i<DIM; i++){
            normVec[i]=range01(absVals[i],minima[i],maxima[i]);
        }
        return normVec;
    }
    
    public double[] getNormVec(){
        return normVec;
    }
    
    /**
     * Calculates RBF features with linear subdivisions for each sensor.
     */
    private double[] calcRbfFeaturevec(){
        VECTORSIZE = DIM*PARTS;
        double[] x_vec = new double[VECTORSIZE];
        double dist = 1/(double)PARTS;
        for (int i=0;i<DIM; i++){
            double c_i=0;
            for (int j=0;j<PARTS;j++){
                x_vec[i*PARTS+j]=Math.exp(-sqr(normVec[i]-c_i)/(2*sqr(dist/2)));
                c_i+=dist;
            }
        }
        return x_vec;
    }
    
    private double sqr(double x){
        return x*x;
    }
    
    /**
     * Calculates features with subdivisions for each sensor. Three displacements are performed.
     */
    private double[] calcDispBinaryFeatureVec()
    { 
        VECTORSIZE = 3*DIM*PARTS;
        double[] x_vec = new double[VECTORSIZE];
        for (int j=0;j<3;j++){   
            for (int i=0;i<DIM;i++){
                x_vec[(j*DIM*PARTS)+i*PARTS+linTile(PARTS,normVec[i]+(0.333*j)/PARTS)]=1.0;
            }
        }
        return x_vec;
    }
    
    /**
     * Calculates features with subdivisions for each sensor.
     */
    private double[] calcBinaryFeatureVec()
    { 
        VECTORSIZE = DIM*PARTS;
        double[] x_vec = new double[VECTORSIZE];
        
        for (int i=0;i<DIM;i++){
            x_vec[i*PARTS+linTile(PARTS,normVec[i])]=1.0;
        }
        
        return x_vec;
    }
    
    private int linTile(int n, double v01 ){
        if (v01==0) return 0;
        if (v01>=1) return n-1;
        double c_dist = 1/(double) n;
        double sum = c_dist;
        int i = 0;
        while (sum<v01){
            sum+=c_dist;
            i++;
        }
        return i;
    }
    
    public double[] getFeatureVector(){
        return x_vektor;
    }
    
    public double[] getValues(){
        return absVals;
    }

    public String toString()
    {
        String out = "[";
        for (int i=0;i<x_vektor.length;i++){
            out+=nf.format(x_vektor[i]);
            if(i==x_vektor.length-1)
                out+="]";
            else
                out+=";";
        }
        return out;
    }
    
    public static double range100(double val, double min, double max){
        return (100*(val-min))/(max-min);
    }
    
    public static double range01(double val, double min, double max){
        return (val-min)/(max-min);
    }
    
    public static double restoreValue01(double val01, double min, double max)
    {
        return val01*(max-min)+min;
    }
}