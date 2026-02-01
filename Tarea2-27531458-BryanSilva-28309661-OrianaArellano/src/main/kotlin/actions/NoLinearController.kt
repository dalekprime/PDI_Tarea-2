package actions

import models.ImageMatrix
import models.Pixel

class NoLinearController {
    //Filtro de Mediana
    fun applyMedianFilter(imageMatrix: ImageMatrix, size: Int): ImageMatrix {
        val newImage = imageMatrix.copy()
        return newImage;
    }
}