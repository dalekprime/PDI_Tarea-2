package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

class UmbralizerController {
    //Umbral Simple
    fun simpleUmbral(imageMatrix: ImageMatrix, threshold: Number): ImageMatrix {
        val t = threshold.toDouble()
        val grayMat = Mat()
        if (imageMatrix.image.channels() >= 3) {
            if (imageMatrix.image.channels() == 4) {
                Imgproc.cvtColor(imageMatrix.image, grayMat, Imgproc.COLOR_BGRA2GRAY)
            } else {
                Imgproc.cvtColor(imageMatrix.image, grayMat, Imgproc.COLOR_BGR2GRAY)
            }
        } else {
            imageMatrix.image.copyTo(grayMat)
        }
        val binaryMat = Mat()
        Imgproc.threshold(grayMat, binaryMat, t, 255.0, Imgproc.THRESH_BINARY)
        grayMat.release()
        return ImageMatrix(binaryMat, imageMatrix)
    }
    //Umbral Multiple
    fun multiUmbral(imageMatrix: ImageMatrix, thresholdInf: Number, thresholdSup: Number): ImageMatrix {
        val t1 = thresholdInf.toDouble()
        val t2 = thresholdSup.toDouble()
        val grayMat = Mat()
        if (imageMatrix.image.channels() >= 3) {
            if (imageMatrix.image.channels() == 4) {
                Imgproc.cvtColor(imageMatrix.image, grayMat, Imgproc.COLOR_BGRA2GRAY)
            } else {
                Imgproc.cvtColor(imageMatrix.image, grayMat, Imgproc.COLOR_BGR2GRAY)
            }
        } else {
            imageMatrix.image.copyTo(grayMat)
        }
        val binaryMat = Mat()
        Core.inRange(grayMat, Scalar(t1), Scalar(t2), binaryMat)
        grayMat.release()
        return ImageMatrix(binaryMat, imageMatrix)
    }

    fun applyOtsu(imageMatrix: ImageMatrix): ImageMatrix {
        val src = imageMatrix.image
        val gray = Mat()
        val dst = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        Imgproc.threshold(gray, dst, 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)
        gray.release()
        return ImageMatrix(dst, imageMatrix)
    }

    fun applyIsodata(imageMatrix: ImageMatrix): ImageMatrix {
        val src = imageMatrix.image
        val gray = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        var tCurrent = Core.mean(gray).`val`[0]
        var tNext: Double
        val tolerance = 0.5
        while (true) {
            val maskLow = Mat()
            val maskHigh = Mat()
            Imgproc.threshold(gray, maskLow, tCurrent, 255.0, Imgproc.THRESH_BINARY_INV)
            Imgproc.threshold(gray, maskHigh, tCurrent, 255.0, Imgproc.THRESH_BINARY)
            val meanLow = Core.mean(gray, maskLow).`val`[0]
            val meanHigh = Core.mean(gray, maskHigh).`val`[0]
            if (meanLow == 0.0 && meanHigh == 0.0) break
            tNext = (meanLow + meanHigh) / 2.0
            maskLow.release()
            maskHigh.release()
            if (abs(tNext - tCurrent) < tolerance) break
            tCurrent = tNext
        }
        val dst = Mat()
        Imgproc.threshold(gray, dst, tCurrent, 255.0, Imgproc.THRESH_BINARY)
        gray.release()
        return ImageMatrix(dst, imageMatrix)
    }

}