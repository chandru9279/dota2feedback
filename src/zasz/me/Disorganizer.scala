package zasz.me

import java.lang.Exception
import java.awt.geom.{Point2D, Rectangle2D}
import java.awt.geom.Rectangle2D.Float
import java.util.{ArrayList}
import collection.immutable.HashMap
import scala.Predef._
import sun.font.FontFamily
import java.awt.image.BufferedImage
import java.awt.{Font, Graphics, Color}

class Disorganizer(var Tags: HashMap[String, Int], var Width: Integer, var Height: Integer)
{
  val _Increment: (Float) => Float = (It: Float) => It ++
  val _Decrement = (It: Float) => It --
  val _Die = (Msg: String) => throw new Exception(Msg)

  if (null == Tags || 0 == Tags.Count)
    _Die("Argument Exception, No Tags to disorganize");
  if (Width < 30 || Height < 30)
    _Die("Way too low Width or Height for the cloud to be useful");

  private val _MaxEdgeSize: Int = if (Width >= Height) Width else Height;
  private val _SpiralEndSentinel: Point2D.Float = new Point2D.Float(_MaxEdgeSize + 10, _MaxEdgeSize + 10);
  private val _MainArea: Rectangle2D.Float = new Rectangle2D.Float()(0, 0, Width, Height);
  private val _TagsSorted: HashMap[String, Int] = Tags.toList.sortBy(_._2)
  private val _LowestWeight: Int = _TagsSorted.Last().Value;
  private val _HighestWeight: Int = _TagsSorted.First().Value;
  private val _Occupied: ArrayList[Rectangle2D.Float] = new ArrayList[Rectangle2D.Float](_TagsSorted.Count + 4);

  private var _EdgeDirection: (Float, Float) => Float;
  private var _WeightSpan: Int
  private var _FontHeightSpan: Float
  private var _Center: Point2D.Float
  private var _CurrentCorner: Point2D.Float
  private var _CurrentEdgeSize: Int
  private var _SleepingEdge: Boolean
  private var _ServiceObjectNew: Boolean


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
  var MinimumFontSize: Float

