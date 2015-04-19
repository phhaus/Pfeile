package general

/**
  *
  * @author Josip Palavra
  */
case class Property[A] private (private var value: Option[A]) {

  private var _getter: A => A = identity[A]
  private var _setter: A => A = identity[A]

  /**
    * A validator function which can return [[scala.None]] for a good value
    * or a [[scala.Some]] with a message why the value is not accepted.
    */
  private var _validation: A => Option[String] = x => None

  /**
    * Returns the underlying value directly.
    *
    * The underlying value is returned directly, bypassing control structures
    * provided by [[scala.Option]]. As a result, the method can throw [[scala.Option]]-specific
    * exceptions.
    *
    * @return The underlying value that the property holds.
    * @see [[scala.Option]]
    */
  @inline def get = synchronized { option.get }

  /**
    * Returns the option containing the possible value of the property.
    *
    * The important thing about the option is that the property __does not have
    * to have a value in it__, in contrast to [[general.Property# g e t]].
    *
    * @return The option holding the possible property value.
    */
  def option = value map _getter

  def getter = _getter
  def getter_=(x: A => A) = {
    require(x != null)
    _getter = x
  }

  def appendGetter(x: A => A) = getter_=(_getter andThen x)

  def setter = _setter
  def setter_=(x: A => A) = {
    require(x != null)
    _setter = x
  }

  def appendSetter(x: A => A) = setter_=(_setter andThen x)

  def validation = _validation
  def validation_=(x: A => Option[String]): Unit = {
    require(x != null)
    _validation = x
  }

  /**
    * Instructs this property to set itself to the value the other property gets every time the other
    * property is set to another value.
    *
    * So, every time the given property's value is changed through the [[general.Property#set(java.lang.Object)]] method,
    * this property sets its value to the new value of the other property as well.
    * ''This can be a potential source for very annoying bugs, use this with care. You have been warned.''
    *
    * @param another The other property to listen to.
    */
  def complyWith(another: Property[A]): Unit = {
    another appendSetter identityWith { x =>
      this set x
    }
    LogFacility.log(s"Property $this complying now to $another")
  }

  /**
    * Does essentially the same as [[general.Property#complyWith(general.Property)]].
    *
    * But why the overhead and introduce a new operator for it?
    * In future Scala code that I am going to write, I want to see __clearly__ what properties
    * are listening to other ones. Operators/unknown signs draw attention, and that's the reason.
    * ''This can be a potential source for very annoying bugs. You have been warned.
    * Use this only in situations where you are 100% sure this is not going to fail entire algorithms.''
    *
    * @param another The other property to listen to.
    * @see [[general.Property#complyWith(general.Property)]]
    */
  def =<<=(another: Property[A]): Unit = complyWith(another)

  /**
    * Sets the underlying value to a new one.
    *
    * From the [[general.Property]] class there are no restrictions in setting
    * the underlying value, so every value is in theory permitted.
    *
    * Note that the delegates `preSet` and `postSet` can throw exceptions, however.
    *
    * @param x The new value of the property.
    */
  def set(x: A): Unit = synchronized {
    val validationCheck = _validation(x)
    require(validationCheck.isEmpty, s"Property validation failed: ${validationCheck.get}")
    value = Some(_setter(x))
  }

  @inline def filterNot(p: (A) => Boolean) = value.filterNot(p)

  @inline def filter(p: (A) => Boolean) = value.filter(p)

  @inline def toRight[X](left: => X) = value.toRight(left)

  @inline def toLeft[X](right: => X) = value.toLeft(right)

  @inline def nonEmpty = value.nonEmpty

  @inline def flatMap[B](f: (A) => Option[B]) = value.flatMap(f)

  @inline def map[B](f: (A) => B) = value.map(f)

  @inline def isDefined = value.isDefined

  @inline def isEmpty = value.isEmpty

  /**
    * Applies a custom function to the property without mutating it.
    *
    * @param f The function with which to mutate the result of the function.
    * @tparam B The result type of the given function.
    * @return A mutated value.
    */
  def apply[B](f: A => B) = if (f != null) f(get) else get

  def apply() = get

}

object Property {

  def apply[A](x: A) = new Property(Some(x))

  def apply[A]() = new Property[A](None)

  /**
    * Injects code into the set function of the property to check the new value
    * if it exists (if it is not null).
    *
    * @tparam A The type of the underlying value.
    * @return The property with validation check.
    */
  def withValidation[A](): Property[A] = {
    val ret = Property[A]()
    ret appendSetter identityWith { x => require(x != null) }
    ret
  }

  /**
    * Injects code into the set function of the property to check the new value
    * if it exists (if it is not null).
    *
    * @tparam A The type of the underlying value.
    * @param x The initial value to set the property to.
    * @return The property with validation check.
    */
  def withValidation[A](x: A): Property[A] = {
    val ret = withValidation[A]()
    ret set x
    ret
  }

  implicit def toUnderlyingValue[A](property: Property[A]): A = property.get

}
