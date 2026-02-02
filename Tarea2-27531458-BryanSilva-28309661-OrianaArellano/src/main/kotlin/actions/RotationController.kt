package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.Core.flip
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

class RotationController {
    //Espejo Horizontal
    fun mirrorH(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        flip(imageMatrix.image, newImage.image, 1)
        return newImage;
    }
    //Espejo Vertical
    fun mirrorV(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        flip(imageMatrix.image, newImage.image, 0)
        return newImage;
    }
    //Rotacion
    fun rotation(imageMatrix: ImageMatrix, angle: Double): ImageMatrix {
        val src = imageMatrix.image
        val width = src.cols()
        val height = src.rows()
        val center = Point(width / 2.0, height / 2.0)
        val rotationMat = Imgproc.getRotationMatrix2D(center, angle, 1.0)
        val absCos = abs(rotationMat.get(0, 0)[0])
        val absSin = abs(rotationMat.get(0, 1)[0])
        val newWidth = (height * absSin + width * absCos).toInt()
        val newHeight = (height * absCos + width * absSin).toInt()
        val oldCenterX = center.x
        val oldCenterY = center.y
        val newCenterX = newWidth / 2.0
        val newCenterY = newHeight / 2.0
        rotationMat.put(0, 2, rotationMat.get(0, 2)[0] + (newCenterX - oldCenterX))
        rotationMat.put(1, 2, rotationMat.get(1, 2)[0] + (newCenterY - oldCenterY))
        val dest = Mat()
        Imgproc.warpAffine(
            src,
            dest,
            rotationMat,
            Size(newWidth.toDouble(), newHeight.toDouble()),
            Imgproc.INTER_LANCZOS4,
            Core.BORDER_CONSTANT,
            Scalar(0.0, 0.0, 0.0, 0.0)
        )
        rotationMat.release()
        return ImageMatrix(dest)
    }
    //Rotacion 180
    fun rotation180(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        flip(imageMatrix.image, newImage.image, -1)
        return newImage;
    }
}