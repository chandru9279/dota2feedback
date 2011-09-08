package zasz.me

import enums.TagDisplayStrategy._
import enums.StringFormat._
import util.Random
import java.util.Date


object DisplayStrategy
{
  private def _Set = Map(
    EqualHorizontalAndVertical -> new EqualHorizontalAndVertical(),
    AllHorizontal -> new AllHorizontal(),
    AllVertical -> new AllVertical(),
    RandomHorizontalOrVertical -> new RandomHorizontalOrVertical(),
    MoreHorizontalThanVertical -> new RandomHorizontalOrVertical(0.25),
    MoreVerticalThanHorizontal -> new RandomHorizontalOrVertical(0.75)
  )

  def Get(DisplayStrategy: TagDisplayStrategy): DisplayStrategy = _Set(DisplayStrategy)
}

abstract class DisplayStrategy
{
  protected def Seed: Random = new Random(new Date().getTime)

  def GetFormat(): StringFormat
}

class AllHorizontal extends DisplayStrategy
{
  override def GetFormat(): StringFormat = Horizontal
}

class AllVertical extends DisplayStrategy
{
  override def GetFormat(): StringFormat = Vertical
}

class RandomHorizontalOrVertical(private var Split: Double = 0.5) extends DisplayStrategy
{
  override def GetFormat(): StringFormat = if (Seed.nextDouble() > Split) Horizontal else Vertical
}

class EqualHorizontalAndVertical() extends DisplayStrategy
{
  private var _CurrentState: Boolean = Seed.nextBoolean()
  override def GetFormat(): StringFormat =
  {
    _CurrentState = !_CurrentState
    if (_CurrentState) Horizontal else Vertical
  }
}


