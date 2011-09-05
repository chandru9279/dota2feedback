package zasz.me

import util.Random

object DisplayStrategy
{

  import TagDisplayStrategy._

  def HorizontalFormat: StringFormat = new StringFormat()

  def VerticalFormat: StringFormat = new StringFormat()

  VerticalFormat.FormatFlags = StringFormatFlags.DirectionVertical

  protected def Seed: Random = new Random(DateTime.Now.Second)

  private def _Set = Map(
    EqualHorizontalAndVertical -> new EqualHorizontalAndVertical(),
    AllHorizontal -> new AllHorizontal(),
    AllVertical -> new AllVertical(),
    RandomHorizontalOrVertical -> new RandomHorizontalOrVertical(),
    MoreHorizontalThanVertical -> new RandomHorizontalOrVertical(0.25),
    MoreVerticalThanHorizontal -> new RandomHorizontalOrVertical(0.75)
  )

  def Get(DisplayStrategy: TagDisplayStrategy): DisplayStrategy = _Set.get(DisplayStrategy)
}

abstract class DisplayStrategy
{
  abstract def GetFormat(): StringFormat
}

class AllHorizontal extends DisplayStrategy
{
  override def GetFormat(): StringFormat = HorizontalFormat
}

class AllVertical extends DisplayStrategy
{
  override def GetFormat(): StringFormat = VerticalFormat
}

class RandomHorizontalOrVertical(private var Split: Double = 0.5) extends DisplayStrategy
{
  override def GetFormat(): StringFormat = if (Seed.NextDouble() > _Split) HorizontalFormat else VerticalFormat
}

class EqualHorizontalAndVertical(private var _CurrentState: Boolean = Seed.NextDouble() > 0.5) extends DisplayStrategy
{
  override def GetFormat(): StringFormat =
  {
    _CurrentState = !_CurrentState
    if (_CurrentState) HorizontalFormat else VerticalFormat
  }
}


object TagDisplayStrategy extends Enumeration
{
  type TagDisplayStrategy = Value
  val EqualHorizontalAndVertical = Value("EqualHorizontalAndVertical")
  val AllHorizontal = Value("AllHorizontal")
  val AllVertical = Value("AllVertical")
  val RandomHorizontalOrVertical = Value("RandomHorizontalOrVertical")
  val MoreHorizontalThanVertical = Value("MoreHorizontalThanVertical")
  val MoreVerticalThanHorizontal = Value("MoreVerticalThanHorizontal")
}