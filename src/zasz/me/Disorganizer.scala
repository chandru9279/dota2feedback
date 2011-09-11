package zasz.me

import java.awt.geom.{Point2D, Rectangle2D}
import collection.mutable.HashMap
import scala.Predef._
import java.awt.image.BufferedImage
import java.awt.{RenderingHints, Graphics2D, Font, Color}
import zasz.me.enums.StringFormat._
import zasz.me.enums.Theme
import zasz.me.enums.TagDisplayStrategy
import zasz.me.enums.Style
import collection.mutable.ListBuffer
import java.lang.{Double, Boolean, Math, Exception}
import java.lang.Float

class Disorganizer(var Tags: Map[String, Int], var Width: Int, var Height: Int)
{
  val _Increment: (Double) => Double = (X: Double) => X + 1
  val _Decrement: (Double) => Double = (X: Double) => X - 1
  val _Die = (Msg: String) => throw new Exception(Msg)

  if (null == Tags || 0 == Tags.size)
    _Die("Argument Exception, No Tags to disorganize");
  if (Width < 30 || Height < 30)
    _Die("Way too low Width or Height for the cloud to be useful");

  private val _MaxEdgeSize: Int = if (Width >= Height) Width else Height;
  private val _SpiralEndSentinel: Point2D.Double = new Point2D.Double(_MaxEdgeSize + 10, _MaxEdgeSize + 10);
  private val _MainArea: Rectangle2D.Double = new Rectangle2D.Double(0, 0, Width.asInstanceOf[Double], Height.asInstanceOf[Double]);
  private val _TagsSorted: Map[String, Int] = Tags.toSeq.sortBy(_._2).toMap
  private val _LowestWeight: Int = _TagsSorted.last._2
  private val _HighestWeight: Int = _TagsSorted.head._2

  private var _Occupied: ListBuffer[Rectangle2D.Double] = new ListBuffer[Rectangle2D.Double]()
  var _EdgeDirection: Double => Double = _Increment
  var _WeightSpan: Int = 0
  var _FontHeightSpan: Float = 0.0f
  var _Center: Point2D.Double = null
  var _CurrentCorner: Point2D.Double = null
  var _CurrentEdgeSize: Int = 0
  var _SleepingEdge: Boolean = false
  var _ServiceObjectNew: Boolean = true

  /// <summary>
  ///   Default is Times New Roman
  /// </summary>
  var SelectedFont: Font = new Font("Times New Roman", 0, 15)

  /// <summary>
  ///   Default is false, Enable to start seeing Word boundaries used for
  ///   collision detection.
  /// </summary>
  var ShowWordBoundaries: Boolean = false

  /// <summary>
  ///   Set this to true, if vertical must needs to appear with RHS as floor
  ///   Default is LHS is the floor and RHS is ceiling of the Text.
  /// </summary>
  var VerticalTextRight: Boolean = false

  /// <summary>
  ///   Size of the smallest String in the TagCloud
  /// </summary>
  var MinimumFontSize: Float = 1f

  /// <summary>
  ///   Size of the largest String in the TagCloud
  /// </summary>
  var MaximumFontSize: Float = 5f

  /// <summary>
  ///   Use <code>DisplayStrategy.Get()</code> to get a Display Strategy
  ///   Default is RandomHorizontalOrVertical.
  /// </summary>
  var DisplayChoice: DisplayStrategy = DisplayStrategy.Get(TagDisplayStrategy.RandomHorizontalOrVertical)

  /// <summary>
  ///   Use <code>ColorStrategy.Get()</code> to get a Color Strategy
  ///   Default is white background and random darker foreground colors.
  /// </summary>
  var ColorChoice: ColorStrategy = ColorStrategy.Get(Theme.LightBgDarkFg, Style.RandomVaried, Color.WHITE, Color.BLACK)

  /// <summary>
  ///   A rotate transform will be applied on the whole image based on this
  ///   Angle in degrees. Which means the Boundaries are not usable for hover animations
  ///   in CSS/HTML.
  /// </summary>
  var Angle: Integer = 0

  /// <summary>
  ///   Default is false. Set this to true to crop out blank background.
  /// </summary>
  var Crop: Boolean = false

  /// <summary>
  ///   Default is 30px.
  /// </summary>
  var Margin: Double = 30f

  /// <summary>
  ///   Words that were not rendered because of non-availability
  ///   of free area to render them. If count is anything other than 0
  ///   use a bigger bitmap as input with more area.
  /// </summary>
  var WordsSkipped: HashMap[String, Int] = new HashMap[String, Int];


