package actions

import models.ImageMatrix
import models.Pixel

class NoLinearController {
    //Filtro de Mediana
    fun applyMedianFilter(image: ImageMatrix, size: Int): ImageMatrix {
        val width = image.width
        val height = image.height
        val result = ImageMatrix(width, height)
        result.header = image.header
        result.maxVal = image.maxVal
        val offset = size / 2
        val neighborValuesR = IntArray(size * size)
        val neighborValuesG = IntArray(size * size)
        val neighborValuesB = IntArray(size * size)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var count = 0
                for (ky in -offset..offset) {
                    for (kx in -offset..offset) {
                        val imgY = (y + ky).coerceIn(0, height - 1)
                        val imgX = (x + kx).coerceIn(0, width - 1)
                        val pixel = image.pixels[imgY][imgX]
                        neighborValuesR[count] = pixel.r
                        neighborValuesG[count] = pixel.g
                        neighborValuesB[count] = pixel.b
                        count++
                    }
                }
                neighborValuesR.sort()
                neighborValuesG.sort()
                neighborValuesB.sort()
                val medianIndex = (size * size) / 2
                result.pixels[y][x] = Pixel(
                    neighborValuesR[medianIndex],
                    neighborValuesG[medianIndex],
                    neighborValuesB[medianIndex]
                )
            }
        }
        return result
    }
}