package com.github.mikephil.charting.selectarea

import android.graphics.*
import android.graphics.drawable.Drawable
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs

/**
 * @param isSelectAllYAxis Whether the selected area height covers the full screen
 * Created by acorn on 2022/9/5.
 */
class SelectAreaDrawable(private val minTouchSlop: Int, private val isSelectAllYAxis: Boolean) :
    Drawable() {
    var mDrawRect: RectF? = null
        private set
    private var startX = 0f
    private var startY = 0f
    private var h = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#203D7EFF")
        strokeWidth = Utils.convertDpToPixel(2f)
    }
    private var isDragMode = false

    override fun draw(canvas: Canvas) {
        val rect = mDrawRect ?: return
        canvas.drawRect(rect, paint)
    }

    fun setStartPoint(x: Float, y: Float) {
        reset()
        startX = x
        startY = if (isSelectAllYAxis) 0f else y
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        h = bounds.height().toFloat()
    }

    fun setMovedPoint(x: Float, y: Float) {
        if (isDragMode || abs(x - startX) > minTouchSlop) {
            isDragMode = true
            val bottom = if (isSelectAllYAxis) h else y
            mDrawRect = RectF(startX, startY, x, bottom)
        }
        invalidateSelf()
    }

    fun reset() {
        isDragMode = false
        mDrawRect = null
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}