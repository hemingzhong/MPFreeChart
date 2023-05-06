package com.github.mikephil.charting.selectarea

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

/**
 * Created by acorn on 2022/9/6.
 */
data class SelectedSet(
    val set: ILineDataSet,
    val entrys: List<Entry>
)