package actions

import models.ImageMatrix
import models.Kernel
import models.Pixel
import kotlin.math.roundToInt

class ConvolutionController {
    fun apply(image: ImageMatrix, kernel: Kernel, factor: Double = 1.0, bias: Double = 0.0): ImageMatrix {
        val width = image.width
        val height = image.height
        val resultImage = ImageMatrix(width, height)
        resultImage.header = image.header
        resultImage.maxVal = image.maxVal

        val kHeight = kernel.height
        val kWidth = kernel.width
        val kCenterY = kHeight / 2
        val kCenterX = kWidth / 2

        for (y in 0 until height) {
            for (x in 0 until width) {
                var redAcc = 0.0
                var greenAcc = 0.0
                var blueAcc = 0.0
                for (ky in 0 until kHeight) {
                    for (kx in 0 until kWidth) {
                        val imgY = (y + ky - kCenterY).coerceIn(0, height - 1)
                        val imgX = (x + kx - kCenterX).coerceIn(0, width - 1)
                        val pixel = image.pixels[imgY][imgX]
                        val weight = kernel.matrix[ky][kx]
                        redAcc += pixel.r * weight
                        greenAcc += pixel.g * weight
                        blueAcc += pixel.b * weight
                    }
                }
                val finalR = ((redAcc * factor) + bias).roundToInt().coerceIn(0, 255)
                val finalG = ((greenAcc * factor) + bias).roundToInt().coerceIn(0, 255)
                val finalB = ((blueAcc * factor) + bias).roundToInt().coerceIn(0, 255)
                resultImage.pixels[y][x] = Pixel(finalR, finalG, finalB)
            }
        }
        return resultImage
    }
}