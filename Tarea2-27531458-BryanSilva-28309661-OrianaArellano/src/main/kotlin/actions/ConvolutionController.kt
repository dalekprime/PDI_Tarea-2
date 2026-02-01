package actions

import models.ImageMatrix
import models.Kernel
import models.Pixel
import kotlin.math.roundToInt

class ConvolutionController {
    fun apply(imageMatrix: ImageMatrix, kernel: Kernel, factor: Double = 1.0, bias: Double = 0.0): ImageMatrix {
        val newImage = imageMatrix.copy()
        return newImage;
    }
}