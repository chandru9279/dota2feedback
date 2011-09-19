package zasz.me

import java.awt.geom.{Point2D, Rectangle2D}
import collection.mutable.HashMap
import scala.Predef._
import java.awt.image.BufferedImage
import zasz.me.enums.StringFormat._
import zasz.me.enums.Theme
import zasz.me.enums.TagDisplayStrategy
import zasz.me.enums.Style
import collection.mutable.ListBuffer
import java.lang.{Double, Boolean, Math, Exception, Float}
import collection.JavaConverters._
import reflect.BeanProperty
import collection.immutable.ListMap
import java.awt._

class Disorganizer(var InTags: java.util.Map[String, java.lang.Integer], var Width: Int, var Height: Int)
{
  var Tags = InTags.asScala.map(x => (x._1, Int.unbox(x._2)))
  val _Increment: (Double) => Double = (X: Double) => X + 1
  val _Decrement: (Double) => Double = (X: Double) => X - 1
  val _Die = (Msg: String) => throw new Exception(Msg)

  if (null == Tags || 0 == Tags.size)
    _Die("Argument Exception, No Tags to disorganize");
  if (Width < 30 || Height < 30)
    _Die("Way too low Width or Height for the cloud to be useful");

  private val _MaxEdgeSize: Int = if (Width >= Height) Width else Height;
  private val _MainArea: Rectangle2D.Double = new Rectangle2D.Double(0, 0, Width, Height)
  private val _Center = new Point2D.Double(Width / 2f, Height / 2f);
  private val _SpiralEndSentinel: Point2D.Double = new Point2D.Double(_MaxEdgeSize + 10, _MaxEdgeSize + 10);
  private val _TagsSorted = ListMap[String, Int](Tags.toList.sortBy[Int](_._2): _*)
  private val _HighestWeight: Int = _TagsSorted.last._2
  private val _LowestWeight: Int = _TagsSorted.head._2
  private val _WeightSpan: Int = _HighestWeight - _LowestWeight;
  private val _Padding: Int = 8

  private var _Occupied: ListBuffer[Rectangle2D.Double] = new ListBuffer[Rectangle2D.Double]()
  private var _EdgeDirection: Double => Double = _Increment
  private var _FontHeightSpan: Float = 0.0f
  private var _CurrentCorner: Point2D.Double = null
  private var _CurrentEdgeSize: Int = 0
  private var _SleepingEdge: Boolean = false
  private var _ServiceObjectNew: Boolean = true

  /// <summary>
  ///   Default is Times New Roman
  /// </summary>
  @BeanProperty var SelectedFont: Font = new Font("Times New Roman", 0, 15)

  /// <summary>
  ///   Default is false, Enable to start seeing Word boundaries used for
  ///   collision detection.
  /// </summary>
  @BeanProperty var ShowWordBoundaries: Boolean = false

  /// <summary>
  ///   Set this to true, if vertical must needs to appear with RHS as floor
  ///   Default is LHS is the floor and RHS is ceiling of the Text.
  /// </summary>
  @BeanProperty var VerticalTextRight: Boolean = true

  /// <summary>
  ///   Size of the smallest String in the TagCloud
  /// </summary>
  @BeanProperty var MinimumFontSize: Float = 12f

  /// <summary>
  ///   Size of the largest String in the TagCloud
  /// </summary>
  @BeanProperty var MaximumFontSize: Float = 82f

  /// <summary>
  ///   Use <code>DisplayStrategy.Get()</code> to get a Display Strategy
  ///   Default is RandomHorizontalOrVertical.
  /// </summary>
  @BeanProperty var DisplayChoice: DisplayStrategy = DisplayStrategy.Get(TagDisplayStrategy.AllVertical)

  /// <summary>
  ///   Use <code>ColorStrategy.Get()</code> to get a Color Strategy
  ///   Default is white background and random darker foreground colors.
  /// </summary>
  @BeanProperty var ColorChoice: ColorStrategy = ColorStrategy.Get(Theme.LightBgDarkFg, Style.RandomVaried, Color.WHITE, Color.BLACK)

  /// <summary>
  ///   A rotate transform will be applied on the whole image based on this
  ///   Angle in degrees. Which means the Boundaries are not usable for hover animations
  ///   in CSS/HTML.
  /// </summary>
  @BeanProperty var Angle: Int = 0

  /// <summary>
  ///   Default is false. Set this to true to crop out blank background.
  /// </summary>
  @BeanProperty var Crop: Boolean = false

  /// <summary>
  ///   Default is 30px.
  /// </summary>
  @BeanProperty var Margin: Double = 30f

  /// <summary>
  ///   Words that were not rendered because of non-availability
  ///   of free area to render them. If count is anything other than 0
  ///   use a bigger bitmap as input with more area.
  /// </summary>
  @BeanProperty var WordsSkipped: HashMap[String, Int] = new HashMap[String, Int];


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
    if (Angle != 0) GImage.rotate((Angle * Math.PI / 180), _Center.x, _Center.y)
    if (MaximumFontSize < MinimumFontSize)
      _Die("MaximumFontSize is less than MinimumFontSize");
    _FontHeightSpan = MaximumFontSize - MinimumFontSize
    GImage.setBackground(ColorChoice.GetBackGroundColor())
    GImage.clearRect(0, 0, Width, Height)

