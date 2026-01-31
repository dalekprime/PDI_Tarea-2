package actions

import models.ImageMatrix
import models.Pixel

class ZoomController {
    //ZoomIn Vecino Proximo
    fun zoomINN(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width * factor, height * factor)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until newImage.height) {
            val y1 = (y / factor).coerceIn(0, height - 1)
            for (x in 0 until newImage.width) {
                val x1 = (x / factor).coerceIn(0, width - 1)
                val p = imageMatrix.pixels[y1][x1]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
       return newImage
    }
    //ZoomIn Interpolacion Bilineal
    fun zoomInBLI(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width * factor, height * factor)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        fun rangeX(x: Int) = x.coerceIn(0, width-1)
        fun rangeY(y: Int) = y.coerceIn(0, height-1)
        fun mix(c00: Int, c10: Int, c01: Int, c11: Int, a: Double, b: Double): Int {
            val r0 = (1 - a) * c00 + a * c10
            val r1 = (1 - a) * c01 + a * c11
            val c = (1 - b) * r0 + b * r1
            return c.toInt().coerceIn(0, 255)
        }
        for (y in 0 until newImage.height) {
            val v = y.toDouble() / factor
            val j = v.toInt()
            val b = v - j
            for (x in 0 until newImage.width) {
                val u = x.toDouble() / factor
                val i = u.toInt()
                val a = u - i
                val p00 = imageMatrix.pixels[rangeY(j)][rangeX(i)]
                val p10 = imageMatrix.pixels[rangeY(j)][rangeX(i+1)]
                val p01 = imageMatrix.pixels[rangeY(j+1)][rangeX(i)]
                val p11 = imageMatrix.pixels[rangeY(j+1)][rangeX(i+1)]
                val r = mix(p00.r, p10.r, p01.r, p11.r, a, b)
                val g = mix(p00.g, p10.g, p01.g, p11.g, a, b)
                val blue = mix(p00.b, p10.b, p01.b, p11.b, a, b)
                newImage.pixels[y][x] = Pixel(r, g, blue)
            }
        }
        return newImage
    }
    //ZoomOut Vecino Proximo
    fun zoomOutN(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        if (factor < 1) return imageMatrix
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width / factor, height / factor)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until newImage.height) {
            val y1 = y * factor
            for (x in 0 until newImage.width) {
                val x1 = x * factor
                val p = imageMatrix.pixels[y1][x1]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
        return newImage
    }
    //ZoomOut Interpolacion Bilineal
    fun zoomOutSupersampling(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        if (factor < 1) return imageMatrix
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width / factor, height / factor)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        val area = (factor * factor).toDouble()
        for (y in 0 until newImage.height) {
            for (x in 0 until newImage.width) {
                var sumR = 0
                var sumG = 0
                var sumB = 0
                val startY = y * factor
                val startX = x * factor
                for (dy in 0 until factor) {
                    for (dx in 0 until factor) {
                        val py = (startY + dy).coerceIn(0, height - 1)
                        val px = (startX + dx).coerceIn(0, width - 1)
                        val p = imageMatrix.pixels[py][px]
                        sumR += p.r
                        sumG += p.g
                        sumB += p.b
                    }
                }
                newImage.pixels[y][x] = Pixel(
                    (sumR / area).toInt(),
                    (sumG / area).toInt(),
                    (sumB / area).toInt()
                )
            }
        }
        return newImage
    }
}