  /* Adding 4 Rectangles on the border to make sure that words dont go outside the border.
  * Words going outside the border will collide on these and hence be placed elsewhere.
  */
  _Occupied += new Rectangle2D.Double(0, -1, Width, 1)
  _Occupied += new Rectangle2D.Double(-1, 0, 1, Height)
  _Occupied += new Rectangle2D.Double(0, Height, Width, 1)
  _Occupied += new Rectangle2D.Double(Width, 0, 1, Height)

  def Construct(): BufferedImage =
  {
    if (_ServiceObjectNew) _ServiceObjectNew = false
    else _Die("This object has been used. Dispose this, create and use a new Service object.")
    val cloudImage = new BufferedImage(Width, Height, BufferedImage.TYPE_INT_ARGB)
    val GImage: Graphics2D = cloudImage.createGraphics()
    GImage.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    _Center = new Point2D.Double(cloudImage.getWidth / 2f, cloudImage.getHeight / 2f);
    if (Angle != 0) GImage.rotate((Angle * Math.PI / 180), _Center.x, _Center.y)
    _WeightSpan = _HighestWeight - _LowestWeight;
    if (MaximumFontSize < MinimumFontSize)
      _Die("MaximumFontSize is less than MinimumFontSize");
    _FontHeightSpan = MaximumFontSize - MinimumFontSize
    GImage.setBackground(ColorChoice.GetBackGroundColor())
    GImage.clearRect(0, 0, Width, Height)

    var Tag: (String, Int) = null
    while (_TagsSorted.iterator.hasNext)
    {
      Tag = _TagsSorted.iterator.next()
      GImage.setFont(SelectedFont.deriveFont(CalculateFontSize(Tag._2)))

      val tempBounds: Rectangle2D = GImage.getFont.getStringBounds(Tag._1, GImage.getFontRenderContext);
      var bounds = new Rectangle2D.Double(tempBounds.getX, tempBounds.getY, tempBounds.getWidth, tempBounds.getHeight)
      val Format: StringFormat = DisplayChoice.GetFormat();
      if (Format == Vertical)
        bounds = new Rectangle2D.Double(bounds.getX, bounds.getY, bounds.getHeight, bounds.getWidth)
      val TopLeft: Point2D.Double = CalculateWhere(bounds);
      /* Strategy chosen display format, failed to be placed */
      if (TopLeft.equals(_SpiralEndSentinel))
      {
        WordsSkipped.+=((Tag._1, Tag._2))
      }
      else
      {
        val IsVertical = Format == Vertical
        val TextCenter: Point2D.Double = if (IsVertical & VerticalTextRight)
          new Point2D.Double(TopLeft.getX + (bounds.getWidth / 2f), TopLeft.getY + (bounds.getHeight / 2f))
        else TopLeft;
        GImage.setColor(ColorChoice.GetCurrentColor())
        if (IsVertical & VerticalTextRight) GImage.rotate(-Math.PI, TextCenter.getX, TextCenter.getY);
        GImage.drawString(Tag._1, TopLeft.getX.toFloat, TopLeft.getY.toFloat);
        if (IsVertical & VerticalTextRight) GImage.rotate(Math.PI, TextCenter.getX, TextCenter.getY);
        val boundary = new Rectangle2D.Double(TopLeft.getX, TopLeft.getY, bounds.getWidth.toInt, bounds.getHeight.toInt)
        if (ShowWordBoundaries) GImage.draw(boundary)
        _Occupied + boundary
      }
    }
    GImage.dispose()
    List.range(0, 4) foreach (X => _Occupied.remove(X))
    if (Crop) CropAndTranslate(cloudImage) else cloudImage
  }

  def CalculateWhere(Measure: Rectangle2D.Double): Point2D.Double =
  {
    _CurrentEdgeSize = 1;
    _SleepingEdge = true;
    _CurrentCorner = _Center;

    var CurrentPoint: Point2D.Double = _Center;
    while (TryPoint(CurrentPoint, Measure) == false)
      CurrentPoint = GetNextPoIntegerInEdge(CurrentPoint);
    CurrentPoint;
  }

  def TryPoint(TrialPoint: Point2D.Double, Rectangle: Rectangle2D.Double): Boolean =
  {
    if (TrialPoint.equals(_SpiralEndSentinel)) return true;
    val TrailRectangle = new Rectangle2D.Double(TrialPoint.x, TrialPoint.y, Rectangle.getWidth, Rectangle.getHeight);
    !_Occupied.forall(It => !It.intersects(TrailRectangle));
  }

