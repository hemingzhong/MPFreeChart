package com.github.mikephil.charting.selectarea

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

/**
 * Created by acorn on 2022/9/5.
 */
class SelectAreaHelper(
    callback: Drawable.Callback,
    minTouchSlop: Int,
    isSelectAllYAxis: Boolean,
    private val mChart: LineDataProvider
) :
    View.OnTouchListener {
    var isSelectAreaMode = false
        private set
    private val mDrawable = SelectAreaDrawable(minTouchSlop, isSelectAllYAxis).apply {
        this.callback = callback
    }
    private var callback: ((List<SelectedSet>) -> Unit)? = null

    fun enterSelectAreaMode(callback: ((List<SelectedSet>) -> Unit)) {
        this.isSelectAreaMode = true
        this.callback = callback
    }

    fun quitSelectAreaMode() {
        isSelectAreaMode = false
        callback = null
    }

    fun draw(canvas: Canvas) {
        if (!isSelectAreaMode) return
        mDrawable.draw(canvas)
    }

    fun calculateBounds(w: Int, h: Int) {
        mDrawable.setBounds(0, 0, w, h)
    }

    private fun getSelectedEntrys(set: ILineDataSet): List<Entry>? {
        if (!isSelectAreaMode) return null
        var rect = mDrawable.mDrawRect ?: return null
        if (rect.left > rect.right) { //contains方法不考虑left>right的情况,所以得反转一下
            rect = RectF(rect.right, rect.top, rect.left, rect.bottom)
        }
        val trans = mChart.getTransformer(set.axisDependency)
        val size = set.entryCount
        val buf = FloatArray(2)
        val list = mutableListOf<Entry>()
        for (i in 0 until size) {
            val entry = set.getEntryForIndex(i)
            buf[0] = entry.x
            buf[1] = entry.y
            trans.pointValuesToPixel(buf)
            val isInArea = rect.contains(buf[0], buf[1])
//            logI("entry:${entry.x},${entry.y},position:${buf[0]},${buf[1]}.$isInArea")
            if (isInArea) {
                list.add(entry)
            }
        }
        return list
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startDragging(event)
            MotionEvent.ACTION_MOVE -> onDragging(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> stopDragging(event)
        }
        return true
    }

    private fun startDragging(event: MotionEvent) {
        mDrawable.setStartPoint(event.x, event.y)
    }

    private fun onDragging(event: MotionEvent) {
        mDrawable.setMovedPoint(event.x, event.y)
    }

    private fun stopDragging(event: MotionEvent) {
        val list = mutableListOf<SelectedSet>()
        mChart.lineData.dataSets?.forEach { set ->
            getSelectedEntrys(set)?.let { entrys ->
                list.add(SelectedSet(set, entrys))
            }
        }
        callback?.invoke(list)
        callback = null
        isSelectAreaMode = false
        mDrawable.reset()
        mDrawable.invalidateSelf()
    }

    fun verifyDrawable(who: Drawable): Boolean {
        return who == mDrawable
    }
}