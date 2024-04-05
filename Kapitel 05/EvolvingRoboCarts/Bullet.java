import greenfoot.*;  // (World, Actor, GreenfootImage, and Greenfoot)

/**
 * A bullet that can hit rocks (not used).
 * 
 * @author Poul Henriksen
 */
public class Bullet extends SmoothMover
{
    /** The damage this bullet will deal */
    private static final int damage = 16;
    
    /** A bullet looses one life each act, and will disappear when life = 0 */
    private int life = 30;
    
    public Bullet()
    {
    }
    
    public Bullet(Vector speed, int rotation)
    {
        super(speed);
        setRotation(rotation);
        addForce(new Vector(rotation, 15));
        Greenfoot.playSound("EnergyGun.wav");
    }
    
    /**
     * The bullet will damage Korns if it hits them.
     */
    public void act()
    {
        if(life <= 0) {
            getWorld().removeObject(this);
        } 
        else {
            life--;
            move();
            checkTargetHit();
        }
    }
    
    /**
     * Check whether we have hit an rocks.
     */
    private void checkTargetHit()
    {
        Rock target = (Rock) getOneIntersectingObject(Rock.class);
        if (target != null){
            getWorld().removeObject(this);
            target.hit(damage);
        }
    }
}