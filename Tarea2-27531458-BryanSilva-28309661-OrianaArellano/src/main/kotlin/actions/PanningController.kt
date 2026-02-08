package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import kotlin.math.max
import kotlin.math.min

class PanningController {
    fun panningNOEX(imageMatrix: ImageMatrix, x1: Double, y1: Double, x2: Double, y2: Double): ImageMatrix {
        val mat = imageMatrix.image
        val imgW = mat.cols()
        val imgH = mat.rows()
        var startX = min(x1, x2)
        var startY = min(y1, y2)
        var endX = max(x1, x2)
        var endY = max(y1, y2)
        startX = startX.coerceIn(0.0, imgW.toDouble())
        startY = startY.coerceIn(0.0, imgH.toDouble())
        endX = endX.coerceIn(0.0, imgW.toDouble())
        endY = endY.coerceIn(0.0, imgH.toDouble())
        val width = (endX - startX).toInt()
        val height = (endY - startY).toInt()
        if (width <= 0 || height <= 0) {
            return imageMatrix
        }
        val roi = Rect(startX.toInt(), startY.toInt(), width, height)
        val crop = mat.submat(roi)
        val result = crop.clone()
        crop.release()
        return ImageMatrix(result, imageMatrix)
    }

    fun panningEX(imageMatrix: ImageMatrix, x0: Double, y0: Double, x1: Double, y1: Double): ImageMatrix {
        val src = imageMatrix.image
        val viewX = min(x0, x1).toInt()
        val viewY = min(y0, y1).toInt()
        val viewW = kotlin.math.abs(x1 - x0).toInt()
        val viewH = kotlin.math.abs(y1 - y0).toInt()
        if (viewW <= 0 || viewH <= 0) return ImageMatrix(src.clone(), imageMatrix)
        val dest = Mat.zeros(viewH, viewW, src.type())
        val intersectX0 = max(0, viewX)
        val intersectY0 = max(0, viewY)
        val intersectX1 = min(src.width(), viewX + viewW)
        val intersectY1 = min(src.height(), viewY + viewH)
        if (intersectX1 > intersectX0 && intersectY1 > intersectY0) {
            val srcRect = Rect(intersectX0, intersectY0, intersectX1 - intersectX0, intersectY1 - intersectY0)
            val destX = intersectX0 - viewX
            val destY = intersectY0 - viewY
            val destRect = Rect(destX, destY, srcRect.width, srcRect.height)
            val srcSub = src.submat(srcRect)
            val destSub = dest.submat(destRect)
            srcSub.copyTo(destSub)
            srcSub.release()
            destSub.release()
        }
        return ImageMatrix(dest, imageMatrix)
    }

    fun panningEXLL(imageMatrix: ImageMatrix): ImageMatrix {
        val src = imageMatrix.image
        val currentW = src.cols()
        val currentH = src.rows()
        val padLeft = (-imageMatrix.currentPanningLevelX0).coerceAtLeast(0.0).toInt()
        val padTop = (-imageMatrix.currentPanningLevelY0).coerceAtLeast(0.0).toInt()
        val padRight = (imageMatrix.currentPanningLevelX1 - currentW).coerceAtLeast(0.0).toInt()
        val padBottom = (imageMatrix.currentPanningLevelY1 - currentH).coerceAtLeast(0.0).toInt()
        if (padLeft == 0 && padTop == 0 && padRight == 0 && padBottom == 0) {
            return imageMatrix
        }
        val dst = Mat()
        Core.copyMakeBorder(
            src,
            dst,
            padTop,
            padBottom,
            padLeft,
            padRight,
            Core.BORDER_CONSTANT,
            Scalar(0.0, 0.0, 0.0, 255.0) // Negro total
        )
        val newMatrix = ImageMatrix(dst, imageMatrix)
        newMatrix.currentPanningLevelX0 = 0.0
        newMatrix.currentPanningLevelY0 = 0.0
        newMatrix.currentPanningLevelX1 = dst.cols().toDouble()
        newMatrix.currentPanningLevelY1 = dst.rows().toDouble()
        return newMatrix
    }


}