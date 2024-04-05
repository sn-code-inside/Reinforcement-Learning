import greenfoot.Actor;
import greenfoot.World;
import greenfoot.Greenfoot;
import greenfoot.GreenfootImage;

import java.util.List;
import java.awt.Color;


/**
 * Representation of object-oriented hamsters in the java hamster model.
 * 
 * @author Dietrich Boles ( University of Oldenburg, https://uol.de/en/dietrich-boles ;April,2021 )
 * @version 2.0 (07.06.2008)
 */
public class Hamster extends Actor {

    /**
     * view direction north
     */
    public final static int NORTH = 0;

    /**
     * view direction east
     */
    public final static int EAST = 1;

    /**
     * view direction south
     */
    public final static int SOUTH = 2;

    /**
     * view direction west
     */
    public final static int WEST = 3;

    private int directionOfView;

    private int grainsInJaws;

    /**
     * Constructor to create a new hamster with facing EAST and no grain in the mouth.
     */
    public Hamster() {
        setImage("hamster.png");
        setDirectionOfView(EAST);
        grainsInJaws = 0;
    }
    
    
    /**
     * Constructor to create a new hamster with the specified 
     * direction of view and number of nuclei in the mouth
     *
     * @param direction
     *            direction of view of the hamster
     * @param numberOfGrains
     *            number of grains in the jaws
     */
    public Hamster(int direction, int numberOfGrains) {
        setImage("hamster.png");
        setDirectionOfView(direction);
        grainsInJaws = numberOfGrains;
    }

    /**
     * do something
     */
    public void act() {
        if (isFrontFree()) {
            goAhead();
            if (isGrainThere()) {
                take();
            }
        } else {
           turnLeft();
        }
    }

    /**
     * returns true if there is no wall in front of the called hamster in the 
     * direction of view 
     * (if the hamster is at the edge of the territory in the direction of view, 
     * false is returned)
     * 
     * @return true, if there is no wall in front of the called hamster (direction of view);
     *               otherwise false
     */
    public boolean isFrontFree() {
        int x = getX();
        int y = getY();
        switch (directionOfView) {
        case SOUTH:
            y++;
            break;
        case EAST:
            x++;
            break;
        case NORTH:
            y--;
            break;
        case WEST:
            x--;
            break;
        }

        if (x >= getWorld().getWidth() || y >= getWorld().getHeight() || x < 0
                || y < 0) {
            return false;
        }

        //return getWorld().getObjectsAt(x, y, Wall.class).size() == 0;
        return ((RL_GridEnv_FV)getWorld()).isStatePossible(x,y); // faster
    }

    /**
     * Returns true, if there is at least one grain on the tile on which 
     * the called hamster is currently located.
     * 
     * @return true, if there is at least one grain on the tile on which 
     * the called hamster is currently located, false otherwise
     */
    public boolean isGrainThere() {
        return getWorld().getObjectsAt(getX(), getY(), Grain.class).size() > 0;
    }

    /**
     * returns true if the called hamster has no grains in its jaws.
     * 
     * @return true, if the called hamster has no grains in its jaws,
     *         false otherwise
     */
    public boolean jawsEmpty() {
        return grainsInJaws == 0;
    }

    /**
     *  The called hamster hops onto the tile in front of it in the direction of view.
     * 
     * @throws MauerDaException is thrown if the tile in the direction of view in 
     * front of the hamster is blocked by a wall or the hamster in the direction of
     * view is at the edge of the territory.
     *             
     */
    public void goAhead() throws MauerDaException {
        if (!isFrontFree()) {
            throw new MauerDaException(this, getY(), getX());
        }
        switch (directionOfView) {
        case SOUTH:
            setLocation(getX(), getY() + 1);
            break;
        case EAST:
            setLocation(getX() + 1, getY());
            break;
        case NORTH:
            setLocation(getX(), getY() - 1);
            break;
        case WEST:
            setLocation(getX() - 1, getY());
            break;
        }
    }

