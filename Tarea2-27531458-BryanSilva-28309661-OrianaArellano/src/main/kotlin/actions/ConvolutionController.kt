package actions

import models.ImageMatrix
import models.Kernel
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc

class ConvolutionController {
    fun apply(imageMatrix: ImageMatrix, kernel: Kernel, factor: Double = 1.0, bias: Double = 0.0): ImageMatrix {
        val src = imageMatrix.image
        val dst = Mat()
        //Convertir Kernal a Mat
        val kernelMat = Mat(kernel.height, kernel.width, CvType.CV_32F)
        //Aplicar Kenerl
        for (r in 0 until kernel.height) {
            for (c in 0 until kernel.width) {
                val weight = kernel.matrix[r][c]
                kernelMat.put(r, c, weight * factor)
            }
        }
        Imgproc.filter2D(
            src,
            dst,
            -1,
            kernelMat,
            Point(-1.0, -1.0),
            bias,
            Core.BORDER_REPLICATE
        )
        kernelMat.release()
        return ImageMatrix(dst)
    }
}