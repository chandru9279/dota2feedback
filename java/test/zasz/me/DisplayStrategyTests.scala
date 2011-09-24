package zasz.me

import enums.StringFormat
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import utils.Repeat
import collection.mutable.ListBuffer
import zasz.me.enums.StringFormat._

class DisplayStrategyTests extends AssertionsForJUnit
{

  implicit def int2Rep(i: Int): Repeat = new Repeat(i)

  @Test def checkRandomStrategyIsReallyRandom()
  {
    val strategy = new RandomHorizontalOrVertical()
    val results = new ListBuffer[StringFormat]
    10 times (results += strategy.GetFormat())
    val zontals = results.count(it => it == StringFormat.Horizontal)
    val verticals = results.count(it => it == StringFormat.Vertical)
    println("zontals" + zontals)
    println("verticals" + verticals)
    assert(zontals > 0)
    assert(verticals > 0)
  }

}
