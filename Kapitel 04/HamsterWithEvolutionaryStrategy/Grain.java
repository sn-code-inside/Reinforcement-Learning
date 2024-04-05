import greenfoot.Actor;
import greenfoot.World;

import java.util.List;

/**
 * Representation of grain piles in the "java-hamster-model"
 * 
 * @author Dietrich Boles ( University of Oldenburg, https://uol.de/en/dietrich-boles ;April,2021 )
 * @version 2.0 (07.06.2008)
 * 
 */
public class Grain extends Actor {

	private int number;

	public Grain() {
	    this(1);
	}
	
	public Grain(int number) {
	    this.number = number;
	}

	// wird aufgerufen, wenn das Korn in das Territorium platziert wird
	protected void addedToWorld(World world) {

		// Wenn auf der Kachel schon eine Mauer ist, wird das Korn wieder
		// entfernt
		if (getWorld().getObjectsAt(getX(), getY(), Wall.class).size() > 0) {
			getWorld().removeObject(this);
			return;
		}
		
	    // bereits Korn auf Kachel?
	    List koerner = getWorld().getObjectsAt(getX(), getY(), Grain.class);
	    if (koerner.size() > 1) {
	        Grain korn = (Grain)koerner.get(0);
            if (korn == this) {
                korn = (Grain)koerner.get(1);
            }
	        getWorld().removeObject(korn);
	        this.number += korn.number;
	     }

		setImage("korn" + Math.min(number, 12) + ".png");
		// es werden maximal 12 Koerner angezeigt
	}

	// liefert die Information, das wie vielte Korn dieses Korn auf der Kachel ist
	protected int getNumber() {
		return number;
	}
	
	public void setLocation(int x, int y) {
	    if (getX() == x && getY() == y) {
	        return;
	    }

	    // Mauer auf Kachel?
	    if (getWorld().getObjectsAt(x, y, Wall.class).size() > 0) {
	         return;
	    }
	    
	    // bereits Korn auf Kachel?
	    List koerner = getWorld().getObjectsAt(x, y, Grain.class);
	    if (koerner.size() > 0) {
	        
	        Grain korn = (Grain)koerner.get(0);
	        getWorld().removeObject(korn);
	        this.number += korn.number;
	        setImage("korn" + Math.min(number, 12) + ".png");
	         
	     }
	    	        	   
	     super.setLocation(x, y);
	}
	
	void incNumber(int n) {
	    this.number += n;
	    if (this.number <= 0) {
	        getWorld().removeObject(this);
	    } else {
	        setImage("korn" + Math.min(number, 12) + ".png");
	    }
	}
	
}