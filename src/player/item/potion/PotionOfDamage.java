package player.item.potion;

import general.LogFacility;
import general.Main;
import newent.CommandTeam;
import scala.runtime.AbstractFunction0;
import scala.runtime.BoxedUnit;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * This Potion increases the damage of every unit slightly.
 */
public class PotionOfDamage extends Potion {

    /** the possible images of a potion: length is 3 because of three possible levels */
    private static BufferedImage[] images;

    static {
        images = new BufferedImage[3];

        String generalPath = "resources/gfx/item textures/potion textures/potionOfDamage";
        String path = "<No path selected>";
        try {
            path = generalPath + "[0].png";
            images[0] = ImageIO.read(PotionOfDamage.class.getClassLoader().getResourceAsStream(path));
            path = generalPath + "[1].png";
            images[1] = ImageIO.read(PotionOfDamage.class.getClassLoader().getResourceAsStream(path));
            path = generalPath + "[2].png";
            images[2] = ImageIO.read(PotionOfDamage.class.getClassLoader().getResourceAsStream(path));

        } catch (IOException e) {
            e.printStackTrace();
            LogFacility.log("Image of PotionOfDamage could not be loaded: " + path);
        }
    }

    /**
     * The default constructor with the <code>name</code> and the <code>level = 0</code> and with a
     * default PotionUI {@link player.item.potion.PotionUI#PotionUI()}
     *
     * @see player.item.potion.Potion#Potion(byte, String)
     */
    public PotionOfDamage () {
        this((byte) 0);
    }

    /**
     * Creating a new potion with the defined values and a default PotionUI {@link player.item.potion.PotionUI#PotionUI()}
     *
     * @param level the level of the item (must be: <code>level >= 0 && level <= 2</code>)
     */
    public PotionOfDamage (byte level) {
        super(level, "Potion of Damage");
        potionUI.createComponent(images[level]);
    }

    /**
     *
     * @param level the level of a potion: 0, 1 or 2
     * @param posX the x-position in px for <code>PotionUI</code>
     * @param posY and the y-position
     *
     * @see player.item.potion.PotionOfDamage#PotionOfDamage(byte)
     */
    public PotionOfDamage (byte level, int posX, int posY) {
        super(level, "Potion of Damage");
        potionUI.createComponent(images[level], posX, posY);
    }

    /**
     * trigger the effects from the Potion and finally <b>removes the Potions with {@link player.item.potion.Potion#remove()}</b>.
     *
     * @return <code>true</code> - if the potion could be removed (return type of <code>potion.remove()</code>
     */
    @Override
    public boolean triggerEffect () {
        CommandTeam team = Main.getContext().getTurnSystem().getTeamOfActivePlayer();

        final float extraDamage = (getLevel() + 1) * 0.1f;

        team.setExtraDamage((team.getExtraDamage() - 1) + extraDamage);

        // the next time the same player (activePlayer) gets the turn, the extra damage should be reset
        Main.getContext().getActivePlayer().onTurnGet().registerOnce(new AbstractFunction0<BoxedUnit>() {
            @Override
            public BoxedUnit apply () {
                team.setExtraDamage((team.getExtraDamage() - 1) - extraDamage);
                return BoxedUnit.UNIT;
            }
        });

        return remove();
    }
}