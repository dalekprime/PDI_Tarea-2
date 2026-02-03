package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

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
        return ImageMatrix(binaryMat)
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
        return ImageMatrix(binaryMat)
    }
}