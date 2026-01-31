package actions

import models.ImageMatrix
import models.Pixel

class RotationController {
    //Espejo Horizontal
    fun mirrorH(imageMatrix: ImageMatrix): ImageMatrix{
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width, height)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = imageMatrix.pixels[y][width-1-x]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
        return newImage
    }
    //Espejo Vertical
    fun mirrorV(imageMatrix: ImageMatrix): ImageMatrix{
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width, height)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = imageMatrix.pixels[height-1-y][x]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
        return newImage
    }
    //Rotacion de 90 Grados
    fun rotation90(imageMatrix: ImageMatrix): ImageMatrix{
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(height, width)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until width) {
            for (x in 0 until height) {
                val p = imageMatrix.pixels[height-1-x][y]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
        return newImage
    }
    //Rotacion de 180 Grados
    fun rotation180(imageMatrix: ImageMatrix): ImageMatrix{
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(width, height)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = imageMatrix.pixels[height-1-y][width-1-x]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
        return newImage
    }
    //Rotacion de 270 Grados
    fun rotation270(imageMatrix: ImageMatrix): ImageMatrix{
        val width = imageMatrix.width
        val height = imageMatrix.height
        val newImage = ImageMatrix(height, width)
        newImage.maxVal = imageMatrix.maxVal
        newImage.header = imageMatrix.header
        for (y in 0 until width) {
            for (x in 0 until height) {
                val p = imageMatrix.pixels[x][width-1-y]
                newImage.pixels[y][x] = Pixel(p.r, p.g, p.b)
            }
        }
        return newImage
    }
}