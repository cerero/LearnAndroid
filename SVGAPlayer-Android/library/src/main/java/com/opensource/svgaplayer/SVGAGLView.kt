package com.opensource.svgaplayer

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.kugou.graphic.ICanvasGL
import com.kugou.graphic.glview.GLView
import com.opensource.svgaplayer.drawer.SVGACanvasDrawer
import com.opensource.svgaplayer.utils.SVGARange

class SVGAGLView : GLView {

    enum class FillMode {
        Backward,
        Forward,
    }

    var isAnimating = false
        private set

    var loops = 0

    var clearsAfterStop = true

    private var drawable : SVGAGLDrawable? = null

    var fillMode: SVGAGLView.FillMode = SVGAGLView.FillMode.Forward

    var callback: SVGACallback? = null

    private var animator: ValueAnimator? = null

    private val drawer: SVGACanvasDrawer? = null

    constructor(context : Context) : super(context)

    constructor(context : Context, attrs : AttributeSet) : super(context, attrs)

//    constructor(context : Context, attrs : AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr)

    override fun init() {
        super.init();
    }

    override fun onGLDraw(canvas: ICanvasGL) {
        drawable?.draw(canvas)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator?.removeAllListeners()
        animator?.removeAllUpdateListeners()
    }

    fun startAnimation() {
        startAnimation(null, false)
    }

    fun startAnimation(range: SVGARange?, reverse: Boolean = false) {
        stopAnimation(false)
        val drawable = drawable as? SVGAGLDrawable ?: return
        drawable.cleared = false
        drawable.scaleType = ImageView.ScaleType.CENTER_CROP
        drawable.videoItem.let {
            var durationScale = 1.0
            val startFrame = Math.max(0, range?.location ?: 0)
            val endFrame = Math.min(it.frames ?: 1 - 1, ((range?.location ?: 0) + (range?.length ?: Int.MAX_VALUE) - 1))
            val animator = ValueAnimator.ofInt(startFrame, endFrame)
            try {
                val animatorClass = Class.forName("android.animation.ValueAnimator")
                animatorClass?.let {
                    it.getDeclaredField("sDurationScale")?.let {
                        it.isAccessible = true
                        it.getFloat(animatorClass).let {
                            durationScale = it.toDouble()
                        }
                        if (durationScale == 0.0) {
                            it.setFloat(animatorClass, 1.0f)
                            durationScale = 1.0
                            Log.e("SVGAPlayer", "The animation duration scale has been reset to 1.0x, because you closed it on developer options.")
                        }
                    }
                }
            } catch (e: Exception) {}
            animator.interpolator = LinearInterpolator()
            var fps = it.FPS ?: 20
            animator.duration = ((endFrame - startFrame + 1) * (1000 / fps) / durationScale).toLong()
            animator.repeatCount = if (loops <= 0) 99999 else loops - 1
            animator.addUpdateListener {
                var currentFrame = animator.animatedValue as Int
                var totalFrame = drawable?.videoItem.frames ?: 20

                drawable.currentFrame = currentFrame

                callback?.onStep(currentFrame, (currentFrame.toDouble() / totalFrame.toDouble()))
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    callback?.onRepeat()
                }
                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                    stopAnimation()
                    if (!clearsAfterStop) {
                        if (fillMode == SVGAImageView.FillMode.Backward) {
                            drawable.currentFrame = startFrame
                        }
                        else if (fillMode == SVGAImageView.FillMode.Forward) {
                            drawable.currentFrame = endFrame
                        }
                    }
                    callback?.onFinished()
                }
                override fun onAnimationCancel(animation: Animator?) {
                    isAnimating = false
                }
                override fun onAnimationStart(animation: Animator?) {
                    isAnimating = true
                }
            })
            if (reverse) {
                animator.reverse()
            }
            else {
                animator.start()
            }
            this.animator = animator
        }

    }

    fun pauseAnimation() {
        stopAnimation(false)
        callback?.onPause()
    }

    fun stopAnimation() {
        stopAnimation(clearsAfterStop)
    }

    fun stopAnimation(clear: Boolean) {
        animator?.cancel()
        animator?.removeAllListeners()
        animator?.removeAllUpdateListeners()
        (drawable as? SVGADrawable)?.let {
            it.cleared = clear
        }
    }

    fun setVideoItem(videoItem: SVGAVideoEntity?) {
        setVideoItem(videoItem, SVGADynamicEntity())
    }

    fun setVideoItem(videoItem: SVGAVideoEntity?, dynamicItem: SVGADynamicEntity?) {
        if (videoItem == null) {
            drawable = null
            return
        }
        drawable = SVGAGLDrawable(videoItem, dynamicItem ?: SVGADynamicEntity(), this)
        drawable?.cleared = clearsAfterStop
    }

    fun stepToFrame(frame: Int, andPlay: Boolean) {
        pauseAnimation()
        val drawable = drawable as? SVGADrawable ?: return
        drawable.currentFrame = frame
        if (andPlay) {
            startAnimation()
            animator?.let {
                it.currentPlayTime = (Math.max(0.0f, Math.min(1.0f, (frame.toFloat() / drawable.videoItem.frames.toFloat()))) * it.duration).toLong()
            }
        }
    }

    fun stepToPercentage(percentage: Double, andPlay: Boolean) {
        val drawable = drawable as? SVGADrawable ?: return
        var frame = (drawable.videoItem.frames * percentage).toInt()
        if (frame >= drawable.videoItem.frames && frame > 0) {
            frame = drawable.videoItem.frames - 1
        }
        stepToFrame(frame, andPlay)
    }
}