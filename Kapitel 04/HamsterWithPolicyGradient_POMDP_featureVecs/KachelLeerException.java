/**
 * Hamster-Exception die den Fehler repraesentiert, dass fuer einen Hamster auf
 * einer Kachel ohne Koerner die Methode nimm aufgerufen wird.
 * 
 * @author Dietrich Boles (Universitaet Oldenburg)
 * @version 2.0 (07.06.2008)
 * 
 */
public class KachelLeerException extends HamsterException {

	private int reihe;

	private int spalte;

	/**
	 * Konstruktor, der die Exception mit dem die Exception verschuldenden
	 * Hamster und den Koordinaten der koernerlosen Kachel initialisiert.
	 * 
	 * @param hamster
	 *            der Hamster, der die Exception verschuldet hat
	 * @param reihe
	 *            Reihe der koernerlosen Kachel
	 * @param spalte
	 *            Spalte der koernerlosen Kachel
	 */
	public KachelLeerException(Hamster hamster, int reihe, int spalte) {
		super(hamster);
		this.reihe = reihe;
		this.spalte = spalte;
	}

	/**
	 * liefert die Reihe der koernerlosen Kachel
	 * 
	 * @return die Reihe der koernerlosen Kachel
	 */
	public int getReihe() {
		return this.reihe;
	}

	/**
	 * liefert die Spalte der koernerlosen Kachel
	 * 
	 * @return die Spalte der koernerlosen Kachel
	 */
	public int getSpalte() {
		return this.spalte;
	}

	/**
	 * liefert eine der Exception entsprechende Fehlermeldung
	 * 
	 * @return Fehlermeldung
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		return "Auf der Kachel (" + reihe + "," + spalte
				+ ") liegen keine Koerner!";
	}
}
