import greenfoot.Actor;
import greenfoot.World;
import greenfoot.Greenfoot;
import java.util.List;
/**
 * The class is a representation of the hamster territory in the
 * "java-hamster-model".
 * 
 * @author Dietrich Boles ( University of Oldenburg, https://uol.de/en/dietrich-boles ;April,2021 )
 * @version 2.0 (07.06.2008)
 */
public class Territory extends World {

    /**
     * Creates a new territory with 10x10 tiles
     */
    public Territory() {
        this(10, 10);
    }

    /**
     * Creates a new territory in the specified size.
     * 
     * @param rows  number of rows
     * @param columns number of columns
     */
    public Territory(int rows, int columns) {
        super(columns < 1 ? 10 : columns, rows < 1 ? 10 : rows, 35);
        setBackground("kachel.jpg");
        setPaintOrder(Wall.class, Hamster.class, Grain.class);
    }

    /**
     * Returns the number of rows in the territory.
     * 
     * @return number of rows in the territory
     */
    public int getNumberOfRows() {
        return getHeight();
    }

    /**
     * Returns the number of columns in the territory.
     * 
     * @return number of columns in the territory
     */
    public int getNumberOfColumns() {
        return getWidth();
    }

    /**
     * Checks if there is a wall on the tile (row/column); It returns true exactly if 
     * there is a wall on the given tile or if the given values are outside the territory.
     * 
     * @param row
     *            row of the tile
     * @param column
     *            column ofd the tile
     * @return true if there is a wall on the given tile or if the given values are outside 
     *         the territory; otherwise false
     */
    public boolean wallThere(int row, int column) {
        return getObjectsAt(column, row, Wall.class).size() > 0;
    }

    /**
     * Returns the total number of grains lying around on tiles in the territory.
     * 
     * @return the total number of grains lying around on tiles in the territory
     */
    public int getNumberOfGrains() {
        int num = 0;
        for (int r = 0; r < getNumberOfRows(); r++) {
            for (int s = 0; s < getNumberOfColumns(); s++) {
                num += getNumberOfGrains(r, s);
            }
        }
        return num;
    }

    /**
     *  Returns the number of grains on the tile (row/column) or 0 if the tile does
     *  not exist or is blocked by a wall
     * 
     * @param row
     *             row of the tile
     * @param column
     *            column of the tile
     * @return the number of grains on the tile (row/column) or 0 if the tile does
     *  not exist or is blocked by a wall
     */
    public int getNumberOfGrains(int row, int column) {
        List actors = getObjectsAt(column, row, Grain.class);
        if (actors == null || actors.size() == 0) {
            return 0;
        }
        return ((Grain)actors.get(0)).getNumber();
    }

    /**
     * Returns the total number of existing hamsters in the territory.
     * 
     * @return total number of existing hamsters in the territory
     */
    public int getAnzahlHamster() {
        return getObjects(Hamster.class).size();
    }

    /**
     * Returns all existing hamsters in the territory
     * 
     * @return All existing hamsters in the territory
     */
    public Hamster[] getHamster() {
        return (Hamster[]) getObjects(Hamster.class).toArray(new Hamster[0]);
    }

    /**
     *  Returns the number of hamsters on the tile (row/column) or 0 if the 
     *  tile does not exist or is blocked by a wall.
     * 
     * @param row
     *            row of the tile
     * @param column
     *            column of the tile
     * @return the number of hamsters on the tile (row/column) or 0 if the 
     *          tile does not exist or is blocked by a wall
     */
    public int getNumberOfHamsters(int row, int column) {
        return getObjectsAt(column, row, Hamster.class).size();
    }

    /**
     * Returns all hamsters currently on the tile (row/column) (incl. the default hamster).
     * 
     * @param row
     *             row of the tile
     * @param column
     *             column of the tile
     * @return all hamsters currently on the tile (row/column)
     */
    public Hamster[] getHamster(int row, int column) {
        return (Hamster[]) getObjectsAt(column, row, Hamster.class).toArray(
                new Hamster[0]);
    }

    /**
     * Inhabits the territory with a fixed population.
     */
    public void generatePopulation() {
        Hamster h1 = new Hamster();
        h1.turnLeft();
        h1.turnLeft();
        h1.turnLeft();
        addObject(h1, 0, 0);

        Hamster h2 = new Hamster();
        h2.turnLeft();
        addObject(h2, getWidth() - 1, getHeight() - 1);

        Hamster h3 = new Hamster();
        addObject(h3, getWidth() / 2, getHeight() / 2);

        generateGrains(getWidth() * getHeight() / 2);
    }

    /**
     * Randomly places a specified number of grains in the territory.
     * 
     * @param howMany
     *            Number of grains to be placed
     */
    public void generateGrains(int howMany) {
        for (int i = 0; i < howMany; i++) {
            Grain korn = new Grain();
            int x = Greenfoot.getRandomNumber(getWidth());
            int y = Greenfoot.getRandomNumber(getHeight());
            addObject(korn, x, y);
        }
    }
}