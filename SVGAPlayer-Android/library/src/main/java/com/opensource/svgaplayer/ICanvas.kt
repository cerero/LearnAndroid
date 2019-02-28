package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path

interface ICanvas {
    val width: Int
    val height: Int

    fun save(): Int
    fun clipPath(path : Path):Boolean
    fun clipRect(left: Int, top: Int, right: Int, bottom:Int)
    fun drawBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint)
    fun drawPath(path: Path, paint: Paint)
    fun concat(matrix: Matrix)
    fun restore()
}