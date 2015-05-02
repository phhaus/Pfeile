package player.item;

import general.JavaInterop;
import general.PfeileContext;
import gui.Drawable;
import newent.*;
import scala.collection.Seq;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The <code>WorldLootList</code> is saved in PfeileContext. This class provides the information need to store all loots,
 * that are placed in world. Calling the draw method here allows the user to draw every loot saved in the List by drawing
 * it's LootUI.
 */
public class WorldLootList implements Drawable {
    private final List<Loot> lootList;

    /** The list is used to save every not {@link newent.VisionStatus#Hidden} Loot in order to speed up the drawing process */
    private final List<Loot> lootVisibleList;

    /** this spawn the loot automatically. It registers the spawn calls to listener of turnSystem */
    private final LootSpawner lootSpawner;

    /**
     * The context on which this world loot list is operating on.
     * For decoupling reasons. Previous implementations relied too much on initialization of global variables.
     */
    private final PfeileContext context;

    /** creating a new WorldLootList with the default size 18 [as java.util.ArrayList].
     * It also creates a new List for every visible Loot (from the view of the activePlayer) and registers
     * the {@link WorldLootList#updateVisibleLoot()} to {@link newent.Entity#onLocationChanged()} and
     * {@link general.TurnSystem#onTurnGet()}.
     * */
    public WorldLootList (PfeileContext context) {
        this.context = context;
        lootList = new ArrayList<>(20);
        lootVisibleList = new ArrayList<>(12);
        lootSpawner = new LootSpawner(context);

        context.turnSystem().onTurnGet().registerJava(team -> {
            updateVisibleLoot();
        });

        // every time, when the location of a player has changed, the list of every not-hidden loot must update itself.
        // ==> {@link WorldLootList#updateVisibleLoot()} is registered to the "onLocationChanged"-Delegate of every Player.
        final Seq<Team> teamSeq = context.getTurnSystem().teams().apply();
        teamSeq.foreach(JavaInterop.asScala(team -> {
            Player player = ((CommandTeam) team).getHead();
            player.onLocationChanged().registerJava(locationChangedEvent -> {
                updateVisibleLoot();
            });
        }));
    }

    /**
     * Registers a new Loot to the lootList and updates the list of the visibleLoots (to speed the drawing process)
     *
     * @param droppedLoot the loot to be added to the list
     */
    public void add (Loot droppedLoot) {
        synchronized (lootList) {
            lootList.add(droppedLoot);
        }
        updateVisibleLoot();
    }

    /**
     * Removes the specified Loot and if the element could be removed,
     * it updates the list of the visibleLoots {@link WorldLootList#updateVisibleLoot()}.
     *
     * @param collectedLoot the loot to be removed
     * @return <code>true</code> - if the list contained the <code>collectedLoot</code>
     */
    public boolean remove (Loot collectedLoot) {
        boolean removed;

        synchronized (lootList) {
            removed = lootList.remove(collectedLoot);
        }

        if (removed)
            updateVisibleLoot();

        return removed;
    }

    /**
     * Returns the ArrayList of all Loots. Do not change the list; just use it if you want to find loots or change loots
     *
     * @return the list
     * @see player.item.WorldLootList#getVisibleLoots()
     */
    public List<Loot> getLoots () {
        return lootList;
    }

    /**
     * Returns the ArrayList of Loots, which are visible ({@link newent.VisionStatus#Revealed} or {@link newent.VisionStatus#Visible})
     * by the activePlayer. Don't change the returned lootList itself, only the loots inside.
     * Use, whenever possible the methods of WorldLootList itself.
     * <b>Don't forget to synchronize changes in visibleLoots</b>
     *
     * @return the list of every visible loot (by the {@link general.PfeileContext#getActivePlayer()})
     */
    public List<Loot> getVisibleLoots () {
        return lootVisibleList;
    }

    /**
     * Updates the {@link WorldLootList#getVisibleLoots()}.
     * If the visibleRadius of the activePlayer changes, or when the activePlayer's turn changes, this method has to be triggered
     * by a Delegate.
     * */
    public void updateVisibleLoot () {
        final VisionMap visibleMap = context.getActivePlayer().visionMap();

        synchronized (lootVisibleList) {
            lootVisibleList.clear();

            for(Loot loot : lootList) {
                // only the loot to the visibleList, when the VisionStatus isn't Hidden
                if (visibleMap.visionStatusOf(loot.getGridX(), loot.getGridY()) != VisionStatus.Hidden)
                    lootVisibleList.add(loot);

            }
        }
    }

    /**
     * Draws every Loot, but only in the visible area of the activePlayer.
     * The VisionStatus mustn't be {@link newent.VisionStatus#Hidden}.
     *
     * @param g the graphics object used by every {@link gui.Drawable}.
     */
    @Override
    public void draw (Graphics2D g) {
        for (Loot loot : lootVisibleList) {
            loot.getLootUI().draw(g);
        }
    }

    @Override
    public String toString () {
        return "WorldLootList{ " + lootList.toString() + " }";
    }
}
