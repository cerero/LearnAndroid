package com.opensource.svgaplayer

import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceView
import android.widget.ImageView
import com.kugou.graphic.ICanvasGL
import com.kugou.graphic.glview.GLView
import com.opensource.svgaplayer.drawer.SVGACanvasDrawer

class SVGASurfaceViewDrawable (val videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity, val surfaceView : SurfaceView) {
    private var isDirty = true
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

    private val lock = java.lang.Object()
    private val drawer = SVGACanvasDrawer(videoItem, dynamicItem)
    private var canvasWrapper : CanvasWrapper = CanvasWrapper(null ,null)
    private var startTime: Long = 0

    fun draw(canvas: Canvas?) {
        var deltaTime = (System.nanoTime() - startTime) / 1000000000.0f
        synchronized(lock) {
            if (cleared) {
                canvas?.drawRGB(0, 0, 0)
                return
            }

            if (!isDirty) {
                lock.wait()
            }

            canvas?.drawRGB(0, 0, 0)
            canvasWrapper.normCavas = canvas
            canvasWrapper.let {
                drawer.drawFrame(it, currentFrame, scaleType)
            }

            isDirty = false
        }
        Log.d("SVGAImageView", "render thread " + Thread.currentThread().name + " running, deltatime: " + deltaTime)
        startTime = System.nanoTime()
    }

    private fun invalidateSelf() {
        synchronized(lock) {
            isDirty = true
            lock.notifyAll()
        }
    }

//    override fun setAlpha(alpha: Int) { }
//
//    override fun getOpacity(): Int {
//        return PixelFormat.TRANSPARENT
//    }
//
//    override fun setColorFilter(colorFilter: ColorFilter?) {
//
//    }
}