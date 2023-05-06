package com.hamz.hfreechart.library

import android.content.Context
import android.util.AttributeSet
import android.view.ViewConfiguration
import com.hamz.hfreechart.library.renderer.XFreeLineChartRenderer
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.highlight.IHighlighter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.selectarea.SelectAreaHelper;

/**
 * x轴不必递增的LineChart
 */
class XFreeLineChart : BarLineChartBase<LineData>, LineDataProvider {

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    override fun init() {
        super.init()
        mRenderer = XFreeLineChartRenderer(this, mAnimator, mViewPortHandler)
        mSelectAreaHelper =
            SelectAreaHelper(this, ViewConfiguration.get(context).scaledTouchSlop, false, this)
    }

    override fun getLineData(): LineData {
        return mData
    }

    fun setHighlighter(highlighter: IHighlighter) {
        this.mHighlighter = highlighter
    }

    override fun onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer is XFreeLineChartRenderer) {
            (mRenderer as? XFreeLineChartRenderer)?.releaseBitmap()
        }
        super.onDetachedFromWindow()
    }
}