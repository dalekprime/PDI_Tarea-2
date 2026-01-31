package actions

import models.ImageMatrix
import models.Pixel

class UmbralizerController {
    //Umbral Simple
    fun simpleUmbral(imageMatrix: ImageMatrix, threshold: Number): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val grey = (0.299*imageMatrix.pixels[y][x].r + 0.587*imageMatrix.pixels[y][x].g + 0.114*imageMatrix.pixels[y][x].b)
                val ele = if (grey < threshold.toInt()) 0 else 255
                imageMatrix.pixels[y][x] = Pixel(ele, ele, ele)
            }
        }
        imageMatrix.header = "P1"
        return imageMatrix
    }
    //Umbral Multiple
    fun multiUmbral(imageMatrix: ImageMatrix, thresholdInf: Number, thresholdSup: Number): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        val t1 = thresholdInf.toInt()
        val t2 = thresholdSup.toInt()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val grey = (0.299*imageMatrix.pixels[y][x].r + 0.587*imageMatrix.pixels[y][x].g + 0.114*imageMatrix.pixels[y][x].b)
                if (grey >= t1 && grey <= t2) {
                    imageMatrix.pixels[y][x] = Pixel(255, 255, 255)
                } else {
                    imageMatrix.pixels[y][x] = Pixel(0, 0, 0)
                }
            }
        }
        imageMatrix.header = "P1"
        return imageMatrix
    }
}