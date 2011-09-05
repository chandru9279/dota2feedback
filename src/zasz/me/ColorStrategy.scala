package zasz.me

import java.util.HashMap
import java.util.Random
import java.awt._
import Theme._
import Style._

object ColorStrategy
{
  val _Set = new HashMap[Style, (Color, HslColor, Theme) => ColorStrategy]
  _Set += Style.Fixed -> ((BgHsl, FgHsl, TheTheme) => new FixedForeground(BgHsl, FgHsl))
  _Set += Style.Varied -> ((BgHsl, FgHsl, TheTheme) => new VariedForeground(BgHsl, FgHsl, TheTheme))
  _Set += Style.RandomVaried -> ((BgHsl, FgHsl, TheTheme) => new RandomVaried(BgHsl, FgHsl, TheTheme))
  _Set += Style.Random -> ((BgHsl, FgHsl, TheTheme) => new RandomForeground(BgHsl, FgHsl))
  _Set += Style.Grayscale -> ((BgHsl, FgHsl, TheTheme) => new Grayscale(BgHsl, FgHsl, TheTheme))

  def Get(TheTheme: Theme, TheStyle: Style, Background: Color, Foreground: Color): ColorStrategy =
  {
    import Implicits._
    val FgHsl: HslColor = if (TheTheme == LightBgDarkFg) Foreground.Darken else Foreground.Lighten
    if (Background.getAlpha != 255) /* Not interfering with any transparent color */
      return _Set.get(TheStyle)(Background, FgHsl, TheTheme)
    val BgHsl: HslColor = if (TheTheme == LightBgDarkFg) Background.Lighten else Background.Darken
    _Set.get(TheStyle)(HslColor.toRGB(BgHsl), FgHsl, TheTheme)
  }
}

abstract class ColorStrategy protected(protected var Background: Color, protected var Foreground: HslColor)
{
  protected val _Seed = new Random(DateTime.Now.Second)

  def GetCurrentColor(): Color
}

class FixedForeground(Background: Color, Foreground: HslColor) extends ColorStrategy(Background, Foreground)
{
  override def GetCurrentColor(): Color = HslColor.toRgb(Foreground)
}

class RandomForeground(Background: Color, Foreground: HslColor) extends FixedForeground(Background, Foreground)
{
  override def GetCurrentColor(): Color =
  {
    Foreground.Hue = _Seed.NextDouble();
    HslColor.toRGB(Foreground);
  }
}

class VariedForeground(Background: Color, Foreground: HslColor, TheTheme: Theme) extends ColorStrategy(Background, Foreground)
{
  protected var Range: Double
  /* Dark foreground needed, so Luminosity is reduced to somewhere between 0 & 0.5
  * Saturation is full so the color comes out, removing all blackness/greyness
  * For Light foreground Luminosity is kept between 0.5 & 1
  */
  Foreground.Saturation = 1.0
  Range = if (TheTheme == LightBgDarkFg) 0d else 0.5

  override def GetCurrentColor(): Color =
  {
    Foreground.getLuminance = (_Seed.NextDouble() * 0.5) + Range
    HslColor.toRGB(Foreground)
  }
}

class RandomVaried(Background: Color, Foreground: HslColor, TheTheme: Theme) extends VariedForeground(Background, Foreground, TheTheme)
{
  override def GetCurrentColor(): Color =
  {
    _Foreground.Hue = _Seed.NextDouble();
    base.GetCurrentColor();
  }
}

class Grayscale(Background: Color, Foreground: HslColor, TheTheme: Theme) extends VariedForeground(Background, Foreground, TheTheme)
{
  /* Saturation is 0 - Meaning no color specified by hue can be seen at all.
  * So luminance is now reduced to showing grayscale */
  _Foreground.Saturation = 0.0;
  _Background = Color.FromArgb(Background.A, if (TheTheme == LightBgDarkFg) Color.White else Color.Black);
}


object Theme extends Enumeration
{
  type Theme = Value
  val DarkBgLightFg = Value("DarkBgLightFg")
  val LightBgDarkFg = Value("LightBgDarkFg")
}

object Style extends Enumeration
{
  type Style = Value
  val Fixed = Value("Fixed")
  val Random = Value("Random")
  val Varied = Value("Varied")
  val RandomVaried = Value("RandomVaried")
  val Grayscale = Value("Grayscale")
}

object Implicits
{
  implicit def ExtendColor(Given: Color) = new ColorExtensions(Given)
}

class ColorExtensions(Given: Color)
{
  def Lighten(Given: Color): HslColor =
  {
    val ColorHsl = HslColor.fromRGB(Given)
    if (ColorHsl.Luminosity < 0.5) ColorHsl.Luminosity = 0.75
    ColorHsl
  }

  def Darken(Given: Color): HslColor =
  {
    val ColorHsl = HslColor.fromRGB(Given)
    if (ColorHsl.Luminosity > 0.5) ColorHsl.Luminosity = 0.25
    ColorHsl
  }
}
