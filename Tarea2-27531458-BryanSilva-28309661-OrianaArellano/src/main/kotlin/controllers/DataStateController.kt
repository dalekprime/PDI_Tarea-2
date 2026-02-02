package controllers

import javafx.scene.control.Label
import models.ImageMatrix

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
        val mat = imageMatrix?.image ?: return
        //Setea las dimensiones
        val width = imageMatrix.image.width()
        val height = imageMatrix.image.height()
        dimImage.text = "Dimensiones: $width x $height"
        //Setea la cantidad de colores unicos
        val uniqueColors = HashSet<Int>()
        val channels = mat.channels()
        val totalPixels = width.toLong() * height.toLong()
        val bufferSize = (totalPixels * channels).toInt()
        val buffer = ByteArray(bufferSize)
        mat.get(0, 0, buffer)
        var index = 0
        if (channels >= 3) {
            for (i in 0 until totalPixels.toInt()) {
                val b = buffer[index].toInt() and 0xFF
                val g = buffer[index + 1].toInt() and 0xFF
                val r = buffer[index + 2].toInt() and 0xFF
                var colorHash = (r shl 16) or (g shl 8) or b
                if (channels == 4) {
                    val a = buffer[index + 3].toInt() and 0xFF
                    colorHash = (a shl 24) or colorHash
                }
                uniqueColors.add(colorHash)
                index += channels
            }
        } else {
            for (i in 0 until totalPixels.toInt()) {
                val gray = buffer[index].toInt() and 0xFF
                uniqueColors.add(gray)
                index++
            }
        }
        colorsImage.text = "#Colores: ${uniqueColors.size}"
        //Setea la cantidad de bit por pixel
        val bpp = mat.elemSize() * 8
        bppImage.text = "Bpp: ${bpp.toInt()}"
    }
}