  /// <summary>
  ///   Size of the largest String in the TagCloud
  /// </summary>
  var MaximumFontSize: Float

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
  var Margin: Float

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
    ColorChoice = ColorStrategy.Get(Theme.LightBgDarkFg, Style.RandomVaried, Color.White, Color.Black)
    VerticalTextRight = false
    ShowWordBoundaries = false
    Margin = 30f
    /* Adding 4 Rectangles on the border to make sure that words dont go outside the border.
    * Words going outside the border will collide on these and hence be placed elsewhere.
    */
    _Occupied += new Rectangle2D.Float(0, -1, _Width, 1)
    _Occupied += new Rectangle2D.Float(-1, 0, 1, _Height)
    _Occupied += new Rectangle2D.Float(0, _Height, _Width, 1)
    _Occupied += new Rectangle2D.Float(_Width, 0, 1, _Height)
  }

  def Construct(Borders: HashMap[String, Rectangle2D.Float]): BufferedImage =
  {
    if (_ServiceObjectNew) _ServiceObjectNew = false
    else _Die("This object has been used. Dispose this, create and use a new Service object.")
    var TheCloudBitmap = new BufferedImage(_Width, _Height)
    val GImage: Graphics = Graphics.FromImage(TheCloudBitmap);
    GImage.TextRenderingHInteger = TextRenderingHInteger.AntiAlias;
    _Center = new Point2D.Float(TheCloudBitmap.Width / 2f, TheCloudBitmap.Height / 2f);
    if (Angle != 0) GImage.Rotate(_Center, Angle);
    _WeightSpan = _HighestWeight - _LowestWeight;
    if (MaximumFontSize < MinimumFontSize)
      _Die("MaximumFontSize is less than MinimumFontSize");
    _FontHeightSpan = MaximumFontSize - MinimumFontSize;
    GImage.Clear(ColorChoice.GetBackGroundColor());

    foreach(var Tag: KeyValuePair[String, Integer]  in _TagsSorted)
    {
      val FontToApply = new Font(SelectedFont, CalculateFontSize(Tag.Value));
      val StringBounds: Size2D = GImage.MeasureString(Tag.Key, FontToApply);


      StringFormat Format = DisplayChoice.GetFormat();
      boolean IsVertical = Format.FormatFlags.HasFlag(StringFormatFlags.DirectionVertical);
      if (IsVertical)
      {
        float StringWidth = StringBounds.Width;
        StringBounds.Width = StringBounds.Height;
        StringBounds.Height = StringWidth;
      }
      Point2D.Float TopLeft = CalculateWhere(StringBounds);
      /* Strategy chosen display format, failed to be placed */
      if (TopLeft.Equals(_SpiralEndSentinel))
      {
        WordsSkipped.Add(Tag.Key, Tag.Value);
        continue;
      }
      Point2D.Float TextCenter = IsVertical & VerticalTextRight
      ? new Point2D.Float(TopLeft.X + (StringBounds.Width / 2f),
      TopLeft.Y + (StringBounds.Height / 2f))
      : TopLeft;
      var CurrentBrush = new SolidBrush(ColorChoice.GetCurrentColor());
      if (IsVertical & VerticalTextRight) GImage.Rotate(TextCenter, -180);
      GImage.DrawString(Tag.Key, FontToApply, CurrentBrush, TopLeft, Format);
      if (IsVertical & VerticalTextRight) GImage.Rotate(TextCenter, 180);
      if (ShowWordBoundaries)
        GImage.DrawRectangle(new Pen(CurrentBrush), TopLeft.X, TopLeft.Y, StringBounds.Width,
          StringBounds.Height);
      _Occupied.Add(new Rectangle2D.Float(TopLeft, StringBounds));
    }
    GImage.Dispose();
    _Occupied.RemoveRange(0, 4);
    if (Crop)
      TheCloudBitmap = CropAndTranslate(TheCloudBitmap);
    Borders = _Occupied
      .Zip(_TagsSorted.Keys.Where(Word => !WordsSkipped.ContainsKey(Word)), (Rect, Tag) => new
      {
        Rect, Tag
      })
      .ToHashMap(It => It.Tag, It => It.Rect);
    return TheCloudBitmap;
  }

        private Point2D.Float CalculateWhere(SizeF Measure)
        {
            _CurrentEdgeSize = 1;
            _SleepingEdge = true;
            _CurrentCorner = _Center;

            Point2D.Float CurrentPoInteger = _Center;
            while (TryPoInteger(CurrentPoInteger, Measure) == false)
                CurrentPoInteger = GetNextPoIntegerInEdge(CurrentPoInteger);
            return CurrentPoInteger;
        }

        boolean TryPoInteger(Point2D.Float TrialPoInteger, SizeF Rectangle)
        {
            if (TrialPoInteger.Equals(_SpiralEndSentinel)) return true;
            var TrailRectangle = new Rectangle2D.Float(TrialPoInteger, Rectangle);
            return !_Occupied.Any(It => It.IntersectsWith(TrailRectangle));
        }

        /*
         * This method gives poIntegers that crawls along an edge of the spiral, described below.
         */

        Point2D.Float GetNextPoIntegerInEdge(Point2D.Float Current)
        {
            do
            {
                if (Current.Equals(_CurrentCorner))
                {
                    _CurrentCorner = GetSpiralNext(_CurrentCorner);
                    if (_CurrentCorner.Equals(_SpiralEndSentinel)) return _SpiralEndSentinel;
                }
                Current = Current.X == _CurrentCorner.X
                              ? new Point2D.Float(Current.X, _EdgeDirection(Current.Y))
                              : new Point2D.Float(_EdgeDirection(Current.X), Current.Y);
            } while (!_MainArea.Contains(Current));
            return Current;
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

        Point2D.Float GetSpiralNext(Point2D.Float PreviousCorner)
        {
            float X = PreviousCorner.X, Y = PreviousCorner.Y;
            boolean EdgeSizeEven = (_CurrentEdgeSize & 1) == 0;

            if (_SleepingEdge)
            {
                X = EdgeSizeEven ? PreviousCorner.X - _CurrentEdgeSize : PreviousCorner.X + _CurrentEdgeSize;
                _SleepingEdge = false;
                /* Next edge will be standing. Sleeping = Parallal to X-Axis; Standing = Parallal to Y-Axis */
            }
            else
            {
                Y = EdgeSizeEven ? PreviousCorner.Y - _CurrentEdgeSize : PreviousCorner.Y + _CurrentEdgeSize;
                _CurrentEdgeSize++;
                _SleepingEdge = true;
            }

            _EdgeDirection = EdgeSizeEven ? _Decrement : _Increment;

            /* If the spiral widens to a poInteger where its arms are longer than the Height & Width,
             * it's time to end the spiral and give up placing the word. There is no 'poInteger'
             * (no pun Integerended) in going for wider spirals, as you are out of bounds now.
             * Our spiral is an Archimedean Right spiral, made up of Line segments @
             * right-angles to each other.
             */
            return _CurrentEdgeSize > _MaxEdgeSize ? _SpiralEndSentinel : new Point2D.Float(X, Y);
        }

        // Range Mapping
        private float CalculateFontSize(Integer Weight)
        {
            // Strange case where all tags have equal weights
            if (_WeightSpan == 0) return (MinimumFontSize + MaximumFontSize)/2f;
            // Convert the Weight Integero a 0-1 range (float)
            float WeightScaled = (Weight - _LowestWeight)/(float) _WeightSpan;
            // Convert the 0-1 range Integero a value in the Font range.
            return MinimumFontSize + (WeightScaled*_FontHeightSpan);
        }

        /// <summary>
        ///   Uses the list of occupied areas to
        ///   crop the Bitmap and translates the list of occupied areas
        ///   keeping them consistant with the new cropped bitmap
        /// </summary>
        /// <param name = "CloudToCrop">The bitmap of the cloud to crop</param>
        /// <returns>The cropped version of the bitmap</returns>
        private Bitmap CropAndTranslate(Bitmap CloudToCrop)
        {
            float NewTop = _Occupied.Select(It => It.Top).Min() - Margin;
            float NewLeft = _Occupied.Select(It => It.Left).Min() - Margin;

            float Bottom = _Occupied.Select(It => It.Bottom).Max() + Margin;
            float Right = _Occupied.Select(It => It.Right).Max() + Margin;

            if (NewTop < 0) NewTop = 0;
            if (NewLeft < 0) NewLeft = 0;

            if (Bottom > _Height) Bottom = _Height;
            if (Right > _Width) Right = _Width;

            var PopulatedArea = new Rectangle2D.Float(NewLeft, NewTop, Right - NewLeft, Bottom - NewTop);
            _Occupied = _Occupied.Select(It => new Rectangle2D.Float(It.X - NewLeft, It.Y - NewTop, It.Width, It.Height)).ToArrayList();
            return CloudToCrop.Clone(PopulatedArea, CloudToCrop.PixelFormat);
        }
    }
}