  /*
   * This method gives poIntegers that crawls along an edge of the spiral, described below.
   */
  def GetNextPoIntegerInEdge(Cur: Point2D.Double): Point2D.Double =
  {
    var Current = Cur
    do
    {
      if (Current.equals(_CurrentCorner))
      {
        _CurrentCorner = GetSpiralNext(_CurrentCorner);
        if (_CurrentCorner.equals(_SpiralEndSentinel)) return _SpiralEndSentinel;
      }
      Current = if (Current.x == _CurrentCorner.x)
        new Point2D.Double(Current.getX, _EdgeDirection(Current.getY))
      else new Point2D.Double(_EdgeDirection(Current.getX), Current.y)
    } while (!_MainArea.contains(Current))
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

  def GetSpiralNext(PreviousCorner: Point2D.Double): Point2D.Double =
  {
    var X: Double = PreviousCorner.getX
    var Y = PreviousCorner.getY;
    val EdgeSizeEven: Boolean = (_CurrentEdgeSize & 1) == 0;

    if (_SleepingEdge)
    {
      X = if (EdgeSizeEven) PreviousCorner.getX - _CurrentEdgeSize else PreviousCorner.getX + _CurrentEdgeSize;
      _SleepingEdge = false;
      /* Next edge will be standing. Sleeping = Parallal to X-Axis; Standing = Parallal to Y-Axis */
    }
    else
    {
      Y = if (EdgeSizeEven) PreviousCorner.getY - _CurrentEdgeSize else PreviousCorner.getY + _CurrentEdgeSize;
      _CurrentEdgeSize = _CurrentEdgeSize + 1;
      _SleepingEdge = true;
    }

    _EdgeDirection = if (EdgeSizeEven) _Decrement else _Increment

    /* If the spiral widens to a poInteger where its arms are longer than the Height & Width,
    * it's time to end the spiral and give up placing the word. There is no 'poInteger'
    * (no pun Integerended) in going for wider spirals, as you are out of bounds now.
    * Our spiral is an Archimedean Right spiral, made up of Line segments @
    * right-angles to each other.
    */
    if (_CurrentEdgeSize > _MaxEdgeSize) _SpiralEndSentinel else new Point2D.Double(X, Y);
  }

  // Range Mapping
  def CalculateFontSize(Weight: Int): Float =
  {
    // Strange case where all tags have equal weights
    if (_WeightSpan == 0) return (MinimumFontSize + MaximumFontSize) / 2f
    // Convert the Weight Integero a 0-1 range (Double)
    val WeightScaled: Float = (Weight - _LowestWeight) / _WeightSpan;
    // Convert the 0-1 range Integero a value in the Font range.
    (WeightScaled * _FontHeightSpan) + MinimumFontSize
  }

  /// <summary>
  ///   Uses the list of occupied areas to
  ///   crop the Bitmap and translates the list of occupied areas
  ///   keeping them consistant with the new cropped bitmap
  /// </summary>
  /// <param name = "CloudToCrop">The bitmap of the cloud to crop</param>
  /// <returns>The cropped version of the bitmap</returns>
  def CropAndTranslate(CloudToCrop: BufferedImage): BufferedImage =
  {
    var NewTop = _Occupied.map(x => scala.Double.box(x.getY)).min(Ordering[Double]) - Margin
    var NewLeft = _Occupied.map(x => scala.Double.box(x.getX)).min(Ordering[Double]) - Margin

    var Bottom = _Occupied.map(x => scala.Double.box(x.getY + x.getHeight)).max(Ordering[Double]) + Margin
    var Right = _Occupied.map(x => scala.Double.box(x.getX + x.getWidth)).max(Ordering[Double]) + Margin

    if (NewTop < 0) NewTop = 0;
    if (NewLeft < 0) NewLeft = 0;

    if (Bottom > Height) Bottom = Height;
    if (Right > Width) Right = Width;

    val PopulatedArea = new Rectangle2D.Double(NewLeft, NewTop, Right - NewLeft, Bottom - NewTop);
    _Occupied = _Occupied.map(It => new Rectangle2D.Double(It.getX - NewLeft, It.getY - NewTop, It.getWidth, It.getHeight))
    CloudToCrop.getSubimage(PopulatedArea.getX.toInt, PopulatedArea.getY.toInt, PopulatedArea.getWidth.toInt, PopulatedArea.getHeight.toInt);
  }

  //
  //  def drawStringWithFormat(g2d: Graphics2D, Format: StringFormat, tag: String, x: Int, y: Int) = {
  //    if(Format == Vertical) {
  //        g2d.rotate(-2 * Math.PI / angle)
  //        g2d.drawString(tag, x, y);
  //        g2d.rotate(2 * Math.PI / angle)
  //    }
  //    else g2d.drawString(tag, x, y)
  //  }
}