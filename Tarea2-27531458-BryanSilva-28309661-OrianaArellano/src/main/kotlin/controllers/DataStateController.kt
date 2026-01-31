package controllers

import javafx.scene.control.Label
import models.ImageMatrix
import models.Pixel
import kotlin.math.ceil
import kotlin.math.log

class DataStateController {

    //Referencias a las etiquetas de datos
    private var dimImage: Label
    private var colorsImage: Label
    private var bppImage: Label

    constructor(dimImage: Label, colorsImage: Label, bppImage: Label) {
        this.dimImage = dimImage
        this.colorsImage = colorsImage
        this.bppImage = bppImage
    }

    fun update(imageMatrix: ImageMatrix?) {
        imageMatrix?: return
        //Setea las dimensiones
        val width = imageMatrix.width
        val height = imageMatrix.height
        dimImage.text = "Dimensiones: ${imageMatrix.width} x ${imageMatrix.height}"
        //Setea la cantidad de colores unicos
        val uniqueColors = HashSet<Pixel>()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = imageMatrix.pixels[y][x]
                uniqueColors.add(pixel)
            }
        }
        colorsImage.text = "#Colores: ${uniqueColors.size}"
        //Setea la cantidad de bit por pixel
        val bbp = when(imageMatrix.header){
            "P1" -> 1
            "P2" -> ceil(log(imageMatrix.maxVal.toDouble(), 2.0)).toInt()
            "P3" -> ceil(3 * log(imageMatrix.maxVal.toDouble(), 2.0)).toInt()
            "PNG/BMP" -> 24
            else -> return
        }
        bppImage.text = "Bpp: $bbp"
    }
}