package world

import world.brush.TileTypeBrush

import scala.collection.mutable
import scala.util.Random

/** Base trait for all terrain types.
  *
  * With a need to support extensibility, we need a base "interface" so that we can support terrains
  * that are implemented differently.
  * @author Josip Palavra
  */
trait TerrainLike {

  /** The type of tile that the terrain is managing. */
  type TileType <: TileLike

  /** The world to which the terrain is linked. */
  val world: WorldLike

  /** The width dimension of the terrain. */
  def width: Int
  /** The height dimension of the terrain. */
  def height: Int

  require(width > 0, "Width may not be negative or 0.")
  require(height > 0, "Height may not be negative or 0.")

  /** Returns true if the specified coordinate is valid.
    *
    * @param x The x coordinate.
    * @param y The y coordinate.
    * @return is the point at the given point on the map (<code>x >= 0 && y >= 0 && x < width && y < height</code>)
    */
  def isTileValid(x: Int, y: Int) = x >= 0 && x < width && y >= 0 && y < height

  /** Returns the tile at the given coordinate.
    *
    * It is implementation-specific how terrains are coping with coordinates that are out of bounds.
    * Some may throw an exception, others just return null. Like I said, implementation-specific.
    *
    * @param x The x coordinate.
    * @param y The y coordinate.
    * @return The tile at the specified coordinate.
    */
  def tileAt(x: Int, y: Int): TileType

  /** Sets the tile type at the given position to the specified tile.
    *
    * @param x The x position.
    * @param y The x position.
    * @param t The new tile type to set to.
    */
  def setTileAt(x: Int, y: Int, t: TileType): Unit

  /** Returns the tiles in a one dimensional list. */
  def tiles: List[TileType] = {
    val ret = mutable.MutableList[TileType]()
    for(y <- 0 until height) {
      for(x <- 0 until width) {
        ret += tileAt(x, y)
      }
    }
    ret.toList
  }

  def generate(r: Random = new Random)(seed: Long = r.nextLong( )): Unit

}

/** The default terrain (for now).
  *
  * The terrain manages isometric tiles and has no references to the [[comp.Component]] class
  * or whatsoever GUI class we have.
  *
  */
class DefaultTerrain(override val world: DefaultWorld) extends TerrainLike {

  override type TileType = IsometricPolygonTile

  override lazy val width  = 25
  override lazy val height = 25

  // Wow. The generation process is so straightforward.
  private val _primitiveRandom = new Random
  // The tiles are managed in a (multidimensional) array, NOT in a scala collection class.
  private val _tiles = Array.tabulate(width, height) { (x, y) =>
    _primitiveRandom.nextInt(2) match {
      case 0 => new GrassTile(x, y, DefaultTerrain.this)
      case 1 => new SeaTile(x, y, DefaultTerrain.this)
      case _ => null
    }
  }

  @throws[ArrayIndexOutOfBoundsException]("Tile is not specified.")
  override def tileAt(x: Int, y: Int) = {
    require(isTileValid(x, y))
    _tiles(x)(y)
  }

  override def setTileAt(x: Int, y: Int, t: TileType): Unit = {
    require(t ne null)
    _tiles(x)(y) = t
  }

  override def generate(r: Random)(seed: Long): Unit = {

    def tileTypeStage(): Unit = {

      case class TTPoint(x: Int, y: Int, tileType: Class[_ <: IsometricPolygonTile])

      val points = mutable.MutableList[TTPoint]()

      for(i <- 0 until ((width * height) / 2)) {
        points += TTPoint(r.nextInt(width), r.nextInt(height), r.nextInt(2) match {
          case 0 => classOf[SeaTile]
          case 1 => classOf[GrassTile]
        })
      }

      val brush = new TileTypeBrush

      points foreach { p =>
        brush.tileType = p.tileType
        brush.applyBrush(this, p.x, p.y)
      }

    }

    r.setSeed(seed)

    tileTypeStage()

  }
}

object DefaultTerrain
