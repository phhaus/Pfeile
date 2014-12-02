package newent

import java.awt.{Color, Graphics2D, Point}

import comp.Component
import general.Delegate
import general.Main.getGameWindow
import newent.pathfinding.AStarPathfinder
import player.Life
import world.WorldLike

/** Represents a player in the world.
  *
  * @param world The world of the entity. Should not be null.
  * @param spawnpoint The spawnpoint of the player.
  * @param name The name. If null, a name based on the hash code will be generated.
  */
class Player(world: WorldLike,
             spawnpoint: Point,
             name: String) extends Entity(world, spawnpoint, name) with MoveableEntity with TeleportableEntity with
                                   InventoryEntity with LivingEntity with VisionEntity with Combatant {

  // Game section.

  private var _localVisionPoint = visionMap.grantVision(getGridX, getGridY, 5)

  private def updateLocalVisionPoint(): Unit = {
    if(_localVisionPoint ne null) {
      _localVisionPoint.releaseVision()
    }
    _localVisionPoint = visionMap.grantVision(getGridX, getGridY, 5)
  }

  override protected def setGridPosition(x: Int, y: Int): Unit = {
    super.setGridPosition( x, y )
    updateLocalVisionPoint()
  }

  /** The default movement points that the entity has. */
  override def defaultMovementPoints = 4
  override val pathfinderLogic       = new AStarPathfinder(20, { t => true })
  override val life                  = new Life(400.0, 2.2, 400.0)

  /** the number of arrows the player an still use from this his/her selected usable <code> Mechanics.arrowNumberFreeSet </code> inventory */
  private var arrowNumberFreeSetUsable = general.Mechanics.arrowNumberFreeSet
   /** the number of arrows the player an still use from this his/her selected usable <code> Mechanics.arrowNumberFreeSet </code> inventory */
  def getArrowNumberFreeSetUsable = arrowNumberFreeSetUsable
   /** the number of arrows the player an still use from this his/her selected usable <code> Mechanics.arrowNumberFreeSet </code> inventory */
  def setArrowNumberFreeSetUsable(arrowNumberUsable: Int) = arrowNumberFreeSetUsable = arrowNumberUsable

  // Delegate registration only valid after initialization of the actual life object.
  life.onDeath += { () =>
     general.Main.getContext.getTimeClock.stop()
     getGameWindow.getScreenManager.getActiveScreen.onLeavingScreen(getGameWindow.getScreenManager.getActiveScreen, gui.GameOverScreen.SCREEN_INDEX)
     //animation.SoundPool.stop_allMelodies()
     //animation.SoundPool.start_gameOverMelodie()
  }

  /** Called when turn is assigned to the player. */
  val onTurnGet = Delegate.createZeroArity

  /** Called when the player ended his turn. <p>
    * In terms of UI the equivalent to the delegate would be pressing the "End turn" button,
    * which essentially tells the program that the player has completed all of his actions now.
    */
  val onMovesCompleted = Delegate.createZeroArity

  /******* TEST CODE FOR ATTACK MECHANISM *********
  Main.getContext.onTurnEnd += { () =>
    world.terrain.tileAt(5, 5).take(AttackEvent(
    inventory.remove({ _.isInstanceOf[AbstractArrow] }).get.asInstanceOf[Weapon],
    tileLocation, world.terrain.tileAt(5, 5), this, 1.5))
    println("An arrow has been shot.")
  }

  world.terrain.tileAt(5, 5).onImpact += { e =>
    println(s"${e.weapon} has arrived at its destination!")
  }
  */

  /** The component that the representable object uses first. Method is called only once.
    *
    * The start component must not be null at first, else it will throw a [[IllegalArgumentException]].
    * @return A component object which the representable object uses first.
    */
  override protected def startComponent = new Component {

    private val drawColor = new Color(255, 0, 0)

    setBounds(tileLocation.component.getBounds)

    onLocationChanged += { e => setBounds(e.end.component.getBounds) }

    override def draw(g: Graphics2D): Unit = {
      g.setColor(drawColor)
      g.fill(getBounds)
    }
  }

  /** The initial attribute object with which the entity begins recording its attributes.
    *
    * The method just gets called once to initialize an underlying field.
    */
  override protected def initialAttribute = new Attributes {

    override protected def initialCurrent(initObject: Current) = initObject

    override protected def initialLasting(initObject: Lasting) = initObject
  }
}
