package player.weapon;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import general.Mechanics;

/**
 * Die abstrakte Pfeil-Klasse, von der die Pfeilarten abgeleitet werden. Wenn
 * Spieler Pfeile bekommen, bekommen sie Pfeil-Instanzen durch den Konstruktor
 * der Subklasse. Diese Objekte kommen in das Inventar des Spielers.
 * 
 * <b> Indexes: </b> Feuer = 0; Wasser = 1; Sturm = 2; Stein = 3; Eis = 4; Blitz
 * = 5; Licht = 6; Schatten = 7;
 * 
 * @version 25.11.2013
 */
public abstract class AbstractArrow extends RangedWeapon implements gui.Drawable {

	/**
	 * Wert des Schadens (nach Abnahme durch Entfernung)
	 */
	protected float attackValueCurrent;

    /** the rotation of the BufferedImage to draw the arrow to the direction of the attacked field */
    protected double rotation;

	protected double speed;

	/**
	 * Wert in 'float', wie stark der Pfeil an Schaden verliert : <b> jeweils
	 * nach 25m <b>
	 * 
	 * Prozentsatz (0.1f = 10%), was dem Pfeil an Schaden abgezogen wird zählt
	 * erst nach 25m Entfernung
	 * */
	protected float damageLosingRate;

	/** Wahrscheinlichkeit mit der Man sich selbst trifft */
	protected float selfHittingRate;

	/**
	 * Treffunsicherheitsquote (Grundwert) Je h�her, desto unwahrscheinlicher
	 * ein Treffer Je weiter die Distance, desto unwahrscheinlicher ein Treffer
	 */
	protected float aimMissing;

	/** Treffunsicherheitsquote: Faktor der Erh�hung (pro 25m) */
	protected float aimMissingRate;

	/** aktuelle Treffunsicherheitsquote */
	protected float aimMissingCurrent;

	/**
	 * Reichweite des Pfeils : in Metern angegeben [25m genau] Verwenden, falls
	 * andere Pfeile [d.h. Sturmpfeil] oder Felder / andere Eigenschaften die
	 * Reichweite ver�ndern
	 */
	protected int rangeValueCurrent;

	/**
	 * Wie weit der Pfeil bisher geflogen ist : in Metern angegeben Feld: [auf
	 * 25m genau] Laenge = 100 Breite = 100
	 */
	protected int distanceReached;

	 /** Wie Schnel sich der Pfeil bewegt */
	protected int arrowSpeed;
	//
	// /** Wie gro� seine Beschleunigung ist */
	// protected int acceleration;
	
	/** Wie weit der Schaden des Pfeils nach dem Auftreffen reicht */
	protected int damageRadius;

	/** Positon im Koordinatensystem des Pfeils: Die X-Position */
	protected int fieldX;
	/** Position im Koordinatensystem des Pfeils: Y-Position */
	protected int fieldY;
	/** Nummer des Feldes über das sich der Pfeil befindet */
	protected int fieldNr;
	/** X-Position des Pfeils - f�r GUI */
	protected int posX;
	/** Y-Position des Pfeils - f�r GUI */
	protected int posY;
	/**
	 * X-Positon im Koordinatensystem der Felder: Hier ist die Position X des
	 * Zielfeldes
	 */
	protected int fieldXAim;
	/**
	 * Y-Positon im Koordinatensystem der Felder: Hier ist die Position Y des
	 * Zielfeldes
	 */
	protected int fieldYAim;

    /** X-Position des Ziels auf Bildschirm */
    protected int posXAim;

    /** Y-Poition auf Bildschirm des Ziels */
    protected int posYAim;

