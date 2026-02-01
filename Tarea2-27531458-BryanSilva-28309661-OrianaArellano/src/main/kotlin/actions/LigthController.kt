package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.Scalar

class LigthController {
    //Cambio de Brillo
    fun brightness(imageMatrix: ImageMatrix, change: Number): ImageMatrix {
        val newImage = imageMatrix.copy()
        val realChange = change.toDouble()
        val scalar = Scalar(realChange, realChange, realChange, 0.0)
        Core.add(imageMatrix.image, scalar, newImage.image)
        return newImage
    }
    //Cambio de Contraste
    fun contrast(imageMatrix: ImageMatrix, change: Number): ImageMatrix {
        val newImage = imageMatrix.copy()
        val realChange = change.toDouble()
        val scalar = Scalar(realChange, realChange, realChange, 0.0)
        Core.multiply(imageMatrix.image, scalar, newImage.image)
        return newImage
    }
}