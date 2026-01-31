package actions

import javafx.scene.control.ColorPicker
import models.ImageMatrix

class TonoController {
    //Negativo
    fun negativeImage(imageMatrix: ImageMatrix): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                imageMatrix.pixels[y][x].r = 255 - imageMatrix.pixels[y][x].r
                imageMatrix.pixels[y][x].g = 255 - imageMatrix.pixels[y][x].g
                imageMatrix.pixels[y][x].b = 255 - imageMatrix.pixels[y][x].b
            }
        }
        return imageMatrix
    }
    //Escala de Grises
    fun greyScale(imageMatrix: ImageMatrix): ImageMatrix {
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val greyColor = 0.299*imageMatrix.pixels[y][x].r + 0.587*imageMatrix.pixels[y][x].g + 0.114*imageMatrix.pixels[y][x].b
                imageMatrix.pixels[y][x].r = greyColor.toInt()
                imageMatrix.pixels[y][x].g = greyColor.toInt()
                imageMatrix.pixels[y][x].b = greyColor.toInt()
            }
        }
        imageMatrix.header = "P2"
       return imageMatrix
    }
    //Escala de Color
    fun colorScale(imageMatrix: ImageMatrix, colorScalePicker: ColorPicker): ImageMatrix {
        val r = (colorScalePicker.value.red * 255.0).toInt()
        val g = (colorScalePicker.value.green * 255.0).toInt()
        val b = (colorScalePicker.value.blue * 255.0).toInt()
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val greyColor = (0.299*imageMatrix.pixels[y][x].r + 0.587*imageMatrix.pixels[y][x].g + 0.114*imageMatrix.pixels[y][x].b).toInt()
                if(greyColor < 128){
                    imageMatrix.pixels[y][x].r = r * greyColor/128
                    imageMatrix.pixels[y][x].g = g * greyColor/128
                    imageMatrix.pixels[y][x].b = b * greyColor/128
                }else{
                    imageMatrix.pixels[y][x].r = r + (255 - r)*(greyColor - 128)/128
                    imageMatrix.pixels[y][x].g = g + (255 - g)*(greyColor - 128)/128
                    imageMatrix.pixels[y][x].b = b + (255 - b)*(greyColor - 128)/128
                }
            }
        }
        return imageMatrix
    }
}