package zasz.me

import java.awt.geom.{Point2D, Rectangle2D}
import collection.immutable.HashMap
import scala.Predef._
import sun.font.FontFamily
import java.awt.image.BufferedImage
import java.awt.{RenderingHints, Graphics2D, Font, Color}
import zasz.me.enums.StringFormat._
import zasz.me.enums.Theme
import zasz.me.enums.TagDisplayStrategy
import zasz.me.enums.Style
import java.awt.font.FontRenderContext
import java.lang.{Float=>decimal, Boolean, Math, Exception}

class Disorganizer(var Tags: HashMap[String, Int], var Width: decimal, var Height: decimal)
{
  val _Increment: (decimal) => decimal = (X: decimal) => X + 1
  val _Decrement = (X: decimal) => X - 1
  val _Die = (Msg: String) => throw new Exception(Msg)

  if (null == Tags || 0 == Tags.Count)
    _Die("Argument Exception, No Tags to disorganize");
  if (Width < 30 || Height < 30)
    _Die("Way too low Width or Height for the cloud to be useful");

  private val _MaxEdgeSize: Int = if (Width >= Height) Width else Height;
  private val _SpiralEndSentinel: Point2D.Float = new Point2D.Float(_MaxEdgeSize + 10, _MaxEdgeSize + 10);
  private val _MainArea: Rectangle2D.Float = new Rectangle2D.Float(0, 0, Width, Height);
  private val _TagsSorted: Map[String, Int] = Tags.toList.sortBy(_._2)
  private val _LowestWeight: Int = _TagsSorted.Last().Value;
  private val _HighestWeight: Int = _TagsSorted.First().Value;

  private var _Occupied: List[Rectangle2D.Float] = List[Rectangle2D.Float](_TagsSorted.Count + 4);
  var _EdgeDirection: decimal => decimal
  var _WeightSpan: Int
  var _FontHeightSpan: decimal
  var _Center: Point2D.Float
  var _CurrentCorner: Point2D.Float
  var _CurrentEdgeSize: Int
  var _SleepingEdge: Boolean
  var _ServiceObjectNew: Boolean


  /// <summary>
  ///   Default is Times New Roman
  /// </summary>
  var SelectedFont: FontFamily

  /// <summary>
  ///   Default is false, Enable to start seeing Word boundaries used for
  ///   collision detection.
  /// </summary>
  var ShowWordBoundaries: Boolean

  /// <summary>
  ///   Set this to true, if vertical must needs to appear with RHS as floor
  ///   Default is LHS is the floor and RHS is ceiling of the Text.
  /// </summary>
  var VerticalTextRight: Boolean

  /// <summary>
  ///   Size of the smallest String in the TagCloud
  /// </summary>
  var MinimumFontSize: decimal

  /// <summary>
  ///   Size of the largest String in the TagCloud
  /// </summary>
  var MaximumFontSize: decimal

  /// <summary>
  ///   Use <code>DisplayStrategy.Get()</code> to get a Display Strategy
  ///   Default is RandomHorizontalOrVertical.
  /// </summary>
  var DisplayChoice: DisplayStrategy

  /// <summary>
  ///   Use <code>ColorStrategy.Get()</code> to get a Color Strategy
  ///   Default is white background and random darker foreground colors.
  /// </summary>
  var ColorChoice: ColorStrategy

  /// <summary>
  ///   A rotate transform will be applied on the whole image based on this
  ///   Angle in degrees. Which means the Boundaries are not usable for hover animations
  ///   in CSS/HTML.
  /// </summary>
  var Angle: Integer

  /// <summary>
  ///   Default is false. Set this to true to crop out blank background.
  /// </summary>
  var Crop: Boolean

  /// <summary>
  ///   Default is 30px.
  /// </summary>
  var Margin: decimal

  /// <summary>
  ///   Words that were not rendered because of non-availability
  ///   of free area to render them. If count is anything other than 0
  ///   use a bigger bitmap as input with more area.
  /// </summary>
  var WordsSkipped: HashMap[String, Integer] = new HashMap[String, Integer];

  ApplyDefaults

  def ApplyDefaults() {
    SelectedFont = new FontFamily("Times New Roman")
    MinimumFontSize = 1f
    MaximumFontSize = 5f
    Angle = 0
    DisplayChoice = DisplayStrategy.Get(TagDisplayStrategy.RandomHorizontalOrVertical)
    ColorChoice = ColorStrategy.Get(Theme.LightBgDarkFg, Style.RandomVaried, Color.WHITE, Color.BLACK)
    VerticalTextRight = false
    ShowWordBoundaries = false
    Margin = 30f
    /* Adding 4 Rectangles on the border to make sure that words dont go outside the border.
    * Words going outside the border will collide on these and hence be placed elsewhere.
    */
    _Occupied += new Rectangle2D.Float(0, -1, Width, 1)
    _Occupied += new Rectangle2D.Float(-1, 0, 1, Height)
    _Occupied += new Rectangle2D.Float(0, Height, Width, 1)
    _Occupied += new Rectangle2D.Float(Width, 0, 1, Height)
  }

