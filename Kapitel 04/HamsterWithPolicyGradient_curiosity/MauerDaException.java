/**
 * Hamster-Exception die den Fehler repraesentiert, dass fuer einen Hamster, der
 * vor einer Mauer steht, die Methode vor aufgerufen wird auf.
 * 
 * @author Dietrich Boles (Universitaet Oldenburg)
 * @version 2.0 (07.06.2008)
 * 
 */
public class MauerDaException extends HamsterException {

	private int reihe;

	private int spalte;

	/**
	 * Konstruktor, der die Exception mit dem die Exception verschuldenden
	 * Hamster und den Koordinaten der durch eine Mauer belegten Kachel
	 * initialisiert.
	 * 
	 * @param hamster
	 *            der Hamster, der die Exception verschuldet hat
	 * @param reihe
	 *            Reihe der Mauer-Kachel
	 * @param spalte
	 *            Spalte der Mauer-Kachel
	 */
	public MauerDaException(Hamster hamster, int reihe, int spalte) {
		super(hamster);
		this.reihe = reihe;
		this.spalte = spalte;
	}

	/**
	 * liefert die Reihe, in der die Mauer steht
	 * 
	 * @return die Reihe, in der die Mauer steht
	 */
	public int getReihe() {
		return this.reihe;
	}

	/**
	 * liefert die Spalte, in der die Mauer steht
	 * 
	 * @return die Spalte, in der die Mauer steht
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
		return "Die Kachel (" + reihe + "," + spalte
				+ ") ist durch eine Mauer blockiert!";
	}
}
