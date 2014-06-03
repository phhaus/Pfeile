package world

import comp.GUIUpdater
import gui.Drawable
import misc.metadata.OverrideMetadatable
import java.awt.{Point, Graphics2D}
import scala.collection.mutable
import java.awt.image.BufferedImage
import world.brush._
import java.util

/**
 *
 * @author Josip Palavra
 * @version 31.05.2014
 */
class BaseTerrain(sizeX: Int, sizeY: Int, world: IWorld) extends ITerrain with Drawable with GUIUpdater with OverrideMetadatable {

  private val _grid = new mutable.Queue[mutable.Queue[GridElement]]
  private val _world = world
  initializeGrid()

  private[this] def initializeGrid() {
    // build the queue up
    for(x <- 0 until sizeX) {
      val build = new mutable.Queue[GridElement]
      for(y <- 0 until sizeY) {
        val gridElem = new GridElement(x, y)
        gridElem._world = _world.asInstanceOf[ScaleWorld]
        build enqueue gridElem
      }
      _grid enqueue build
    }
  }

  override def getTileAt(x: Int, y: Int): IBaseTile = _grid.apply(x).apply(y).tile
  override def getSizeY: Int = _grid.apply(0).size
  override def getSizeX: Int = _grid.size
  override def getFieldAt(x: Int, y: Int): IField = getTileAt(x, y).getField
  override def getWorld = _world

  protected[world] def grid = _grid

  override def draw(g: Graphics2D): Unit = {
    // draw every tile
    _grid foreach(i => i foreach(elem => elem.tile.draw(g)))
  }

  override def updateGUI(): Unit = {
    _grid foreach(i => i foreach(elem => elem.tile.updateGUI()))
  }

  def relink() {
    for(x <- 0 until sizeX) {
      for(y <- 0 until sizeY) {
        val g = _grid.apply(x).apply(y)
        g.link(g.north)
        g.link(g.northeast)
        g.link(g.east)
        g.link(g.southeast)
        g.link(g.south)
        g.link(g.southwest)
        g.link(g.west)
        g.link(g.northwest)
      }
    }
  }

  private def recomputeHeights() {
    for(x <- 0 until sizeX) {
      for(y <- 0 until sizeY) {
        val g = _grid.apply(x).apply(y)
        g.recomputeCornerPosition()
      }
    }
    relink()
  }

  def adjustHeights() {

    recomputeHeights()

    var gridelem: GridElement = null
    val isMax = true

    def westMinHeight(): Int = {
      var n = (gridelem.northwest, 0)
      if(n._1 ne null) {
        n = (n._1, n._1.tile.getTileHeight)
      }
      var w = (gridelem.west, 0)
      if(w._1 ne null) {
        w = (w._1, w._1.tile.getTileHeight)
      }
      var s = (gridelem.southwest, 0)
      if(s._1 ne null) {
        s = (s._1, s._1.tile.getTileHeight)
      }
      val that = (gridelem, gridelem.tile.getTileHeight)

      if(isMax) Math.max(Math.max(n._2, w._2), Math.max(s._2, that._2))
      else Math.min(Math.min(n._2, w._2), Math.min(s._2, that._2))
    }

    def southMinHeight(): Int = {
      var n = (gridelem.southwest, 0)
      if(n._1 ne null) {
        n = (n._1, n._1.tile.getTileHeight)
      }
      var w = (gridelem.south, 0)
      if(w._1 ne null) {
        w = (w._1, w._1.tile.getTileHeight)
      }
      var s = (gridelem.southeast, 0)
      if(s._1 ne null) {
        s = (s._1, s._1.tile.getTileHeight)
      }
      val that = (gridelem, gridelem.tile.getTileHeight)

      if(isMax) Math.max(Math.max(n._2, w._2), Math.max(s._2, that._2))
      else Math.min(Math.min(n._2, w._2), Math.min(s._2, that._2))
    }

    def eastMinHeight(): Int = {
      var n = (gridelem.northeast, 0)
      if(n._1 ne null) {
        n = (n._1, n._1.tile.getTileHeight)
      }
      var w = (gridelem.east, 0)
      if(w._1 ne null) {
        w = (w._1, w._1.tile.getTileHeight)
      }
      var s = (gridelem.southeast, 0)
      if(s._1 ne null) {
        s = (s._1, s._1.tile.getTileHeight)
      }
      val that = (gridelem, gridelem.tile.getTileHeight)

      if(isMax) Math.max(Math.max(n._2, w._2), Math.max(s._2, that._2))
      else Math.min(Math.min(n._2, w._2), Math.min(s._2, that._2))
    }

    def northMinHeight(): Int = {
      var n = (gridelem.northwest, 0)
      if(n._1 ne null) {
        n = (n._1, n._1.tile.getTileHeight)
      }
      var w = (gridelem.north, 0)
      if(w._1 ne null) {
        w = (w._1, w._1.tile.getTileHeight)
      }
      var s = (gridelem.northeast, 0)
      if(s._1 ne null) {
        s = (s._1, s._1.tile.getTileHeight)
      }
      val that = (gridelem, gridelem.tile.getTileHeight)

      if(isMax) Math.max(Math.max(n._2, w._2), Math.max(s._2, that._2))
      else Math.min(Math.min(n._2, w._2), Math.min(s._2, that._2))
    }

    for(x <- 0 until sizeX) {
      for(y <- 0 until sizeY) {
        gridelem = _grid.apply(x).apply(y)
        val n = northMinHeight()
        val w = westMinHeight()
        val s = southMinHeight()
        val e = eastMinHeight()

        gridelem.northCorner.getRefY.value -= n
        gridelem.westCorner.getRefY.value -= w
        gridelem.southCorner.getRefY.value -= s
        gridelem.eastCorner.getRefY.value -= e
      }
    }

    updateGUI()
  }
}

class EditableBaseTerrain(x: Int, y: Int, world: IWorld) extends BaseTerrain(x, y, world) with IEditableTerrain {

  private val heightMap = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB)
  private val colorMap = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB)

  override def edit(brush: IRawBrush, points: util.List[Point]): Unit = {
    brush match {
      case cbrush: ColorBrush =>
        val g = colorMap createGraphics()
        val it = points.iterator()
        while(it.hasNext) {
          val p = it.next()
          g setColor cbrush.getColor
          g fillOval(p.x - brush.getThickness, p.y - brush.getThickness, brush.getThickness * 2, brush.getThickness * 2)
        }
      case hbrush: HeightBrush => println("Height brush not implemented yet.")
      case _ =>
    }

    def collectSelectedTiles(points: util.Collection[Point]): util.LinkedList[IBaseTile] =
    {
      val selections = new util.LinkedList[IBaseTile]
      val it = points.iterator()
      while(it.hasNext) {
        val value = it.next()
        selections.add(getWorld.getTileAt(value.x, value.y))
      }
      selections
    }

    if(brush.isInstanceOf[TileTypeBrush]) brush.assignPoints(points)
    else brush.asInstanceOf[IBrush].assign(collectSelectedTiles(points))

  }

  override def set(x: Int, y: Int, tile: IBaseTile): Unit = {
    val terr = grid
    terr.apply(x).apply(y).tile = tile.asInstanceOf[BaseTile]
  }

  override def getHeightMap: BufferedImage = heightMap
  override def getColorMap: BufferedImage = colorMap
}
