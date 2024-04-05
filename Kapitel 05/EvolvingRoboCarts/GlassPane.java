import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)
import greenfoot.Color;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * The GlassPane is used to augment the screen wit additional informations or visualizations (sensor beams, fps, states, results, ...). 
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
public class GlassPane extends Actor
{
    public static final float FONT_SIZE = 48.0f;
    public static int WIDTH = 0;
    public static int HEIGHT = 0;
    public static boolean ANN_SHOW_ACTIVATIONS = false; // Set true to display the neuron activations live.
    public final static Color SENSOR_COLOR = new Color( 255 ,255, 255, 200);
    
    /* Queue with lines to draw. */ 
    private static LinkedList <Integer> lineDrawList = new LinkedList <Integer>();
    
    /** Size of the visualization of the artificial neural net */
    public static int ANN_IMAGE_WIDTH = 400;
    public static int ANN_IMAGE_HEIGHT = 300;
    private static NeuralNetwork ann = null;
    private GreenfootImage imgAnn = null;
    public static ArrayList <IntegerVec2D> neuronXY;
    public static ArrayList <Integer> neuronDiameters;
    
    private int d_min, d_max;
    
    /**
     * The two possbile FPS display modes the calculation can take.
     * 
     * Average will display the number of frames that 
     * were displayed in the previous second, thereby
     * giving you an average FPS reading from the last
     * second.
     * 
     * Realtime will calculate the time difference 
     * between this frame and the last one and then
     * update the reading after each and every frame.
     * This is potentially more accurate if you want
     * to find accurateintermittent spikes in 
     * framerates, but less accurate for a simple reading.
     * 
     * The FPS routines are from Sven van Nigtevecht
     */
    public enum Mode {AVERAGE, REALTIME}
    
    /**
     * The mode that the FPS reading should take.
     * For most uses, leave this as Mode.AVERAGE.
     */
    public static final Mode mode = Mode.REALTIME;   
    private static final greenfoot.Color textColor = greenfoot.Color.BLACK;
    private static final String prefix = "FPS: ";
    private static final int updateFreq = 20;
    private static GreenfootImage imgFps = new GreenfootImage(prefix, 16, textColor, null);
    private long countAct;
    private long prevTime;
    private double fps;
    
    /**
     * Create a score board with dummy result for testing.
     */
    public GlassPane(int width, int height, int witdh_ann, int height_ann)
    {
        WIDTH=width;
        HEIGHT=height;
        ANN_IMAGE_WIDTH = witdh_ann;
        ANN_IMAGE_HEIGHT = height_ann;
        makeImage();
    }
    
    public void act(){
        countAct++;
        if (mode == Mode.REALTIME) {
            long gap = System.currentTimeMillis() - prevTime;
            if(gap != 0 && countAct%updateFreq == 0) {
                countAct = 0;
                setFPS((1.0 /gap) *1000.0);
            }
            prevTime = System.currentTimeMillis();
        } else if (mode == Mode.AVERAGE) {
            long curTime = System.currentTimeMillis();
            if(curTime >= prevTime +1000) {
                setFPS(countAct);
                prevTime = curTime;
                countAct = 0;
            }
        }
        if (ANN_SHOW_ACTIVATIONS) {
            neuronDiameters = ann.getNeuronDiameters();
        }
        updateImage();
    }
    
    public void setNeuralNetImage(NeuralNetwork ann){
        this.ann = ann;
        imgAnn = ann.produceVisualizationImage(ANN_IMAGE_WIDTH, ANN_IMAGE_HEIGHT,ANN_SHOW_ACTIVATIONS);
        neuronXY = ann.getNeuronXY();
    }

    private void makeImage(){
        GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);
        image.setColor(new Color(255,255,255, 128));
        setImage(image);
    }
    
    public void updateImage(){
        GreenfootImage img = getImage();
        img.clear();
        drawLineQueue(img);
        if (imgAnn!=null){
            int x_offset = WIDTH-ANN_IMAGE_WIDTH;
            img.drawImage(imgAnn,x_offset,0);
            if (ANN_SHOW_ACTIVATIONS && !neuronDiameters.isEmpty()) {
                int i = 0;
                for (IntegerVec2D n : this.neuronXY){ 
                    int d = neuronDiameters.get(i);
                    img.fillOval(x_offset+n.x-d/2,n.y-d/2,d,d);
                    i++;
                }
            }
        }
        img.drawImage(imgFps, 1, 1);
    }
    
    /**
     * Puts a line to the list of the to drawn lines.
     * @param x0 X-coordinate of the startpoint.
     * @param y0 Y-coordinate of the startpoint.
     * @param x1 X-coordinate of the endpoint.
     * @param y1 Y-coordinate of the endpoint.
     */
    public static void enqueueLine( int x0, int y0, int x1, int y1 ){
        lineDrawList.add(x0);
        lineDrawList.add(y0);
        lineDrawList.add(x1);
        lineDrawList.add(y1);
    }
    
    public static void drawLineQueue(GreenfootImage img){
        while (!lineDrawList.isEmpty()){
            int x0 = lineDrawList.removeFirst();
            int y0 = lineDrawList.removeFirst();
            int x1 = lineDrawList.removeFirst();
            int y1 = lineDrawList.removeFirst();
            img.drawLine(x0,y0,x1,y1);
        }
    }
    
    /* fps */
    /**
     * Set the value of the frames per second.
     * @param val the value to set the FPS to.
     */
    private void setFPS(Long val)
    {
        fps = val;
        imgFps = new GreenfootImage(prefix +val.toString(), 16, textColor, null);
    }
    
    /**
     * Set the value of the frames per second.
     * @param val the value to set the FPS to.
     */
    private void setFPS(Double val)
    {
        fps = val;
        imgFps = new GreenfootImage(prefix +val.toString(), 16, textColor, null);
    }
    
    /**
     * Get the number of frames per second.
     * @return the number of frames per second.
     */
    public double getFPS()
    {
        return fps;
    }   
}
