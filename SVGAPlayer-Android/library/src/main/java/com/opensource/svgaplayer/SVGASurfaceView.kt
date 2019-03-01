package com.opensource.svgaplayer

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.LinearInterpolator
import com.opensource.svgaplayer.utils.SVGARange
import java.lang.ref.WeakReference
import java.net.URL

/**
 * Created by PonyCui on 2017/3/29.
 */

open class SVGASurfaceView : SurfaceView, Runnable {

    enum class FillMode {
        Backward,
        Forward,
    }

    var isAnimating = false
        private set

    var loops = 0

    var clearsAfterStop = true

    var fillMode: FillMode = FillMode.Forward

    var callback: SVGACallback? = null

    private var animator: ValueAnimator? = null
    private var drawable : SVGASurfaceViewDrawable? = null

    private var renderThread: Thread? = null
    private var surfaceHolder: SurfaceHolder? = null
    @Volatile private var running: Boolean = false

    constructor(context: Context?) : super(context) {
        setSoftwareLayerType()
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setSoftwareLayerType()
        attrs?.let { loadAttrs(it) }
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setSoftwareLayerType()
        attrs?.let { loadAttrs(it) }
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        setSoftwareLayerType()
        attrs?.let { loadAttrs(it) }
        init()
    }

    private fun setSoftwareLayerType() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private fun init() {
        surfaceHolder = getHolder()
    }

    override fun run() {
//        var startTime = System.nanoTime()
        while (running) {
            var surfaceHolder = surfaceHolder as? SurfaceHolder ?: continue
            var drawable = drawable as? SVGASurfaceViewDrawable ?: continue

            if (surfaceHolder.surface.isValid && drawable != null) {
//                var deltaTime = (System.nanoTime() - startTime) / 1000000000.0f
//                Log.d("SVGASurfaceView", "render thread " + Thread.currentThread().name + " is running, deltatime: " + deltaTime)
//                startTime = System.nanoTime()
//                synchronized(drawable) {
                    var canvas = holder.lockCanvas()
                    canvas.drawRGB(0, 0, 0)
                    drawable.draw(canvas)
                    holder.unlockCanvasAndPost(canvas)
//                }
            }
        }

    }

    private fun resume() {
        if (renderThread != null)
            pause()

        running = true
        renderThread = Thread(this)
        renderThread?.start()
    }

    private fun pause() {
        running = false
        while (true) {
            try {
                renderThread?.join()
                renderThread = null
                break
            } catch (e: InterruptedException) {
                // retry
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator?.removeAllListeners()
        animator?.removeAllUpdateListeners()
    }

    private fun loadAttrs(attrs: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SVGAImageView, 0, 0)
        loops = typedArray.getInt(R.styleable.SVGAImageView_loopCount, 0)
        clearsAfterStop = typedArray.getBoolean(R.styleable.SVGAImageView_clearsAfterStop, true)
        val antiAlias = typedArray.getBoolean(R.styleable.SVGAImageView_antiAlias, true)
        val autoPlay = typedArray.getBoolean(R.styleable.SVGAImageView_autoPlay, true)
        typedArray.getString(R.styleable.SVGAImageView_fillMode)?.let {
            if (it == "0") {
                fillMode = FillMode.Backward
            }
            else if (it == "1") {
                fillMode = FillMode.Forward
            }
        }
        typedArray.getString(R.styleable.SVGAImageView_source)?.let {
            val parser = SVGAParser(context)
            Thread {
                val callback: SVGAParser.ParseCompletion = object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        this@SVGASurfaceView.post {
                            videoItem.antiAlias = antiAlias
                            setVideoItem(videoItem)
                            if (autoPlay) {
                                startAnimation()
                            }
                        }
                    }

                    override fun onError() {}
                }
                if(it.startsWith("http://") || it.startsWith("https://")) {
                    parser.parse(URL(it), callback)
                } else {
                    parser.parse(it, callback)
                }
            }.start()
        }
        typedArray.recycle()
    }

    fun startAnimation() {
        startAnimation(null, false)
    }

    fun startAnimation(range: SVGARange?, reverse: Boolean = false) {
        stopAnimation(false)
        val drawable = drawable as? SVGASurfaceViewDrawable ?: return
        drawable.cleared = false
//        drawable.scaleType = scaleType
        drawable.videoItem.let {
            var durationScale = 1.0
            val startFrame = Math.max(0, range?.location ?: 0)
            val endFrame = Math.min(it.frames - 1, ((range?.location ?: 0) + (range?.length ?: Int.MAX_VALUE) - 1))
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
            animator.duration = ((endFrame - startFrame + 1) * (1000 / it.FPS) / durationScale).toLong()
            animator.repeatCount = if (loops <= 0) 99999 else loops - 1
            animator.addUpdateListener {
//                synchronized(drawable) {
                    drawable.currentFrame = animator.animatedValue as Int
                    callback?.onStep(drawable.currentFrame, ((drawable.currentFrame + 1).toDouble() / drawable.videoItem.frames.toDouble()))
//                }
            }
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    callback?.onRepeat()
                }
                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                    stopAnimation()
                    if (!clearsAfterStop) {
//                        synchronized(drawable) {
                            if (fillMode == FillMode.Backward) {
                                drawable.currentFrame = startFrame
                            }
                            else if (fillMode == FillMode.Forward) {
                                drawable.currentFrame = endFrame
                            }
//                        }

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

        resume()
    }

    fun pauseAnimation() {
        stopAnimation(false)
        callback?.onPause()
    }

    fun stopAnimation() {
        stopAnimation(clear = clearsAfterStop)
    }

    fun stopAnimation(clear: Boolean) {
        pause()
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
        drawable = SVGASurfaceViewDrawable(videoItem, dynamicItem ?: SVGADynamicEntity(), this)
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
//        synchronized(drawable) {
            var frame = (drawable.videoItem.frames * percentage).toInt()
            if (frame >= drawable.videoItem.frames && frame > 0) {
                frame = drawable.videoItem.frames - 1
            }
            stepToFrame(frame, andPlay)
//        }
    }

}
