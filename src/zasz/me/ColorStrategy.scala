package zasz.me


import enums.Style._
import enums.Theme._
import java.awt._
import util.{Random=>Rand}
import java.util.Date

object ColorStrategy
{
  var _Set = Map[Style, (Color, HslColor, Theme) => ColorStrategy]()
  _Set += Fixed -> ((BgHsl, FgHsl, TheTheme) => new FixedForeground(BgHsl, FgHsl))
  _Set += Varied -> ((BgHsl, FgHsl, TheTheme) => new VariedForeground(BgHsl, FgHsl, TheTheme))
  _Set += RandomVaried -> ((BgHsl, FgHsl, TheTheme) => new RandomVaried(BgHsl, FgHsl, TheTheme))
  _Set += Random -> ((BgHsl, FgHsl, TheTheme) => new RandomForeground(BgHsl, FgHsl))
  _Set += Grayscale -> ((BgHsl, FgHsl, TheTheme) => new Grayscale(BgHsl, FgHsl, TheTheme))

  def Get(TheTheme: Theme, TheStyle: Style, Background: Color, Foreground: Color): ColorStrategy =
  {
    import Implicits._
    val FgHsl: HslColor = if (TheTheme == LightBgDarkFg) Foreground.Darken else Foreground.Lighten
    if (Background.getAlpha != 255) /* Not interfering with any transparent color */
      return _Set(TheStyle).apply(Background, FgHsl, TheTheme)
    val BgHsl: HslColor = if (TheTheme == LightBgDarkFg) Background.Lighten else Background.Darken
    _Set(TheStyle).apply(BgHsl.getRGB, FgHsl, TheTheme)
  }
}

abstract class ColorStrategy protected(protected var Background: Color, protected var Foreground: HslColor)
{
  protected val _Seed = new Rand(new Date().getTime)

  def GetCurrentColor(): Color
}

class FixedForeground(Background: Color, Foreground: HslColor) extends ColorStrategy(Background, Foreground)
{
  override def GetCurrentColor(): Color = Foreground.getRGB
}

class RandomForeground(Background: Color, Foreground: HslColor) extends FixedForeground(Background, Foreground)
{
  override def GetCurrentColor(): Color =
  {
    Foreground.adjustHue(_Seed.nextInt(361))
    Foreground.getRGB
  }
}

class VariedForeground(Background: Color, Foreground: HslColor, TheTheme: Theme) extends ColorStrategy(Background, Foreground)
{
  protected var Range: Int
  /* Dark foreground needed, so Luminosity is reduced to somewhere between 0 & 0.5
  * Saturation is full so the color comes out, removing all blackness/greyness
  * For Light foreground Luminosity is kept between 0.5 & 1
  */
  Foreground.adjustSaturation(100)
  Range = if (TheTheme == LightBgDarkFg) 0 else 50

  override def GetCurrentColor(): Color =
  {
    Foreground.adjustLuminance(_Seed.nextInt(50) *  + 1 + Range)
    Foreground.getRGB
  }
}

class RandomVaried(Background: Color, Foreground: HslColor, TheTheme: Theme) extends VariedForeground(Background, Foreground, TheTheme)
{
  override def GetCurrentColor(): Color =
  {
    Foreground.adjustHue(_Seed.nextInt(100) + 1)
    super.GetCurrentColor();
  }
}

class Grayscale(var Background: Color, Foreground: HslColor, TheTheme: Theme) extends VariedForeground(Background, Foreground, TheTheme)
{
  /* Saturation is 0 - Meaning no color specified by hue can be seen at all.
  * So luminance is now reduced to showing grayscale */
  Foreground.adjustSaturation(0)
  val Temp = if (TheTheme == LightBgDarkFg) Color.WHITE else Color.BLACK
  Background = new Color(Temp.getRed, Temp.getGreen,  Temp.getBlue, Background.getAlpha)
}




object Implicits
{
  implicit def ExtendColor(Given: Color): ColorExtensions = new ColorExtensions(Given)
}

class ColorExtensions(Given: Color)
{
  def Lighten(): HslColor =
  {
    val ColorHsl = new HslColor(Given)
    if (ColorHsl.getLuminance < 50) ColorHsl.adjustLuminance(75)
    ColorHsl
  }

  def Darken(): HslColor =
  {
    val ColorHsl = new HslColor(Given)
    if (ColorHsl.getLuminance > 50) ColorHsl.adjustLuminance(25)
    ColorHsl
  }
}