    _TagsSorted.foreach(X => drawTag(X._1, X._2, GImage))
    GImage.dispose()
    _Occupied.remove(0, 4)
    if (Crop) CropAndTranslate(cloudImage) else cloudImage
  }

  def drawTag(tag: String, weight: Int, GImage: Graphics2D) =
  {
    GImage.setFont(SelectedFont.deriveFont(CalculateFontSize(weight)))
    val metrics: FontMetrics = GImage.getFontMetrics
    val lowerYaxisBy: Int = metrics.getMaxDescent + _Padding / 2;
    val raiseXaxisBy: Int = _Padding / 2;
    var bounds = new Rectangle2D.Double(0, 0, metrics.stringWidth(tag) + _Padding,
      metrics.getHeight + _Padding)
    val IsVertical: Boolean = DisplayChoice.GetFormat() == Vertical
    if (IsVertical)
      bounds = new Rectangle2D.Double(0, 0, bounds.height, bounds.width)
    val TopLeft: Point2D.Double = CalculateWhere(bounds);
    /* Strategy chosen display format, failed to be placed */
    if (TopLeft.equals(_SpiralEndSentinel))
    {
      WordsSkipped.put(tag, weight)
    }
    else
    {
      GImage.setColor(ColorChoice.GetCurrentColor())
      if (IsVertical)
      {
        GImage.rotate(Math.PI / 2.0, TopLeft.x, TopLeft.y)
        GImage.drawString(tag, (TopLeft.x + raiseXaxisBy).toFloat, (TopLeft.y - lowerYaxisBy).toFloat)
        GImage.rotate(-Math.PI / 2.0, TopLeft.x, TopLeft.y)
      } else GImage.drawString(tag, (TopLeft.x + raiseXaxisBy).toFloat, (TopLeft.y - lowerYaxisBy + bounds.height).toFloat)
      val boundary = new Rectangle2D.Double(TopLeft.x, TopLeft.y, bounds.width, bounds.height)
      if (ShowWordBoundaries) GImage.draw(boundary)
      _Occupied + boundary
    }
  }

  def CalculateWhere(Measure: Rectangle2D.Double): Point2D.Double =
  {
    _CurrentEdgeSize = 1;
    _SleepingEdge = true;
    _CurrentCorner = _Center;

    var CurrentPoint: Point2D.Double = _Center;
    while (TryPoint(CurrentPoint, Measure))
      CurrentPoint = GetNextPointInEdge(CurrentPoint);
    CurrentPoint;
  }

  def TryPoint(TrialPoint: Point2D.Double, Rectangle: Rectangle2D.Double): Boolean =
  {
    if (TrialPoint.equals(_SpiralEndSentinel)) return false;
    val TrailRectangle = new Rectangle2D.Double(TrialPoint.x, TrialPoint.y, Rectangle.width, Rectangle.height);
    _Occupied.exists(_.intersects(TrailRectangle))
  }

  /*
   * This method gives poIntegers that crawls along an edge of the spiral, described below.
   */
  def GetNextPointInEdge(Cur: Point2D.Double): Point2D.Double =
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
        new Point2D.Double(Current.x, _EdgeDirection(Current.y))
      else new Point2D.Double(_EdgeDirection(Current.x), Current.y)
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
   */

  def GetSpiralNext(PreviousCorner: Point2D.Double): Point2D.Double =
  {
    var X: Double = PreviousCorner.x
    var Y = PreviousCorner.y;
    val EdgeSizeEven: Boolean = (_CurrentEdgeSize & 1) == 0;

    if (_SleepingEdge)
    {
      X = if (EdgeSizeEven) PreviousCorner.x - _CurrentEdgeSize else PreviousCorner.x + _CurrentEdgeSize;
      _SleepingEdge = false;
      /* Next edge will be standing. Sleeping = Parallal to X-Axis; Standing = Parallal to Y-Axis */
    }
    else
    {
      Y = if (EdgeSizeEven) PreviousCorner.y - _CurrentEdgeSize else PreviousCorner.y + _CurrentEdgeSize;
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
    // Convert the Weight Integer to a 0-1 range (Double)
    val WeightScaled: Float = (Weight - _LowestWeight.toFloat) / _WeightSpan;
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
    var NewTop = _Occupied.map(x => scala.Double.box(x.y)).min(Ordering[Double]) - Margin
    var NewLeft = _Occupied.map(x => scala.Double.box(x.x)).min(Ordering[Double]) - Margin

    var Bottom = _Occupied.map(x => scala.Double.box(x.y + x.height)).max(Ordering[Double]) + Margin
    var Right = _Occupied.map(x => scala.Double.box(x.x + x.width)).max(Ordering[Double]) + Margin

    if (NewTop < 0) NewTop = 0;
    if (NewLeft < 0) NewLeft = 0;

    if (Bottom > Height) Bottom = Height;
    if (Right > Width) Right = Width;

    val PopulatedArea = new Rectangle2D.Double(NewLeft, NewTop, Right - NewLeft, Bottom - NewTop);
    _Occupied = _Occupied.map(It => new Rectangle2D.Double(It.x - NewLeft, It.y - NewTop, It.width, It.height))
    CloudToCrop.getSubimage(PopulatedArea.x.toInt, PopulatedArea.y.toInt, PopulatedArea.width.toInt, PopulatedArea.height.toInt);
  }
}