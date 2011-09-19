package zasz.me

import enums.Style._
import enums.Theme._
import java.awt._
import models.HslColor
import util.{Random => Rand}
import java.util.Date

object ColorStrategy
{
  private var strategySet = Map[Style, (Color, HslColor, Theme) => ColorStrategy]()
  strategySet += Fixed -> ((BgHsl, FgHsl, TheTheme) => new FixedForeground(BgHsl, FgHsl))
  strategySet += Varied -> ((BgHsl, FgHsl, TheTheme) => new VariedForeground(BgHsl, FgHsl, TheTheme))
  strategySet += RandomVaried -> ((BgHsl, FgHsl, TheTheme) => new RandomVaried(BgHsl, FgHsl, TheTheme))
  strategySet += Random -> ((BgHsl, FgHsl, TheTheme) => new RandomForeground(BgHsl, FgHsl))
  strategySet += Grayscale -> ((BgHsl, FgHsl, TheTheme) => new Grayscale(BgHsl, FgHsl, TheTheme))

  def Get(TheTheme: Theme, TheStyle: Style, Background: Color, Foreground: Color): ColorStrategy =
  {
    import Implicits._
    val FgHsl: HslColor = if (TheTheme == LightBgDarkFg) Foreground.Darken() else Foreground.Lighten()
    if (Background.getAlpha != 255) /* Not interfering with any transparent color */
      return strategySet(TheStyle).apply(Background, FgHsl, TheTheme)
    val BgHsl: HslColor = if (TheTheme == LightBgDarkFg) Background.Lighten() else Background.Darken()
    strategySet(TheStyle).apply(BgHsl.getRGB, FgHsl, TheTheme)
  }
}

abstract class ColorStrategy protected(protected var Background: Color, protected var Foreground: HslColor)
{
  protected val _Seed = new Rand(new Date().getTime)

  def GetBackGroundColor(): Color = Background

  def GetCurrentColor(): Color
}

class FixedForeground(BackgroundFF: Color, ForegroundFF: HslColor) extends ColorStrategy(BackgroundFF, ForegroundFF)
{
  override def GetCurrentColor(): Color = Foreground.getRGB
}

class RandomForeground(BackgroundRF: Color, ForegroundRF: HslColor) extends FixedForeground(BackgroundRF, ForegroundRF)
{
  override def GetCurrentColor(): Color =
  {
    Foreground.adjustHue(_Seed.nextInt(361))
  }
}

class VariedForeground(BackgroundVF: Color, ForegroundVF: HslColor, protected val TheTheme: Theme) extends ColorStrategy(BackgroundVF, ForegroundVF)
{
  protected var Range: Int = 0
  /* Dark foreground needed, so Luminosity is reduced to somewhere between 0 & 0.5
  * Saturation is full so the color comes out, removing all blackness/greyness
  * For Light foreground Luminosity is kept between 0.5 & 1
  */
  Foreground = new HslColor(Foreground.adjustSaturation(100))
  Range = if (TheTheme == LightBgDarkFg) 0 else 50

  override def GetCurrentColor(): Color =
  {
    val fl: Int = _Seed.nextInt(50) + 1 + Range
    Foreground.adjustLuminance(fl)
  }
}

class RandomVaried(BackgroundRV: Color, ForegroundRV: HslColor, TheThemeRV: Theme) extends VariedForeground(BackgroundRV, ForegroundRV, TheThemeRV)
{
  override def GetCurrentColor(): Color =
  {
    val fl: Float = _Seed.nextInt(100) + 1
    val hue: Color = Foreground.adjustHue(fl)
    val some = new HslColor(hue)

    Foreground = new HslColor(hue)

    super.GetCurrentColor();
  }
}

class Grayscale(BackgroundGS: Color, ForegroundGS: HslColor, TheThemeGS: Theme) extends VariedForeground(BackgroundGS, ForegroundGS, TheThemeGS)
{
  /* Saturation is 0 - Meaning no color specified by hue can be seen at all.
  * So luminance is now reduced to showing grayscale */
  Foreground = new HslColor(Foreground.adjustSaturation(0))
  val Temp = if (TheTheme == LightBgDarkFg) Color.WHITE else Color.BLACK
  Background = new Color(Temp.getRed, Temp.getGreen, Temp.getBlue, Background.getAlpha)
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
    if (ColorHsl.getLuminance < 50) return new HslColor(ColorHsl.adjustLuminance(75))
    ColorHsl
  }

  def Darken(): HslColor =
  {
    val ColorHsl = new HslColor(Given)
    if (ColorHsl.getLuminance > 50) return new HslColor(ColorHsl.adjustLuminance(25))
    ColorHsl
  }
}
