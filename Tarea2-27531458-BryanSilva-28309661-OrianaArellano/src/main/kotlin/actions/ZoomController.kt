package actions

import models.ImageMatrix
import models.Pixel

class ZoomController {
    //ZoomIn Vecino Proximo
    fun zoomINN(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        val newImage = imageMatrix.copy()
        return newImage;
    }
    //ZoomIn Interpolacion Bilineal
    fun zoomInBLI(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        val newImage = imageMatrix.copy()
        return newImage;
    }
    //ZoomOut Vecino Proximo
    fun zoomOutN(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        val newImage = imageMatrix.copy()
        return newImage;
    }
    //ZoomOut Interpolacion Bilineal
    fun zoomOutSupersampling(imageMatrix: ImageMatrix, factor: Int): ImageMatrix {
        val newImage = imageMatrix.copy()
        return newImage;
    }
}