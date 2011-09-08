package zasz.me.enums

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