	/**
	 * <b> KONSTUCKTOR: <b> float : Grundschaden des Pfeils float :
	 * Grundverteidigung des Pfeils int : Grundreichweite des Pfeils float :
	 * Selbsttrefferwahrscheinlichkeit float : Treffunsicherheitsquote float :
	 * Faktor der Erh�hung der Treffunsicherheitsquote float : Faktor der
	 * Verringerung der Schadenswirkung
	 * 
	 * // int : arrowSpeed - Geschwindigkeit des Pfeils // int : acceleration -
	 * Beschleunigung des Pfeils // int : damageRadius - Schadensradius des
	 * Pfeils
	 */
	public AbstractArrow(float attackVal, float defenseVal, int rangeVal,
			float selfHittingRate, float aimMissing, float aimMissingRate,
			float damageLosingRate, double speed, String name) {
		super(name);
		setAttackValue(attackVal);
		setAttackValCurrent(attackVal);
		setDefenseValue(defenseVal);
		// Reichweite des Pfeils wird minimal (+/- 1 Feld) an die Entfernung
		// angepasst
		if (Mechanics.worldSizeX <= 7) {
			setRange(rangeVal - 100);
			setRangeValueCurrent(rangeVal - 100);
		} else if (Mechanics.worldSizeX > 7 && Mechanics.worldSizeX <= 17) {
			setRange(rangeVal);
			setRangeValueCurrent(rangeVal);
		} else {
			setRange(rangeVal + 100);
			setRangeValueCurrent(rangeVal + 100);
		}
		setSelfHittingRate(selfHittingRate);
		setAimMissing(aimMissing);
		setAimMissingRate(aimMissingRate);
		setAimMissingCurrent(aimMissing);
		setDamageLosingRate(damageLosingRate);
		setDistanceReached(0);
		setSpeed(speed);

		// TODO: arrowSpeed in subclasses of AbstractArrow
		// TODO: damageRadius
	}

	/** Gibt den Aktuellen Wert des Schadens zur�ck */
	public float getAttackValCurrent() {
		return attackValueCurrent;
	}

	/** Setzt den Aktuellen Wert des Schadens (nicht Grundschaden) zur�ck */
	public void setAttackValCurrent(float newAtackValueCurrent) {
		attackValueCurrent = newAtackValueCurrent;
	}

	/** Gibt die Schadensverlustrate zur�ck */
	public float getDamageLosingRate() {
		return damageLosingRate;
	}

	/** setzt die Schadensverlustrate (nur in dieser Klasse aufrufbar!) */
	private void setDamageLosingRate(float newDamageLosingRate) {
		this.damageLosingRate = newDamageLosingRate;
	}

	/** Selbsttrefferrate */
	public float getSelfHittingRate() {
		return this.selfHittingRate;
	}

	/** Selbsttrefferrate setzen */
	private void setSelfHittingRate(float newSelfHittingRate) {
		this.selfHittingRate = newSelfHittingRate;
	}

	/** Treffunsicherheitsquote (Grundwert) */
	public float getAimMissing() {
		return this.aimMissingRate;
	}

	/** Treffunsicherheitsquote (Grundwert) */
	private void setAimMissing(float newAimMissing) {
		this.aimMissingRate = newAimMissing;
	}

	/** Treffunsicherheitsquote (Faktor der Erh�hung) */
	public float getAimMissingRate() {
		return this.aimMissingRate;
	}

	/** Treffunsicherheitsquote (Faktor der Erh�hung) */
	private void setAimMissingRate(float newAimMissingRate) {
		this.aimMissingRate = newAimMissingRate;
	}

	/**
	 * Treffunsicherheitsquote (aktueller Wert =! Grundwert [Erh�hung durch
	 * Entfernung])
	 */
	public float getAimMissingCurrent() {
		return this.aimMissingRate;
	}

	/**
	 * Treffunsicherheitsquote (aktueller Wert [nicht gleich Grundwert; Erh�hung
	 * durch Entfernung]
	 */
	public void setAimMissingCurrent(float newAimMissingCurrent) {
		this.aimMissingRate = newAimMissingCurrent;
	}

    /** returns the current attack value of the arrow */
    @Override
    public float getAttackValue () {
        return attackValueCurrent;
    }

    /** TODO: Defense Value is independed from the attacking arrow */
    @Override
    public float getDefenseValue () {
        return super.getDefenseValue();
    }

