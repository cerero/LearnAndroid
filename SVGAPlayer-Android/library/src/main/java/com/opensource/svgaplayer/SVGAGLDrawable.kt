package com.opensource.svgaplayer

import android.widget.ImageView
import com.kugou.graphic.ICanvasGL
import com.kugou.graphic.glview.GLView
import com.opensource.svgaplayer.drawer.SVGACanvasDrawer

class SVGAGLDrawable (val videoItem: SVGAVideoEntity, val dynamicItem: SVGADynamicEntity, val glView : GLView) {
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

    fun draw(canvas: ICanvasGL?) {
        if (cleared) {
            return
        }

        canvasWrapper.glCanvas = canvas
        canvasWrapper.let {
            drawer.drawFrame(it, currentFrame, scaleType)
        }
    }

    private fun invalidateSelf() {
        glView.requestRender()
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