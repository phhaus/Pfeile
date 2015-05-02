package general.property

import java.lang

class StaticProperty[A <: AnyRef] extends PropertyBase[A] with StaticAccessors[A]
class LazyStaticProperty[A <: AnyRef] extends StaticProperty[A] with LazyInit[A]

class IntStaticProperty extends StaticProperty[lang.Integer]
class DoubleStaticProperty extends StaticProperty[lang.Double]
class LongStaticProperty extends StaticProperty[lang.Long]

class LazyIntStaticProperty extends LazyStaticProperty[lang.Integer]
class LazyDoubleStaticProperty extends LazyStaticProperty[lang.Double]
class LazyLongStaticProperty extends LazyStaticProperty[lang.Long]

//class LazyStaticProperty[A] extends PropertyLike[A, A] with StaticAccessors[A, A] with LazyInit[A, A]
