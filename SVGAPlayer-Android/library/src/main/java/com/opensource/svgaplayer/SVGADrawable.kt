package com.opensource.svgaplayer

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.opensource.svgaplayer.drawer.SVGACanvasDrawer

class SVGADrawable(val videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity): Drawable() {

    constructor(videoItem: SVGAVideoEntity): this(videoItem, SVGADynamicEntity())

    var cleared = true
        internal set (value) {
            if (field == value) {
                return
            }
            field = value
            invalidateSelf()
        }

    var currentFrame = 0
        internal set (value) {
            if (field == value) {
                return
            }
            field = value
            invalidateSelf()
        }

    var scaleType: ImageView.ScaleType = ImageView.ScaleType.MATRIX

    private val drawer = SVGACanvasDrawer(videoItem, dynamicItem)
    private var canvasWrapper : CanvasWrapper = CanvasWrapper(null ,null)

    private var startTime: Long = 0
    override fun draw(canvas: Canvas?) {
        var deltaTime = (System.nanoTime() - startTime) / 1000000000.0f

        if (cleared) {
            return
        }
        canvasWrapper.normCavas = canvas
        canvasWrapper?.let {
            drawer.drawFrame(it, currentFrame, scaleType)
        }

        Log.d("SVGASurfaceView", "render thread " + Thread.currentThread().name + " running, deltatime: " + deltaTime)
        startTime = System.nanoTime()
    }

    override fun setAlpha(alpha: Int) { }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {

    }

}