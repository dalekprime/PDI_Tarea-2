package actions

import models.ImageMatrix
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ZoomController {
    //ZoomIn Vecino Proximo
    fun zoomINN(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        return applyOpenCVZoom(imageMatrix, factor.toDouble(), Imgproc.INTER_NEAREST)
    }
    //ZoomIn Interpolacion Bilineal
    fun zoomInBLI(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        return applyOpenCVZoom(imageMatrix, factor.toDouble(), Imgproc.INTER_LINEAR)
    }
    //ZoomOut Vecino Proximo
    fun zoomOutN(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        if (factor < 1) return imageMatrix
        val scale = 1.0 / factor.toDouble()
        return applyOpenCVZoom(imageMatrix, scale, Imgproc.INTER_NEAREST)
    }
    //ZoomOut Interpolacion Bilineal
    fun zoomOutSupersampling(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        if (factor < 1) return imageMatrix
        val scale = 1.0 / factor.toDouble()
        return applyOpenCVZoom(imageMatrix, scale, Imgproc.INTER_AREA)
    }
    private fun applyOpenCVZoom(imageMatrix: ImageMatrix, scale: Double, interpolation: Int): ImageMatrix {
        val src = imageMatrix.image
        val dst = Mat()
        val newWidth = (src.cols() * scale).toInt()
        val newHeight = (src.rows() * scale).toInt()
        Imgproc.resize(src, dst, Size(newWidth.toDouble(), newHeight.toDouble()), 0.0, 0.0, interpolation)
        return ImageMatrix(dst, imageMatrix)
    }
}