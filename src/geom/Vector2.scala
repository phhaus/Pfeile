package geom

import java.awt.geom.Point2D
import java.awt.{Dimension, Point => APoint}

import scala.math._

/**
 * A two-dimensional, floating point vector.
 * @param x The x component.
 * @param y The y component.
 */
@deprecated("", "2016-01-29")
case class Vector2(x: Float, y: Float) extends FloatVector {

  override type VecType = Vector2

  def this(p: APoint) = this(p.x, p.y)

  def dot(vec: Vector2): Float = x * vec.x + y * vec.y

  override def unifiedVector(factor: Float) = Vector2(factor, factor)

  override def vectorFrom(x: List[Float]) = {
    require(x.size == dimension)
    Vector2(x.head, x(1))
  }

  def lerp(dest: Vector2, lerpFactor: Float): Vector2 = {
    ((dest - this) * lerpFactor) + this
  }

  def rotate(angle: Float): Vector2 = {
    val rad = toRadians( angle )
    val cos = math.cos( rad )
    val sin = math.sin( rad )
    Vector2( (x * cos - y * sin).asInstanceOf[Float], (x * sin + y * cos).asInstanceOf[Float] )
  }

  def round = new Point(math.round(x), math.round(y))
  def toPoint = new Point(x.asInstanceOf[Int], y.asInstanceOf[Int])
  def toDimension = new Dimension(x.asInstanceOf[Int], y.asInstanceOf[Int])

  override def asList = List(x, y)

  def div(vec: Vector2) = /(vec)
  def div(f: Float) = /(f)
  def add(vec: Vector2) = this.+(vec)
  def add(f: Float) = this.+(f)
  def sub(vec: Vector2) = this.-(vec)
  def sub(f: Float) = this.-(f)
  def mult(vec: Vector2) = *(vec)
  def mult(f: Float) = *(f)

  def negated = Vector2(-x, -y)
}

@deprecated("", "2016-01-29")
object Vector2 {
  def apply(point: Point2D): Vector2 = Vector2(point.getX.asInstanceOf[Float], point.getY.asInstanceOf[Float])
  def apply(point: APoint): Vector2 = Vector2(point.x, point.y)

  val zero = Vector2(0, 0)
}
