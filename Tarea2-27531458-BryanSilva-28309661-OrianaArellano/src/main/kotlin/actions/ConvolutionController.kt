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
        var src = imageMatrix.image.clone()
        val dst = Mat()
        val tempDst = Mat()
        //Transparencia
        if (src.channels() == 4) {
            val bgr = Mat()
            Imgproc.cvtColor(src, bgr, Imgproc.COLOR_BGRA2BGR)
            src.release()
            src = bgr
        }
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
            tempDst,
            CvType.CV_16S,
            kernelMat,
            Point(-1.0, -1.0),
            bias,
            Core.BORDER_REPLICATE
        )
        Core.convertScaleAbs(tempDst, dst)

        src.release()
        kernelMat.release()
        tempDst.release()
        return ImageMatrix(dst, imageMatrix)
    }
}