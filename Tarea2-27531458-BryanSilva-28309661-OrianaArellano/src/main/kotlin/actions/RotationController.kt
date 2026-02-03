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
        val radians = Math.toRadians(angle)
        val sin = abs(kotlin.math.sin(radians))
        val cos = abs(kotlin.math.cos(radians))
        val newWidth = (src.width() * cos + src.height() * sin).toInt()
        val newHeight = (src.width() * sin + src.height() * cos).toInt()
        val center = Point(src.width() / 2.0, src.height() / 2.0)
        val rotationMat = Imgproc.getRotationMatrix2D(center, angle, 1.0)
        rotationMat.put(0, 2, rotationMat.get(0, 2)[0] + (newWidth / 2.0 - center.x))
        rotationMat.put(1, 2, rotationMat.get(1, 2)[0] + (newHeight / 2.0 - center.y))
        val dest = Mat()
        Imgproc.warpAffine(
            src,
            dest,
            rotationMat,
            Size(newWidth.toDouble(), newHeight.toDouble()),
            Imgproc.INTER_LINEAR,
            Core.BORDER_CONSTANT,
            Scalar(0.0, 0.0, 0.0, 0.0) // Fondo transparente/negro
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