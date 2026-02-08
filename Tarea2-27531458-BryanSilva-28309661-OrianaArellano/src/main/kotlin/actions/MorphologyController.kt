package actions

import models.ImageMatrix
import models.Kernel
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class MorphologyController {
    //Operaciones básicas
    fun erode(imageMatrix: ImageMatrix, kernel: Mat): ImageMatrix {
        return applyMorphology(imageMatrix, Imgproc.MORPH_ERODE, kernel)
    }
    fun dilate(imageMatrix: ImageMatrix, kernel: Mat): ImageMatrix {
        return applyMorphology(imageMatrix, Imgproc.MORPH_DILATE, kernel)
    }
    fun open(imageMatrix: ImageMatrix, kernel: Mat): ImageMatrix {
        return applyMorphology(imageMatrix, Imgproc.MORPH_OPEN, kernel)
    }
    fun close(imageMatrix: ImageMatrix, kernel: Mat): ImageMatrix {
        return applyMorphology(imageMatrix, Imgproc.MORPH_CLOSE, kernel)
    }
    // Función genérica
    private fun applyMorphology(imageMatrix: ImageMatrix, operation: Int, kernel: Mat): ImageMatrix {
        val dest = Mat()
        Imgproc.morphologyEx(imageMatrix.image, dest, operation, kernel)
        return ImageMatrix(dest, imageMatrix)
    }
    // Generador de Elementos Estructurantes
    fun createStructuringElement(shapeType: String, size: Int): Mat {
        val shape = when (shapeType) {
            "Cruz" -> Imgproc.MORPH_CROSS
            "Elipse" -> Imgproc.MORPH_ELLIPSE
            "Rectangulo" -> Imgproc.MORPH_RECT
            else -> Imgproc.MORPH_RECT
        }
        val finalSize = if (size % 2 == 0) size + 1 else size
        return Imgproc.getStructuringElement(shape, Size(finalSize.toDouble(), finalSize.toDouble()))
    }
    fun customKernelToMat(k: Kernel): Mat {
        val rows = k.height
        val cols = k.width
        val mat = Mat(rows, cols, CvType.CV_8U)
        val buffer = ByteArray(rows * cols)
        var count = 0
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val value = k.matrix[y][x]
                buffer[count++] = if (value > 0.0) 1.toByte() else 0.toByte()
            }
        }
        mat.put(0, 0, buffer)
        return mat
    }
}