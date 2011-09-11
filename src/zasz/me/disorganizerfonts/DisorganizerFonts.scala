package zasz.me.disorganizerfonts

import java.awt._
import javax.servlet.ServletContext

object DisorganizerFonts extends Enumeration
{
  type DisorganizerFonts = Value

  def getFont(fontname: DisorganizerFonts, servletContext: ServletContext): Font =
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