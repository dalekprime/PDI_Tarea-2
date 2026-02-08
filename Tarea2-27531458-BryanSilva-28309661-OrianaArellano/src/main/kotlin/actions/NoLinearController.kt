package actions

import models.ImageMatrix
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class NoLinearController {
    //Filtro de Mediana
    fun applyMedianFilter(imageMatrix: ImageMatrix, size: Int): ImageMatrix {
        val src = imageMatrix.image
        val dest = Mat()
        var kSize = size
        if (kSize % 2 == 0) {
            kSize++
        }
        if (kSize < 1) kSize = 1
        Imgproc.medianBlur(src, dest, kSize)
        return ImageMatrix(dest, imageMatrix)
    }
}