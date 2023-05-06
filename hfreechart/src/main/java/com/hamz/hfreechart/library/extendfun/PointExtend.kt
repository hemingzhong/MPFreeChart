package com.acorn.xfreechart.library.extendfun

import kotlin.math.sqrt

fun calculateDistanceOf2Points(x1: Double, y1: Double, x2: Double, y2: Double): Float {
    val aSideLen = y2 - y1
    val bSideLen = x2 - x1
    //给定直角三角形中两条直角边的边长,计算最长边的长度
    return Math.hypot(aSideLen, bSideLen).toFloat()
}

/**
 * 给定直角三角形中两条直角边的边长,计算最长边的长度
 *
 */
private fun calculateSideLenInRightTriangle(a: Float, b: Float): Float {
    return sqrt((a * a + b * b).toDouble()).toFloat()
}