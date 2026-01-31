package actions

import models.ImageMatrix

class LigthController {
    //Cambio de Brillo
    fun brightness(imageMatrix: ImageMatrix, change: Number): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                imageMatrix.pixels[y][x].r = (imageMatrix.pixels[y][x].r + change.toInt()).coerceIn(0, 255)
                imageMatrix.pixels[y][x].g = (imageMatrix.pixels[y][x].g + change.toInt()).coerceIn(0, 255)
                imageMatrix.pixels[y][x].b = (imageMatrix.pixels[y][x].b + change.toInt()).coerceIn(0, 255)
            }
        }
        return imageMatrix
    }
    //Cambio de Contraste
    fun contrast(imageMatrix: ImageMatrix, change: Number): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                imageMatrix.pixels[y][x].r = (((imageMatrix.pixels[y][x].r - 128) * change.toDouble()) + 128).coerceIn(0.0, 255.0).toInt()
                imageMatrix.pixels[y][x].g = (((imageMatrix.pixels[y][x].g - 128) * change.toDouble()) + 128).coerceIn(0.0, 255.0).toInt()
                imageMatrix.pixels[y][x].b = (((imageMatrix.pixels[y][x].b - 128) * change.toDouble()) + 128).coerceIn(0.0, 255.0).toInt()
            }
        }
        return imageMatrix
    }
}