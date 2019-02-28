package com.opensource.svgaplayer

import android.graphics.*
import com.kugou.graphic.ICanvasGL

class CanvasWrapper constructor(var normCavas: Canvas?,var glCanvas: ICanvasGL?) : ICanvas {
    private var innerMatrix : ICanvasGL.OrthoBitmapMatrix = ICanvasGL.OrthoBitmapMatrix()
    private var srcTmpPoint = floatArrayOf(1.0f, 1.0f)
    private var dstTmpPoint = FloatArray(2)
    private var tmpMatValues = FloatArray(9)

    override val width: Int
        get() {
            return if (normCavas != null) {
                normCavas?.width ?: 0
            } else if (glCanvas != null) {
                glCanvas?.width ?: 0
            } else {
                0
            }
        }

    override val height: Int
        get() {
            return if (normCavas != null) {
                normCavas?.height ?: 0
            } else if (glCanvas != null) {
                glCanvas?.height ?: 0
            } else {
                0
            }
        }


    override fun save(): Int {
        var ret = 0
        if (normCavas != null) {
            ret = normCavas?.save() ?: 0
        } else {
            glCanvas?.save()
        }

        return ret
    }

    override fun clipPath(path : Path):Boolean {
        return if (normCavas != null) {
            normCavas?.clipPath(path) ?: false
        } else {
            false
//            glCanvas.clipPath(path)
        }
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
            normCavas?.drawBitmap(bitmap, matrix, paint)
        } else {
            innerMatrix.reset()
            matrix.getValues(tmpMatValues)
//            matrix.mapPoints(dstTmpPoint, srcTmpPoint)
//
//            var rad = Math.atan2(dstTmpPoint[1].toDouble(), dstTmpPoint[0].toDouble())
//            var degree = Math.toDegrees(rad)

            innerMatrix.translate(tmpMatValues[Matrix.MTRANS_X], tmpMatValues[Matrix.MTRANS_Y])
//            innerMatrix.scale(tmpMatValues[Matrix.MSCALE_X], tmpMatValues[Matrix.MSCALE_Y])

            glCanvas?.drawBitmap(bitmap, innerMatrix)
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