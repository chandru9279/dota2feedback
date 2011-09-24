package zasz.me

import org.junit.Test
import java.util.HashMap
import collection.JavaConverters._

class ScalaTests
{
  @Test def SortDesc()
  {
    val hashMap: HashMap[String, java.lang.Integer] = new DisorganizerServlet().getSample
    val Tags = hashMap.asScala.map(x => (x._1, Int.unbox(x._2)))
    val _TagsSorted = collection.immutable.ListMap[String, Int](Tags.toList.sortBy[Int](-_._2): _*)
    println("head : " + _TagsSorted.head._2)
    println("last : " + _TagsSorted.last._2)
    _TagsSorted.foreach(X => println(X))
    assert(_TagsSorted.head._2 > _TagsSorted.last._2)
  }

  @Test def StackoverflowQuestion()
  {
    val map = Map("A" -> 5, "B" -> 12, "C" -> 2, "D" -> 9, "E" -> 18)
    val sortedIMMUTABLEMap = collection.immutable.ListMap[String, Int](map.toList.sortBy[Int](_._2): _*)
    println("head : " + sortedIMMUTABLEMap.head._2)
    println("last : " + sortedIMMUTABLEMap.last._2)
    sortedIMMUTABLEMap.foreach(X => println(X))
    assert(sortedIMMUTABLEMap.head._2 < sortedIMMUTABLEMap.last._2)

    val sortedMUTABLEMap = collection.mutable.ListMap[String, Int](map.toList.sortBy[Int](_._2): _*)
    println("head : " + sortedMUTABLEMap.head._2)
    println("last : " + sortedMUTABLEMap.last._2)
    sortedMUTABLEMap.foreach(X => println(X))
    assert(sortedMUTABLEMap.head._2 > sortedMUTABLEMap.last._2)
  }

}