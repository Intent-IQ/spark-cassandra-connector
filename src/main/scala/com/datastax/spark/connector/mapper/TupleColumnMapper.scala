package com.datastax.spark.connector.mapper

import com.datastax.spark.connector.cql.TableDef
import com.datastax.spark.connector.rdd.reader.{AnyObjectFactory, ObjectFactory}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class TupleColumnMapper[T <: Product : ClassTag] extends ColumnMapper[T] {

  private def indexedColumnRefs(n: Int) =
    (0 until n).map(IndexedColumnRef)

  override def columnMap(tableDef: TableDef): ColumnMap = {

    val GetterRegex = "_([0-9]+)".r
    val cls = implicitly[ClassTag[T]].runtimeClass

    val constructor =
      indexedColumnRefs(cls.getConstructors()(0).getParameterTypes.length)

    val getters = {
      for (name@GetterRegex(id) <- cls.getMethods.map(_.getName))
      yield (name, IndexedColumnRef(id.toInt - 1))
    }.toMap

    val setters =
      Map.empty[String, ColumnRef]

    SimpleColumnMap(constructor, getters, setters)
  }

  override def objectFactory[R <: T : TypeTag]: ObjectFactory[R] = new AnyObjectFactory[R]()
}
