package player.item;

import general.LogFacility;
import general.Main;
import newent.Bot;
import newent.Entity;
import newent.InventoryEntity;
import newent.Player;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Loots dropped by dead enemies or creeps are BagOfLoots, no {@link player.item.Treasure}.
 */
public class BagOfLoots extends Loot {

    /** the texture of a BagOfLoots */
    private static BufferedImage image;

    static {
        String path = "resources/gfx/item textures/bagOfLoots.png";
        try {
            image = ImageIO.read(BagOfLoots.class.getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            e.printStackTrace();
            LogFacility.log("The BufferedImage of class BagOfLoots couldn't be loaded! Path: " + path,
                    LogFacility.LoggingLevel.Error);
        }
    }

    /** Creating a new BagOfLoots from a deadEntity. All values are taken from the deadEntity.
     * @param deadEntity the entity, which dropped a BagOfLoots (--> usually a dead Entity)
     * @see player.item.Loot#Loot(int, int, LootUI, String)
     * @see player.item.BagOfLoots#BagOfLoots(int, int) */
    public BagOfLoots (Entity deadEntity) {
        super(deadEntity.getGridX(), deadEntity.getGridY(), "BagOfLoots [from " + deadEntity.name() + "]");

    }

    /**
     * Creates a new BagOfLoots on the position(<code>gridX</code>|<code>gridY</code>).
     *
     * @param gridX the x-position of the tile, where the <code>BagOfLoots</code> should be placed
     * @param gridY and the y-position
     * @see player.item.Loot#Loot(int, int, String)
     * @see player.item.BagOfLoots#BagOfLoots(newent.Entity)
     */
    public BagOfLoots (int gridX, int gridY) {
        super(gridX, gridY, "BagOfLoots");
    }

    @Override
    public boolean collect (Player activePlayer) {
        // there is no difference between any entity and a player.
        return collect((InventoryEntity) activePlayer);
    }

    @Override
    public boolean collect (Bot activeBot) {
        // there is no difference between any entity and a player.
        return collect((InventoryEntity) activeBot);
    }

    @Override
    public boolean collect (InventoryEntity entity) {
        // controlling if the inventory is full, is already done by "put(this)".
        if (entity.inventory().put(this)) {
            // only remove "this" from the WorldLootList, if it has been added to inventory successfully.
            Main.getContext().getWorldLootList().remove(this);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public BufferedImage getImage () {
        return image;
    }
}