  def Construct(): BufferedImage =
  {
    if (_ServiceObjectNew) _ServiceObjectNew = false
    else _Die("This object has been used. Dispose this, create and use a new Service object.")
    val cloudImage = new BufferedImage(Width, Height)
    val GImage: Graphics2D = cloudImage.getGraphics
    GImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    _Center = new Point2D.Float(cloudImage.Width / 2f, cloudImage.Height / 2f);
    if (Angle != 0) GImage.rotate((Angle * Math.PI / 180), _Center.x, _Center.y)
    _WeightSpan = _HighestWeight - _LowestWeight;
    if (MaximumFontSize < MinimumFontSize)
      _Die("MaximumFontSize is less than MinimumFontSize");
    _FontHeightSpan = MaximumFontSize - MinimumFontSize
    GImage.clearRect(ColorChoice.GetBackGroundColor())

    var Tag: (String, Int) = null
    while(_TagsSorted.iterator.hasNext)
    {
      Tag = _TagsSorted.iterator.next()
      val FontToApply = new Font(SelectedFont, CalculateFontSize(Tag.Value));
      val frc: FontRenderContext = g2.getFontRenderContext();
      var bounds: Rectangle2D = g2.getFont().getStringBounds(Tag, frc);
      val Format: StringFormat = DisplayChoice.GetFormat();
      if (Format == Vertical)
        bounds = new Rectangle2D.Float(bounds.getX, bounds.getY, bounds.getHeight, bounds.getWidth)
      val TopLeft:Point2D.Float = CalculateWhere(bounds);
      /* Strategy chosen display format, failed to be placed */
      if (TopLeft.equals(_SpiralEndSentinel))
      {
        WordsSkipped.Add(Tag.Key, Tag.Value);
        continue;
      }
      val TextCenter: Point2D.Float = if(IsVertical & VerticalTextRight)
        new Point2D.Float(TopLeft.X + (bounds.Width / 2f), TopLeft.Y + (StringBounds.Height / 2f))
      else TopLeft;
      g2d.setColor(ColorChoice.GetCurrentColor())
      if (IsVertical & VerticalTextRight) GImage.rotate(-Math.PI, TextCenter.getX, TextCenter.getY);
      GImage.setFont(FontToApply)
      GImage.drawString(Tag.Key, TopLeft.getX, TopLeft.getY);
      if (IsVertical & VerticalTextRight) GImage.rotate(Math.PI, TextCenter.getX, TextCenter.getY);
      if (ShowWordBoundaries)
        GImage.drawRect(TopLeft.X, TopLeft.Y, bounds.getWidth, bounds.getHeight);
      _Occupied.Add(new Rectangle2D.Float(TopLeft, StringBounds));
    }
    GImage.dispose()
    List.range(0,4) foreach(X => _Occupied.remove(X))
    if (Crop) CropAndTranslate(cloudImage) else cloudImage
  }

  def CalculateWhere(Measure: Rectangle2D.Float): Point2D.Float = {
      _CurrentEdgeSize = 1;
      _SleepingEdge = true;
      _CurrentCorner = _Center;

      var CurrentPoint:Point2D.Float = _Center;
      while (TryPoint(CurrentPoint, Measure) == false)
          CurrentPoint = GetNextPoIntegerInEdge(CurrentPoint);
      CurrentPoint;
  }

  def TryPoint(TrialPoint: Point2D.Float, Rectangle: Rectangle2D.Float): Boolean = {
      if (TrialPoint.equals(_SpiralEndSentinel)) return true;
      val TrailRectangle = new Rectangle2D.Float(TrialPoint.x, TrialPoint.y, Rectangle.getWidth, Rectangle.getHeight);
      !_Occupied.forall(It => !It.IntersectsWith(TrailRectangle));
  }

  /*
   * This method gives poIntegers that crawls along an edge of the spiral, described below.
   */
  def GetNextPoIntegerInEdge(Cur: Point2D.Float): Point2D.Float = {
    var Current = Cur
      do
      {
          if (Current.equals(_CurrentCorner))
          {
              _CurrentCorner = GetSpiralNext(_CurrentCorner);
              if (_CurrentCorner.equals(_SpiralEndSentinel)) return _SpiralEndSentinel;
          }
          Current = if(Current.x == _CurrentCorner.x)
                        new Point2D.Float(Current.X, _EdgeDirection((decimal)(Current.getY)))
                        else new Point2D.Float(_EdgeDirection((decimal)(Current.getX)), Current.y)
      } while (!_MainArea.Contains(Current))
      Current
  }