    /**
	 * Aktuelle Reichweite {vom Start zum Ziel} (nicht Grundweite: sie wurde
	 * ggf. durch andere Pfeile,... ge�ndert) in 25m genau
	 */
	public int getRangeValueCurrent() {
		return rangeValueCurrent;
	}

	public void setRangeValueCurrent(int newRangeValueCurrent) {
		rangeValueCurrent = newRangeValueCurrent;
	}

	/** Zur�ckgelgete Distanz des Pfeils [in 25m genau] */
	public int getDistanceReached() {
		return distanceReached;
	}

	/**
	 * Setzt: Zur�ckgelgete Distanz des Pfeils [in 25m genau]
	 * 
	 * @param newDistanceReached
	 *            (int-Wert)
	 */
	public void setDistanceReached(int newDistanceReached) {
		distanceReached = newDistanceReached;
	}

	/** X-Position bei den Feldern */
	public int getFieldX() {
		return fieldX;
	}

	/** X-Position bei den Feldern */
	public void setFieldX(int fieldX) {
		this.fieldX = fieldX;
	}

	/** Y-Position bei den Feldern */
	public int getFieldY() {
		return fieldY;
	}

	/** Y-Position bei den Feldern */
	public void setFieldY(int fieldY) {
		this.fieldY = fieldY;
	}

	/** Gibt die Feldnummer des Feldes zur�ck, auf dem der Pfeil ist */
	public int getFieldNr() {
		return fieldNr;
	}

	/**
	 * Setzt die Feldnummer neu SETZT DAMIT AUCH EINE NEUE POSITON: die Position
	 * des Feldes der 'int fieldNr'
	 */
	public void setFieldNr(int fieldNr) {
		this.fieldNr = fieldNr;
	}

	/**
	 * Gibt die Feldposition X des Feldes (im Field-Koordinatensystem) zur�ck,
	 * dessen Ziel (Zielfeld) der Pfeil anvisiert hat
	 */
	public int getFieldXAim() {
		return fieldXAim;
	}

	/**
	 * Seitzt die Feldposition X des Feldes (aus dem Field-Koordinatensystem)
	 * zur�ck, dessen Ziel (Zielfeld) der anvisiert hat
	 */
	public void setFieldXAim(int fieldXAim) {
		this.fieldXAim = fieldXAim;
	}

	/**
	 * Gibt die Feldposition Y des Feldes (im Field-Koordinatensystem) zur�ck,
	 * dessen Ziel (Zielfeld) der Pfeil anvisiert hat
	 */
	public int getFieldYAim() {
		return fieldYAim;
	}

	/**
	 * Seitzt die Feldposition Y des Feldes (aus dem Field-Koordinatensystem)
	 * zur�ck, dessen Ziel (Zielfeld) der anvisiert hat
	 */
	public void setFieldYAim(int fieldYAim) {
		this.fieldYAim = fieldYAim;
	}

	/** Position X auf dem Bildschrim (Pixel) */
	public int getPosX() {
		return posX;
	}

	/** Position X auf dem Bildschirm (Pixel) */
	public void setPosX(int posX) {
		this.posX = posX;
	}

	/** Position Y auf dem Bildschirm (Pixel) */
	public int getPosY() {
		return posY;
	}

	/** Position Y auf dem Bildschirm (Pixel) */
	public void setPosY(int posY) {
		this.posY = posY;
	}

    /** gibt die X-Position auf dem Bildschirm vom Ziel zurück. It is equal to the Center-X-Position of the aimed tile minus half the arrowImage.getWidth()*/
    public int getPosXAim () {
        return posXAim;
    }

    /** setzt die x-Position auf dem Bildschirm vom Ziel */
    public void setPosXAim (int posXAim) {
        this.posXAim = posXAim;
    }

    /** Y-Position auf dem Bildschrim vom Ziel */
    public int getPosYAim () {
        return posYAim;
    }

