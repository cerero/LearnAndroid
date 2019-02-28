package com.opensource.svgaplayer

import android.graphics.*
import com.kugou.graphic.ICanvasGL
import com.kugou.graphic.matrix.BaseBitmapMatrix

class CanvasWrapper constructor(var normCavas: Canvas?,var glCanvas: ICanvasGL?) : ICanvas {
    private var innerMatrix : ICanvasGL.OrthoBitmapMatrix = ICanvasGL.OrthoBitmapMatrix()

    private var tmpMatValues = FloatArray(9)
    override val width: Int
        get() {
            var w = if (normCavas != null) {
                normCavas?.width ?: 0
            } else if (glCanvas != null) {
                glCanvas?.width ?: 0
            } else {
                0
            }
            return w
        }

    override val height: Int
        get() {
            var h = if (normCavas != null) {
                return normCavas?.height ?: 0
            } else if (glCanvas != null) {
                return glCanvas?.height ?: 0
            } else {
                return 0
            }
            return h
        }


    override fun save(): Int {
        var ret: Int = 0
        if (normCavas != null) {
            ret = normCavas?.save() ?: 0
        } else {
            glCanvas?.save()
        }

        return ret
    }

    override fun clipPath(path : Path):Boolean {
        var ret: Boolean = true
        if (normCavas != null) {
            ret = normCavas?.clipPath(path) ?: false
        } else {
            return false
//            glCanvas.clipPath(path)
        }

        return ret
    }

    override fun clipRect(left: Int, top: Int, right: Int, bottom:Int) {
        if (normCavas != null) {
            normCavas?.clipRect(left, top, right, bottom)
        } else {
//            glCanvas.clipPath(path)
        }
    }
    override fun drawBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint) {
        if (normCavas != null) {
            normCavas?.drawBitmap(bitmap, matrix, paint);
        } else {
            innerMatrix.reset()

            matrix.getValues(tmpMatValues);
            innerMatrix.translate(tmpMatValues[Matrix.MTRANS_X], tmpMatValues[Matrix.MTRANS_Y])
            innerMatrix.scale(tmpMatValues[Matrix.MSCALE_X], tmpMatValues[Matrix.MSCALE_Y])

            glCanvas.drawBitmap(bitmap, innerMatrix)
        }
    }

    override fun drawPath(path: Path, paint: Paint) {
        if (normCavas != null) {
            normCavas?.drawPath(path, paint)
        } else {

        }
    }

    override fun concat(matrix: Matrix) {
        if (normCavas != null) {
            normCavas?.concat(matrix)
        } else {

        }
    }

    override fun restore() {
        if (normCavas != null) {
            normCavas?.restore()
        } else {
            glCanvas?.restore()
        }
    }


}