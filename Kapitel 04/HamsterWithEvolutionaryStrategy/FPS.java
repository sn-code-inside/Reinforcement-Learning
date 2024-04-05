import greenfoot.*;
import greenfoot.Color;

/**
 * Displays the number of frames per second on 
 * a given scenario.
 * 
 * @author Michael Berry (mjrb4)
 * @version 06/07/08
 */
public class FPS extends Actor
{
    /**
     * The two possbile modes the calculation can take.
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
     */
    public enum Mode {AVERAGE, REALTIME}
    /**
     * The mode that the FPS reading should take.
     * For most uses, leave this as Mode.AVERAGE.
     */
    public static final Mode mode = Mode.REALTIME;
    
    private static final Color textColor = Color.BLACK;
    private static final String prefix = "FPS: ";
    private static final int updateFreq = 20;
    private long countAct;
    private long prevTime;
    private double fps;
    
    /**
     * Create a new FPS reading.
     */
    public FPS()
    {
        countAct = 0;
        setImage(new GreenfootImage(150, 16));
        GreenfootImage image = getImage();
        updateImage("-");
    }
    
    /**
     * Set the value of the frames per second.
     * @param val the value to set the FPS to.
     */
    private void setFPS(Long val)
    {
        fps = val;
        updateImage(val.toString());
    }
    
    /**
     * Set the value of the frames per second.
     * @param val the value to set the FPS to.
     */
    private void setFPS(Double val)
    {
        fps = val;
        updateImage(val.toString());
    }
    
    /**
     * Get the number of frames per second.
     * @return the number of frames per second.
     */
    public double getFPS()
    {
        return fps;
    }
    
    /**
     * Calculate the FPS based on the mode, and
     * update the reading.
     */
    public void act()
    {
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
    }
    
    /**
     * Draw the image.<p>
     * Sven van Nigtevecht slightly changed this,
     * not so special though.
     * @param value the FPS value
     */
    private void updateImage(String value)
    {
        getImage().clear();
        GreenfootImage txt = new GreenfootImage(prefix +value, 16, textColor, null);
        getImage().drawImage(txt, 1, 1);
    }
    
    /**
     * Pauses the FPS, so it doesn't display a value anymore.
     * Call this from a World subclass when the scenario is paused,
     * like this:
     * <pre>
     * public void stopped()
     * {
     *     java.util.List<FPS> fpss = getObjects(FPS.class);
     *     for (FPS a : fpss) {
     *         a.pause();
     *      }
     *  }
     * </pre>
     * Starting it again works automatically.<p>
     * This was added by Sven van Nigtevecht.
     */
    public void pause()
    {
        updateImage("-");
    }
}