    /** setzt die y-Position auf dem Bildschrim vom Ziel
     * <b> use calculateRotation if neccary </b>*/
    public void setPosYAim (int posYAim) {
        this.posYAim = posYAim;
    }

    /** the speed of the arrow in tiles per turn */
	public double getSpeed() {
		return speed;
	}

	protected void setSpeed(double speed) {
		this.speed = speed;
	}

    @Override
    public int getRange () {
        return super.getRange();
    }

    /** returns the roation of the BufferedImage. With this value the image is drawn in direction to the aim. It's in radient.*/
    public double getRotation () {
        return rotation;
    }

    /** rotates the BufferedImage to <code>rotation</code> so that the arrow is aim to the posXAim/posYAim
     * the value is calculated with the x and y position of this arrow as well as the x and y position of the aim. If
     * they are both not inizalized yet, it's pretty useless to call this method. */
     public void calculateRotation () {
         if (posXAim > posX && posYAim < posY) {
             rotation = Math.atan((double) (posXAim - posX) / (double) (posY - posYAim));
         } else if (posXAim > posX && posYAim > posY) {
             rotation = Math.toRadians(90.0) + Math.atan((double) (posYAim - posY) / (double) (posXAim - posX));
         } else if (posXAim < posX && posYAim > posY) {
             rotation = - (Math.toRadians(90.0) + Math.atan((double) (posYAim - posY) / (double) (posX - posXAim)));
         } else if (posXAim < posX && posYAim < posY) {
             rotation = - Math.atan((double) (posX - posXAim) / (double) (posY - posYAim));
         } else { // the special cases, where the point is placed either on the same x-position or on the same y-position
             if (posYAim > posY && posXAim == posX) // the arrow is turned around
                 rotation = Math.toRadians(180.0);
             else if (posXAim - posX > 0 && posYAim == posY) // the arrow needs to be rotated clockwise (horizontally on the ground)
                 rotation = Math.toRadians(90.0);
             else if (posXAim - posX < 0 && posYAim == posY) // the arrow needs to be rotated counterclockwise (horizontally on the ground)
                 rotation = - Math.toRadians(90.0);
             else if (posYAim < posY && posXAim == posX)// the aim is directly over the tile, so no rotation is needed.
                 rotation = 0;
             else
                 throw new RuntimeException("the X/Y-Position of the arrow and the aim is not possible. positionOfArrow: " + "(" + posX + "|" + posY + ")" + " positionOfAim: " + "(" + posXAim + "|" + posYAim + ").");
         }
    }

    /** gibt die BufferedImage des Pfeils zur�ck
	 * @see <code> ArrowHelper.getArrowImage(int selectedIndex) </code> */
	public abstract BufferedImage getImage();

    /** TODO: do the Zoom */
	@Override
	public void draw(Graphics2D g) {
        AffineTransform old = g.getTransform();
        // it should be rotated from the center of the arrowImage
        g.rotate(getRotation(), getPosX() + (int) (0.5 * getImage().getWidth()), getPosY() + (int) (0.5 * getImage().getHeight()));
		g.drawImage(getImage(), getPosX(), getPosY(), getImage().getWidth(), getImage().getHeight(), null);
        g.setTransform(old);
	}
}

// /** Setzt die Feldnummer neu;
// * Der neue Wert wird automaisch aus den Werten f�r die X- und Y- Position in
// den Feldkoordinaten berechnet */
// public void reFreshFieldNr () {
// this.fieldNr = Field.getFieldNrFromPos(fieldX, fieldY);
// }
// /** setzt die X UND Y Positon bei den Feldern neu;
// * Der neue Wert wird automatisch aus der FieldNr dieses Pfeils berechnet
// */
// public void reFreshPosXYfield () {
// this.posX = com.github.pfeile.gui.Field.getPosXFromFieldNr (fieldNr);
// this.posY = com.github.pfeile.gui.Field.getPosYFromFieldNr (fieldNr);
// }
// --> acceleration
// --> damageRadius

