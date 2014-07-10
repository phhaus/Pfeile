package gui

import java.awt.Graphics2D
import java.awt.event.KeyEvent

import world.{BaseTerrain, BaseTile, IWorld}

import scala.beans.BeanProperty
import general.Main
/**
 *
 * @author Josip
 */
object NewWorldTestScreen extends Screen("New world test", 164) {

  @BeanProperty
  var world: IWorld = null
  
  var shootButtonPressed = false

  def getScreenIndex () : Int = {
    return 164
  }
  
  override def keyPressed(e: KeyEvent) {
    if(e.getKeyCode == KeyEvent.VK_RIGHT) world.getViewport.shiftRel(-7, 0)
    if(e.getKeyCode == KeyEvent.VK_LEFT) world.getViewport.shiftRel(7, 0)
    if(e.getKeyCode == KeyEvent.VK_DOWN) world.getViewport.shiftRel(0, -7)
    if(e.getKeyCode == KeyEvent.VK_UP) world.getViewport.shiftRel(0, 7)
    if(e.getKeyCode == KeyEvent.VK_PAGE_UP) world.getViewport.zoomRel(1.075f)
    if(e.getKeyCode == KeyEvent.VK_PAGE_DOWN) world.getViewport.zoomRel(0.925f)
    //if(e.getKeyCode == KeyEvent.VK_M) world.getViewport.setRotation((0.1 * Main.delta()).asInstanceOf[Int] + world.getViewport.getRotation)
    //if(e.getKeyCode == KeyEvent.VK_N) world.getViewport.setRotation((-0.1 * Main.delta()).asInstanceOf[Int] + world.getViewport.getRotation)
    if(e.getKeyCode == KeyEvent.VK_B) world.getViewport.setPovAngle((0.5 * Main.delta()).asInstanceOf[Int] + world.getViewport.getPovAngle)
    if(e.getKeyCode == KeyEvent.VK_V) world.getViewport.setPovAngle((-0.5 * Main.delta()).asInstanceOf[Int] + world.getViewport.getPovAngle)
    for(i <- 0 until getComponents.size()) {
      getComponents.get(i).updateGUI()
    }
  }

  def bindTileComponents(): Unit = {
    for(x <- 0 until world.getSizeX) {
      for(y <- 0 until world.getSizeY) {
        add(world.getTileAt(x, y).asInstanceOf[BaseTile])
      }
    }
  }

  override def draw(g: Graphics2D): Unit = {
    super.draw(g)
    //world.draw(g)
    world.getTerrain.asInstanceOf[BaseTerrain].drawInfoBox(g)
  }
}
