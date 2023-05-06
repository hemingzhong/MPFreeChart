package com.acorn.xfreechart.library.highlight

import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.DataSet.Rounding
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.IHighlighter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointD

open class XFreeHighlighter<T : LineDataProvider>(private val mChart: T) : IHighlighter {
    /**
     * buffer for storing previously highlighted values
     */
    protected var mHighlightBuffer: MutableList<Highlight> = ArrayList()

    override fun getHighlight(x: Float, y: Float): Highlight? {
        val pos = getValsForTouch(x, y)
        val xVal = pos.x.toFloat()
        val yVal = pos.y.toFloat()
//        logI("xFree getHighlight:" + pos.x + "," + pos.y)
        MPPointD.recycleInstance(pos)
        return getHighlightFromPoint(xVal, yVal, x, y)
    }

    /**
     * Returns a recyclable MPPointD instance.
     * Returns the corresponding xPos for a given touch-position in pixels.
     *
     * @param x positon of touch point(px)
     * @param y positon of touch point(px)
     * @return
     */
    protected fun getValsForTouch(x: Float, y: Float): MPPointD {
        // take any transformer to determine the x-axis value
        return mChart.getTransformer(YAxis.AxisDependency.LEFT).getValuesByTouchPoint(x, y)
    }

    /**
     *  Returns the corresponding Highlight for a given xVal and yVal
     *
     * @param xVal x value of touch point
     * @param yVal y value of touch point
     * @param x positon of touch point(px)
     * @param y positon of touch point(px)
     * @return
     */
    protected fun getHighlightFromPoint(xVal: Float, yVal: Float, x: Float, y: Float): Highlight? {
        val closestValues = getHighlightsAtPoint(xVal, yVal)
        if (closestValues.isEmpty()) return null
        val leftAxisMinDist: Float =
            getMinimumDistance(closestValues, y, AxisDependency.LEFT)
        val rightAxisMinDist: Float =
            getMinimumDistance(closestValues, y, AxisDependency.RIGHT)

        val axis =
            if (leftAxisMinDist < rightAxisMinDist) AxisDependency.LEFT else AxisDependency.RIGHT

        return getClosestHighlightByPixel(closestValues, x, y, axis, mChart.maxHighlightDistance)
    }

    /**
     * Returns a list of Highlight objects representing the entries closest to the given xVal and yVal.
     *
     * @param xVal x value of touch point
     * @param yVal y value of touch point
     * @return
     */
    protected fun getHighlightsAtPoint(xVal: Float, yVal: Float): List<Highlight> {
        mHighlightBuffer.clear()
        val data = mChart.data ?: return mHighlightBuffer
        val dataSetCount = data.dataSetCount
        for (i in 0 until dataSetCount) {
            val dataSet = data.getDataSetByIndex(i)
            if (!data.isHighlightEnabled) continue
            mHighlightBuffer.addAll(buildHighlights(dataSet, i, xVal, yVal, Rounding.CLOSEST))
        }
        return mHighlightBuffer
    }

    /**
     * Build highlights
     *
     * @param set
     * @param dataSetIndex
     * @param xVal x value of touch point
     * @param yVal y value of touch point
     * @return
     */
    protected fun buildHighlights(
        set: IDataSet<*>,
        dataSetIndex: Int,
        xVal: Float,
        yVal: Float,
        rounding: Rounding
    ): List<Highlight> {
        val highlights = mutableListOf<Highlight>()
        //get all entries at xVal.
        //in general,this is empty
        var entries = set.getEntriesForXValue(xVal)
        if (entries.isEmpty()) {
            // Try to find closest x-value and take all entries for that x-value
            val closest = set.getEntryForXValue(xVal, yVal, rounding)
            if (closest != null) {
                entries = set.getEntriesForXValue(closest.x)
            }
        }
        if (entries.isEmpty()) return highlights
        for (e in entries) {
            val pixels = mChart.getTransformer(set.axisDependency).getPixelForValues(e.x, e.y)

            highlights.add(
                Highlight(
                    e.x, e.y, pixels.x.toFloat(), pixels.y.toFloat(),
                    dataSetIndex, set.axisDependency
                )
            )
        }
        return highlights
    }

    /**
     * Returns the Highlight of the DataSet that contains the closest value on the
     * y-axis.
     *
     * @param closestValues        contains two Highlight objects per DataSet closest to the selected x-position (determined by
     * rounding up an down)
     * @param x
     * @param y
     * @param axis                 the closest axis
     * @param minSelectionDistance
     * @return
     */
    open fun getClosestHighlightByPixel(
        closestValues: List<Highlight>, x: Float, y: Float,
        axis: AxisDependency?, minSelectionDistance: Float
    ): Highlight? {
        var closest: Highlight? = null
        var distance = minSelectionDistance
        for (i in closestValues.indices) {
            val high = closestValues[i]
            if (axis == null || high.axis == axis) {
                val cDistance: Float = getDistance(x, y, high.xPx, high.yPx)
                if (cDistance < distance) {
                    closest = high
                    distance = cDistance
                }
            }
        }
//        logI("getClosestHighlightByPixel:$minSelectionDistance,$closestValues")
        return closest
    }

    /**
     * Calculates the distance between the two given points.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    protected open fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()
    }

    /**
     * Returns the minimum distance from a touch value (in pixels) to the
     * closest value (in pixels) that is displayed in the chart.
     *
     * @param closestValues
     * @param pos
     * @param axis
     * @return
     */
    protected open fun getMinimumDistance(
        closestValues: List<Highlight>,
        pos: Float,
        axis: AxisDependency
    ): Float {
        var distance = Float.MAX_VALUE
        for (i in closestValues.indices) {
            val high = closestValues[i]
            if (high.axis == axis) {
                val tempDistance: Float = Math.abs(getHighlightPos(high) - pos)
                if (tempDistance < distance) {
                    distance = tempDistance
                }
            }
        }
        return distance
    }

    protected open fun getHighlightPos(h: Highlight): Float {
        return h.yPx
    }
}