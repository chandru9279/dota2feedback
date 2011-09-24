package zasz.me

import enums.TagDisplayStrategy
import enums.TagDisplayStrategy._
import enums.StringFormat._
import util.Random


object DisplayStrategy
{
  private def _Set: Map[TagDisplayStrategy, DisplayStrategy] = Map(
    TagDisplayStrategy.EqualHorizontalAndVertical -> new EqualHorizontalAndVertical(),
    TagDisplayStrategy.AllHorizontal -> new AllHorizontal(),
    TagDisplayStrategy.AllVertical -> new AllVertical(),
    TagDisplayStrategy.RandomHorizontalOrVertical -> new RandomHorizontalOrVertical(),
    TagDisplayStrategy.MoreHorizontalThanVertical -> new RandomHorizontalOrVertical(0.20),
    TagDisplayStrategy.MoreVerticalThanHorizontal -> new RandomHorizontalOrVertical(0.80)
  )

  def Get(DisplayStrategy: TagDisplayStrategy): DisplayStrategy = _Set(DisplayStrategy)
}

abstract class DisplayStrategy
{
  protected def Seed: Random = new Random()

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


