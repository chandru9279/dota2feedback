package zasz.me.enums

object Style extends Enumeration
{
  type Style = Value
  val Fixed = Value("Fixed")
  val Random = Value("Random")
  val Varied = Value("Varied")
  val RandomVaried = Value("RandomVaried")
  val Grayscale = Value("Grayscale")
}