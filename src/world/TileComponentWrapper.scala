package world

import java.awt.event.{MouseEvent, MouseAdapter}
import java.awt.{Color, Graphics2D}

import comp.{ComponentWrapper, Component}
import gui.{AdjustableDrawing, GameScreen, Drawable}

import scala.collection.JavaConversions

class VisualMap(val tiles: List[TileComponentWrapper]) extends Drawable {

  private val _vp = new WorldViewport

  /** Constructs a visual map from a Java list. Interop method. */
  def this(t: java.util.List[TileComponentWrapper]) = this(JavaConversions.asScalaBuffer(t).toList)

  /** Returns the current shifting of the map in the x direction. */
  def getShiftY: Float = _vp.getShiftY
  /** Returns the current shifting of the map in the y direction. */
  def getShiftX: Float = _vp.getShiftX


  /** Moves the visual.
    *
    * @param shiftX The amount of x units to move the map.
    * @param shiftY The amount of y units to move the map.
    */
  def moveMap(shiftX: Float, shiftY: Float): Unit = {
    _vp.setShiftX(getShiftX + shiftX - getShiftX)
    _vp.setShiftY(getShiftY + shiftY - getShiftY)
    tiles foreach { c => c.component.setX(c.component.getX + shiftX.asInstanceOf[Int]) }
    tiles foreach { c => c.component.setY(c.component.getY + shiftY.asInstanceOf[Int]) }
  }

  def javaTiles = JavaConversions.seqAsJavaList(tiles)


  /** Draws the whole map. */
  override def draw(g: Graphics2D): Unit = tiles foreach { c => c.component.draw(g) }
}

/** The tile trait should not know that it is surrounded by a component wrapper class.
  * This wrapper class is taking care of the visuals.
  *
  * @param tile The tile to keep.
  */
class TileComponentWrapper(val tile: TileLike) extends ComponentWrapper(tile) {

  override val component = new Component with AdjustableDrawing {

    // Initialization has to be done...
    setBounds(tile.bounds)
    setBackingScreen(GameScreen.getInstance())
    handle({ g => g.setColor(Color.GRAY); g.fillPolygon(getBounds) }, { isMouseFocused })

    addMouseListener(new MouseAdapter {
      override def mouseReleased(e: MouseEvent): Unit = {
        GameScreen.getInstance().getActivePlayer.moveTowards(tile.latticeX, tile.latticeY)
      }
    })

    private lazy val f = tile.drawFunction

    override def draw(g: Graphics2D) = {
      f(g)
      drawAll(g)
    }
  }

}

object TileComponentWrapper {
  /** Converts a [[TileLike]] object to a [[TileComponentWrapper]] object. */
  implicit def tile2ComponentTile(t: TileLike) = new TileComponentWrapper(t)
}