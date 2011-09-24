package zasz.me.disorganizerfonts

import java.awt._
import collection.immutable.ListMap

object DisorganizerFonts extends Enumeration
{
  type DisorganizerFonts = Value

  val loadedFonts: Map[String, Font] = new ListMap[String, Font]()
  def loadAllFonts() = {

  }
  def getFont(fontname: DisorganizerFonts): Font =
  {
    Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream(fontname.toString + ".ttf"))
  }

  final val AlphaFridgeMagnets = Value("AlphaFridgeMagnets")
  final val AlphaFridgeMagnetsAllCaps = Value("AlphaFridgeMagnetsAllCaps")
  final val Gnuolane = Value("Gnuolane")
  final val KenyanCoffee = Value("KenyanCoffee")
  final val Sexsmith = Value("Sexsmith")
  final val Steelfish = Value("Steelfish")
  final val VenusRising = Value("VenusRising")
}