  /* Imagine a grid of 5x5 poIntegers, and 0,0 and 4,4 are the topright and bottomleft respectively.
   * You can move in a spiral by navigating as follows:
   * 1. Inc GivenPoInteger's X by 1 and return it.
   * 2. Inc GivenPoInteger's Y by 1 and return it.
   * 3. Dec GivenPoInteger's X by 2 and return it.
   * 4. Dec GivenPoInteger's Y by 2 and return it.
   * 5. Inc GivenPoInteger's X by 3 and return it.
   * 6. Inc GivenPoInteger's Y by 3 and return it.
   * 7. Dec GivenPoInteger's X by 4 and return it.
   * 8. Dec GivenPoInteger's Y by 4 and return it.
   *
   * I'm calling the values 1,2,3,4 etc as _EdgeSize. Any joining of poIntegers in a graph is an edge.
   * To find out if we need to increment or decrement I'm using the condition _EdgeSize is even or not.
   * To increment EdgeSize, using a booleanean _SleepingEdge to count upto 2 steps. I'll blog about this later
   * at chandruon.net!
   *
   *       0  1  2  3  4
   *   0   X  X  X  X  X     .-------->
   *   1   X  X  X  X  X     | .-----.
   *   2   X  X  X  X  X     | | --. |
   *   3   X  X  X  X  X     | '---' |
   *   4   X  X  X  X  X     '-------'
   *
   *
   * Depth of Recursion is meant to be at most ONE in this method,
   * and only when outlying edges are to be skipped.
   *
   */

  def GetSpiralNext(PreviousCorner: Point2D.Float): Point2D.Float = {
      var X: decimal = PreviousCorner.X
      var Y = PreviousCorner.Y;
      val EdgeSizeEven: Boolean = (_CurrentEdgeSize & 1) == 0;

      if (_SleepingEdge)
      {
          X = if(EdgeSizeEven) PreviousCorner.X - _CurrentEdgeSize else PreviousCorner.X + _CurrentEdgeSize;
          _SleepingEdge = false;
          /* Next edge will be standing. Sleeping = Parallal to X-Axis; Standing = Parallal to Y-Axis */
      }
      else
      {
          Y = if(EdgeSizeEven) PreviousCorner.Y - _CurrentEdgeSize else PreviousCorner.Y + _CurrentEdgeSize;
          _CurrentEdgeSize++;
          _SleepingEdge = true;
      }

      _EdgeDirection = if(EdgeSizeEven) _Decrement else _Increment

      /* If the spiral widens to a poInteger where its arms are longer than the Height & Width,
       * it's time to end the spiral and give up placing the word. There is no 'poInteger'
       * (no pun Integerended) in going for wider spirals, as you are out of bounds now.
       * Our spiral is an Archimedean Right spiral, made up of Line segments @
       * right-angles to each other.
       */
      if(_CurrentEdgeSize > _MaxEdgeSize) _SpiralEndSentinel else new Point2D.decimal(X, Y);
  }

  // Range Mapping
  def CalculateFontSize(Weight:Integer): decimal = {
      // Strange case where all tags have equal weights
      if (_WeightSpan == 0) return (MinimumFontSize + MaximumFontSize)/2f;
      // Convert the Weight Integero a 0-1 range (decimal)
      val WeightScaled: decimal = (Weight - _LowestWeight)/(decimal) _WeightSpan;
      // Convert the 0-1 range Integero a value in the Font range.
      MinimumFontSize + (WeightScaled*_FontHeightSpan);
  }

  /// <summary>
  ///   Uses the list of occupied areas to
  ///   crop the Bitmap and translates the list of occupied areas
  ///   keeping them consistant with the new cropped bitmap
  /// </summary>
  /// <param name = "CloudToCrop">The bitmap of the cloud to crop</param>
  /// <returns>The cropped version of the bitmap</returns>
  def CropAndTranslate(CloudToCrop: BufferedImage): BufferedImage = {
      var NewTop = _Occupied.collect(It => It.Top).Min() - Margin;
      var NewLeft = _Occupied.collect(It => It.Left).Min() - Margin;

      var Bottom = _Occupied.collect(It => It.Bottom).Max() + Margin;
      var Right = _Occupied.collect(It => It.Right).Max() + Margin;

      if (NewTop < 0) NewTop = 0;
      if (NewLeft < 0) NewLeft = 0;

      if (Bottom > _Height) Bottom = _Height;
      if (Right > _Width) Right = _Width;

      val PopulatedArea = new Rectangle2D.Float(NewLeft, NewTop, Right - NewLeft, Bottom - NewTop);
      _Occupied = _Occupied.collect(It => new Rectangle2D.Float(It.X - NewLeft, It.Y - NewTop, It.Width, It.Height)).ToArrayList();
      CloudToCrop.getSubimage(PopulatedArea.getX, PopulatedArea.getY, PopulatedArea.width, PopulatedArea.height);
  }

  def drawStringWithFormat(g2d: Graphics2D, Format: StringFormat, tag: String, x: Int, y: Int) = {
    if(Format == Vertical){
        g2d.rotate(-2 * Math.PI / angle)
        g2d.drawString(tag, x, y);
        g2d.rotate(2 * Math.PI / angle)
    }
    else g2d.drawString(tag, x, y)
  }
}