package com.hamz.hfreechart.library.renderer

import android.graphics.*
import com.acorn.xfreechart.library.dataset.XFreeLineDataSet
import com.acorn.xfreechart.library.extendfun.safeGetEntryForIndex
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineScatterCandleRadarDataSet
import com.github.mikephil.charting.renderer.LineRadarRenderer
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import java.lang.ref.WeakReference

class XFreeLineChartRenderer(
    private val mChart: LineDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineRadarRenderer(animator, viewPortHandler) {
    private val TAG = "XFreeLineChartRenderer"

    /**
     * paint for the inner circle of the value indicators
     */
    private val mCirclePaintInner: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    /**
     * Bitmap object used for drawing the paths (otherwise they are too long if
     * rendered directly on the canvas)
     */
    private var mDrawBitmap: WeakReference<Bitmap>? = null

    /**
     * on this canvas, the paths are rendered, it is initialized with the
     * pathBitmap
     */
    private var mBitmapCanvas: Canvas? = null

    /**
     * the bitmap configuration to be used
     */
    private var mBitmapConfig = Bitmap.Config.ARGB_8888

    protected var cubicPath = Path()
    protected var cubicFillPath = Path()

    private var mLineBuffer = FloatArray(4)

    override fun initBuffers() {
    }

    override fun drawData(c: Canvas?) {
        c ?: return
        val width = mViewPortHandler.chartWidth.toInt()
        val height = mViewPortHandler.chartHeight.toInt()

        var drawBitmap = if (mDrawBitmap == null) null else mDrawBitmap!!.get()

        if (drawBitmap == null || drawBitmap.width != width || drawBitmap.height != height) {
            if (width > 0 && height > 0) {
                drawBitmap = Bitmap.createBitmap(width, height, mBitmapConfig)
                mDrawBitmap = WeakReference(drawBitmap)
                mBitmapCanvas = Canvas(drawBitmap)
            } else return
        }
        drawBitmap ?: return
        drawBitmap.eraseColor(Color.TRANSPARENT)

        for (set in mChart.lineData.dataSets) {
            if (!set.isVisible) continue
            drawDataSet(c, set)
        }
        c.drawBitmap(drawBitmap, 0f, 0f, mRenderPaint)
    }

    private fun drawDataSet(c: Canvas, dataSet: ILineDataSet) {
        val entryCount = dataSet.entryCount
        if (entryCount < 1) return
        mRenderPaint.strokeWidth = dataSet.lineWidth
        mRenderPaint.pathEffect = dataSet.dashPathEffect

        when (dataSet.mode) {
            LineDataSet.Mode.LINEAR, LineDataSet.Mode.STEPPED -> drawLinear(c, dataSet)
            LineDataSet.Mode.CUBIC_BEZIER -> drawCubicBezier(dataSet)
//            LineDataSet.Mode.HORIZONTAL_BEZIER -> drawHorizontalBezier(dataSet)
            else -> drawLinear(c, dataSet)
        }


        mRenderPaint.pathEffect = null
    }

    private fun drawLinear(c: Canvas, dataSet: ILineDataSet){
        mRenderPaint.pathEffect = null

        val trans = mChart.getTransformer(dataSet.axisDependency)
        val phaseY = mAnimator.phaseY
        mRenderPaint.style = Paint.Style.STROKE
        val pointsPerEntryPair = 2

        // only one color per dataset
        if (mLineBuffer.size < Math.max(dataSet.entryCount * pointsPerEntryPair, pointsPerEntryPair) * 2) {
            mLineBuffer =
                FloatArray(Math.max(dataSet.entryCount * pointsPerEntryPair, pointsPerEntryPair) * 4)
        }
        var j = 0
        for (i in 0 until dataSet.entryCount) {
            val e1 = dataSet.safeGetEntryForIndex(if (i == 0) 0 else i - 1)
            val e2 = dataSet.safeGetEntryForIndex(i)
            if (e1 == null || e2 == null) continue

            mLineBuffer[j++] = e1.x
            mLineBuffer[j++] = e1.y * phaseY

            mLineBuffer[j++] = e2.x
            mLineBuffer[j++] = e2.y * phaseY
        }
        if (j > 0) { //需要绘制线条
            trans.pointValuesToPixel(mLineBuffer)

            val size = Math.max((dataSet.entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 2

            mRenderPaint.color = dataSet.color

            c.drawLines(mLineBuffer, 0, size, mRenderPaint)
        }
    }

    private fun drawCubicBezier(dataSet: ILineDataSet){

        val phaseY = mAnimator.phaseY

        val trans = mChart.getTransformer(dataSet.axisDependency)

        mXBounds.set(mChart, dataSet)

        val intensity = dataSet.cubicIntensity

        cubicPath.reset()

        if (mXBounds.range >= 1) {
            var prevDx = 0f
            var prevDy = 0f
            var curDx = 0f
            var curDy = 0f

            // Take an extra point from the left, and an extra from the right.
            // That's because we need 4 points for a cubic bezier (cubic=4), otherwise we get lines moving and doing weird stuff on the edges of the chart.
            // So in the starting `prev` and `cur`, go -2, -1
            // And in the `lastIndex`, add +1
            val firstIndex: Int = mXBounds.min + 1
            val lastIndex: Int = mXBounds.min + mXBounds.range
            var prevPrev: Entry?
            var prev = dataSet.getEntryForIndex(Math.max(firstIndex - 2, 0))
            var cur = dataSet.getEntryForIndex(Math.max(firstIndex - 1, 0))
            var next = cur
            var nextIndex = -1
            if (cur == null) return

            // let the spline start
            cubicPath.moveTo(cur.x, cur.y * phaseY)
            for (j in 0 until dataSet.entryCount) {

                prevPrev = prev
                prev = cur
                cur = if (nextIndex == j) next else dataSet.getEntryForIndex(j)
                nextIndex = if (j + 1 < dataSet.entryCount) j + 1 else j
                next = dataSet.getEntryForIndex(nextIndex)
                prevDx = (cur!!.x - prevPrev!!.x) * intensity
                prevDy = (cur!!.y - prevPrev!!.y) * intensity
                curDx = (next.x - prev!!.x) * intensity
                curDy = (next.y - prev!!.y) * intensity

                cubicPath.cubicTo(
                    prev!!.x + prevDx, (prev!!.y + prevDy) * phaseY,
                    cur!!.x - curDx, (cur!!.y - curDy) * phaseY,
                    cur!!.x, cur!!.y * phaseY
                )


            }
        }

        // if filled is enabled, close the path

        // if filled is enabled, close the path
        if (dataSet.isDrawFilledEnabled) {
            cubicFillPath.reset()
            cubicFillPath.addPath(cubicPath)
            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds)
        }

        mRenderPaint.color = dataSet.color

        mRenderPaint.style = Paint.Style.STROKE

        trans.pathValueToPixel(cubicPath)

        mBitmapCanvas!!.drawPath(cubicPath, mRenderPaint)

        mRenderPaint.pathEffect = null

    }

    protected fun drawCubicFill(
        c: Canvas?,
        dataSet: ILineDataSet,
        spline: Path,
        trans: Transformer,
        bounds: XBounds
    ) {
        val fillMin = dataSet.fillFormatter.getFillLinePosition(dataSet, mChart)
        spline.lineTo(dataSet.getEntryForIndex(bounds.min + bounds.range).x, fillMin)
        spline.lineTo(dataSet.getEntryForIndex(bounds.min).x, fillMin)
        spline.close()
        trans.pathValueToPixel(spline)
        val drawable = dataSet.fillDrawable
        if (drawable != null) {
            drawFilledPath(c, spline, drawable)
        } else {
            drawFilledPath(c, spline, dataSet.fillColor, dataSet.fillAlpha)
        }
    }


    override fun drawValues(c: Canvas?) {
    }

    override fun drawExtras(c: Canvas?) {
        drawCircles(c)
    }

    /**
     * cache for the circle bitmaps of all datasets
     */
    private val mImageCaches = HashMap<IDataSet<*>, DataSetImageCache>()

    /**
     * buffer for drawing the circles
     */
    private val mCirclesBuffer = FloatArray(2)

    private fun drawCircles(c: Canvas?) {
        c ?: return
        mRenderPaint.style = Paint.Style.FILL

        val phaseY = mAnimator.phaseY

        mCirclesBuffer[0] = 0f
        mCirclesBuffer[1] = 0f
        val dataSets = mChart.lineData.dataSets
        for (dataSet in dataSets) {
            if (!dataSet.isVisible || !dataSet.isDrawCirclesEnabled || dataSet.entryCount == 0) continue
            mCirclePaintInner.color = dataSet.circleHoleColor

            val trans = mChart.getTransformer(dataSet.axisDependency)

            val circleRadius = dataSet.circleRadius
            val circleHoleRadius = dataSet.circleHoleRadius
            val drawCircleHole =
                dataSet.isDrawCircleHoleEnabled && circleHoleRadius < circleRadius && circleHoleRadius > 0f
            val drawTransparentCircleHole =
                drawCircleHole && dataSet.circleHoleColor == ColorTemplate.COLOR_NONE

            var imageCache: DataSetImageCache

            if (mImageCaches.containsKey(dataSet)) {
                imageCache = mImageCaches.get(dataSet)!!
            } else {
                imageCache = DataSetImageCache(mRenderPaint, mCirclePaintInner)
                mImageCaches.put(dataSet, imageCache)
            }

            val changeRequired = imageCache.init(dataSet)

            // only fill the cache with new bitmaps if a change is required
            if (changeRequired) {
                imageCache.fill(dataSet, drawCircleHole, drawTransparentCircleHole)
            }

            val pointsLimitAmount = (dataSet as? XFreeLineDataSet<*>)?.mPointVisibleThreshold ?: -1
            //是否限制显示点的数量
            val isLimitPointAmount = pointsLimitAmount > 0
            if (isLimitPointAmount) {
                drawCirclesToCanvasByThreshold(
                    pointsLimitAmount,
                    c,
                    dataSet,
                    trans,
                    phaseY,
                    imageCache,
                    circleRadius
                )
            } else {
                drawCirclesToCanvas(c, dataSet, trans, phaseY, imageCache, circleRadius)
            }
        }
    }

    /**
     * Draw circles to canvas
     * 直接绘制所有点
     * @param c
     * @param dataSet
     * @param trans
     * @param phaseY
     * @param imageCache
     * @param circleRadius
     */
    private fun drawCirclesToCanvas(
        c: Canvas,
        dataSet: ILineDataSet,
        trans: Transformer,
        phaseY: Float,
        imageCache: DataSetImageCache,
        circleRadius: Float
    ) {
        val entryCount = dataSet.entryCount
        //计算屏幕中需要绘制的点
        for (j in 0 until entryCount) {
            val e = dataSet.safeGetEntryForIndex(j) ?: continue
            mCirclesBuffer[0] = e.x
            mCirclesBuffer[1] = e.y * phaseY
            trans.pointValuesToPixel(mCirclesBuffer)
            if (!mViewPortHandler.isInBoundsRight(mCirclesBuffer[0])) {
                continue
            }

            if (!mViewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) ||
                !mViewPortHandler.isInBoundsY(mCirclesBuffer[1])
            ) {
                continue
            }
            val circleBitmap = imageCache.getBitmap(j)

            if (circleBitmap != null) {
                c.drawBitmap(
                    circleBitmap,
                    mCirclesBuffer[0] - circleRadius,
                    mCirclesBuffer[1] - circleRadius,
                    null
                )
            }
        }
    }

    /**
     * Draw circles to canvas by threshold
     * 根据pointsLimitAmount决定是否绘制点
     *
     * @param pointsLimitAmount
     * @param c
     * @param dataSet
     * @param trans
     * @param phaseY
     * @param imageCache
     * @param circleRadius
     */
    private fun drawCirclesToCanvasByThreshold(
        pointsLimitAmount: Int,
        c: Canvas,
        dataSet: ILineDataSet,
        trans: Transformer,
        phaseY: Float,
        imageCache: DataSetImageCache,
        circleRadius: Float
    ) {
        val entryCount = dataSet.entryCount

        //计算当前需要绘制的点的数量
        var needDrawAmount = 0
        for (j in 0 until entryCount) {
            val e = dataSet.safeGetEntryForIndex(j) ?: continue
            mCirclesBuffer[0] = e.x
            mCirclesBuffer[1] = e.y * phaseY
            trans.pointValuesToPixel(mCirclesBuffer)
            if (!mViewPortHandler.isInBoundsRight(mCirclesBuffer[0])) {
                continue
            }

            if (!mViewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) ||
                !mViewPortHandler.isInBoundsY(mCirclesBuffer[1])
            ) {
                continue
            }
            needDrawAmount++
        }
//        Log.i(TAG, "drawCirclesToCanvasByThreshold: $needDrawAmount,$pointsLimitAmount")
        if (needDrawAmount > pointsLimitAmount) return
        drawCirclesToCanvas(c, dataSet, trans, phaseY, imageCache, circleRadius)
    }

    override fun drawHighlighted(c: Canvas, indices: Array<out Highlight>) {
        val lineData = mChart.lineData

        for (high in indices) {
            val set = lineData.getDataSetByIndex(high.dataSetIndex)
            if (set == null || !set.isHighlightEnabled) continue
//            val e = set.getEntryForXValue(high.x, high.y) ?: continue
//            if (!isInBoundsX(e, set)) continue
            val pix = mChart.getTransformer(set.axisDependency).getPixelForValues(
                high.x, high.y * mAnimator.phaseY
            )
            high.setDraw(pix.x.toFloat(), pix.y.toFloat())

            // draw the lines
            drawHighlightLines(c, pix.x.toFloat(), pix.y.toFloat(), set)
        }
    }

    /**
     * path that is used for drawing highlight-lines (drawLines(...) cannot be used because of dashes)
     */
    private val mHighlightLinePath = Path()

    /**
     * Draws vertical & horizontal highlight-lines if enabled.
     *
     * @param c
     * @param x x-position of the highlight line intersection
     * @param y y-position of the highlight line intersection
     * @param set the currently drawn dataset
     */
    override fun drawHighlightLines(
        c: Canvas,
        x: Float,
        y: Float,
        set: ILineScatterCandleRadarDataSet<*>
    ) {

        // set color and stroke-width
        mHighlightPaint.color = set.highLightColor
        mHighlightPaint.strokeWidth = set.highlightLineWidth

        // draw highlighted lines (if enabled)
        mHighlightPaint.pathEffect = set.dashPathEffectHighlight

        // draw vertical highlight lines
        if (set.isVerticalHighlightIndicatorEnabled) {

            // create vertical path
            mHighlightLinePath.reset()
            mHighlightLinePath.moveTo(x, mViewPortHandler.contentTop())
            mHighlightLinePath.lineTo(x, mViewPortHandler.contentBottom())
            c.drawPath(mHighlightLinePath, mHighlightPaint)
        }

        // draw horizontal highlight lines
        if (set.isHorizontalHighlightIndicatorEnabled) {

            // create horizontal path
            mHighlightLinePath.reset()
            mHighlightLinePath.moveTo(mViewPortHandler.contentLeft(), y)
            mHighlightLinePath.lineTo(mViewPortHandler.contentRight(), y)
            c.drawPath(mHighlightLinePath, mHighlightPaint)
        }
    }


    fun releaseBitmap() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas?.setBitmap(null)
            mBitmapCanvas = null
        }
        if (mDrawBitmap != null) {
            val drawBitmap = mDrawBitmap?.get()
            drawBitmap?.recycle()
            mDrawBitmap?.clear()
            mDrawBitmap = null
        }
    }
}
