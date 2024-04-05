/**
 * Oberklasse aller Exception-Klassen des Java-Hamster-Modells. Bei allen
 * Exceptions des Java-Hamster-Modells handelt es sich um Unchecked-Exception,
 * die nicht unbedingt abgefangen bzw. deklariert werden muessen.
 * 
 * @author Dietrich Boles (Universitaet Oldenburg)
 * @version 2.0 (07.06.2008)
 * 
 */
public class HamsterException extends RuntimeException {

	/**
	 * Hamster, der die Exception verschuldet hat
	 */
	private Hamster hamster;

	/**
	 * Konstruktor, der die Exception mit dem Hamster initialisiert, der die
	 * Exception verschuldet hat.
	 * 
	 * @param hamster
	 *            der Hamster, der die Exception verschuldet hat
	 */
	public HamsterException(Hamster hamster) {
		super("");
		this.hamster = hamster;
	}

	/**
	 * liefert den Hamster, der die Exception verschuldet hat
	 * 
	 * @return der Hamster, der die Exception verschuldet hat
	 */
	public Hamster getHamster() {
		return this.hamster;
	}
}