    /**
     * The called hamster turns left.
     */
    public void turnLeft() {
        switch (directionOfView) {
        case SOUTH:
            setDirectionOfView(EAST);
            break;
        case EAST:
            setDirectionOfView(NORTH);
            break;
        case NORTH:
            setDirectionOfView(WEST);
            break;
        case WEST:
            setDirectionOfView(SOUTH);
            break;
        }
    }

    /**
     * The called hamster grabs a grain on the tile it is currently on.
     * 
     * @throws KachelLeerException is thrown if there is no grain on 
     *          the tile on which the hamster is currently located
     *             
     */
    public void take() throws KachelLeerException {
        if (!isGrainThere()) {
            throw new KachelLeerException(this, getY(), getX());
        }
        grainsInJaws++;
        Grain korn = (Grain)getWorld().getObjectsAt(getX(), getY(), Grain.class).get(0);
        korn.incNumber(-1);
    }
    
    /**
     * The called hamster places a grain on the tile it is currently on.
     * 
     * @throws MaulLeerException is thrown when the hamster has no grains
     *                           in its jaws
     *             
     */
    public void give() throws MaulLeerException {
        if (jawsEmpty()) {
            throw new MaulLeerException(this);
        }
        grainsInJaws--;
        getWorld().addObject(new Grain(), getX(), getY());

    }

    /**
     * returns the row of the tile of the territory on which the called hamster is currently located.
     * 
     * @return row of the tile of the territory on which the called hamster is currently located.
     */
    public int getRow() {
        return getY();
    }

    /**
     * returns the column of the tile of the territory on which the called hamster is currently located.
     * 
     * @return column of the tile of the territory on which the called hamster is currently located.
     */
    public int getColumn() {
        return getX();
    }

    /**
     * returns the direction in which the called hamster is currently looking (the values returned 
     * correspond to the constants above)
     * 
     * @return direction in which the called hamster is currently looking 
     */
    public int getDirectionOfView() {
        return directionOfView;
    }

    /**
     * returns the number of grains that the called hamster currently has in its mouth
     * 
     * @return number of grains that the called hamster currently has in its mouth
     */
    public int getGrainsInJaws() {
        return grainsInJaws;
    }

    /**
     * returns the total number of existing hamsters in the territory
     * 
     * @return the total number of existing hamsters in the territory
     */
    public int getNumberOfHamsters() {
        return getWorld().getObjects(Hamster.class).size();
    }

    // copy-constructor
    private Hamster(Hamster h) {
        setImage(h.getImage());
        setDirectionOfView(h.directionOfView);
        grainsInJaws = h.grainsInJaws;
    }

    /**
     * Sets the direction of view.
     * @param richtung direction of view
     */
    protected void setDirectionOfView(int richtung) {
        directionOfView = richtung;
        switch (directionOfView) {
        case SOUTH:
            setRotation(90);
            break;
        case EAST:
            setRotation(0);
            break;
        case NORTH:
            setRotation(270);
            break;
        case WEST:
            setRotation(180);
            break;
        default:
            break;
        }
    }

    /**
     * Is called when the hamster is placed in the territory.
     * @param world environment of the hamster (greenfoot gridworld)
     */
    @Override
    protected void addedToWorld(World world) {
        // Hamster kann nicht auf Mauer platziert werden
        if (getWorld().getObjectsAt(getX(), getY(), Wall.class).size() > 0) {
            getWorld().removeObject(this);
            return;
        }
    }
    
    /**
     * Do only if there is no wall on the tile.
     */
    @Override
    public void setLocation(int x, int y) {
       // if (getWorld().getObjectsAt(x, y, Wall.class).size() == 0) 
       if (((RL_GridEnv_FV)getWorld()).isStatePossible(x,y))  // Much faster, because it uses the arena string-array. (mod. by Lorenz 06/2021)
       {
           super.setLocation(x, y);
       }
    }
}