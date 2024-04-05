import greenfoot.Actor;
import greenfoot.World;

import java.util.List;

/**
 * Representation of walls in the "java-hamster-model"
 * 
 * @author Dietrich Boles (Universitaet Oldenburg)
 * @version 2.0 (07.06.2008)
 * 
 */
public class Wall extends Actor {

    public Wall() {
        setImage("mauer.png");
    }

    protected void addedToWorld(World world) {
        removeObjectsOnCell(getX(), getY());
    }
    
    public void setLocation(int x, int y) {
        if (getX() == x && getY() == y) {
            return;
        }
        removeObjectsOnCell(x, y);
        super.setLocation(x, y);
    }
    
    private void removeObjectsOnCell(int x, int y) {
        List l = getWorld().getObjectsAt(x, y, null);
        for (int i = 0; i < l.size(); i++) {
            Actor actor = (Actor) l.get(i);
            if (actor != this) {
                getWorld().removeObject(actor);
            }
        }
    }
}