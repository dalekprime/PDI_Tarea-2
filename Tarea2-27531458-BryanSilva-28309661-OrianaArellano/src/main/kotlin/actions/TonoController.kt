package actions

import javafx.scene.control.ColorPicker
import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class TonoController {
    //Negativo
    fun negativeImage(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        Core.bitwise_not(imageMatrix.image, newImage.image)
        return newImage;
    }
    //Escala de Grises
    fun greyScale(imageMatrix: ImageMatrix): ImageMatrix {
        if (imageMatrix.image.channels() == 1) {
            return imageMatrix.copy()
        }
        val newImage = Mat()
        if (imageMatrix.image.channels() == 4) {
            Imgproc.cvtColor(imageMatrix.image, newImage, Imgproc.COLOR_BGRA2GRAY)
        } else {
            Imgproc.cvtColor(imageMatrix.image, newImage, Imgproc.COLOR_BGR2GRAY)
        }
        return ImageMatrix(newImage);
    }
    //Escala de Color
    fun colorScale(imageMatrix: ImageMatrix, colorScalePicker: ColorPicker): ImageMatrix {
        val r = (colorScalePicker.value.red * 255.0).toInt()
        val g = (colorScalePicker.value.green * 255.0).toInt()
        val b = (colorScalePicker.value.blue * 255.0).toInt()
        val greyImage = Mat()
        if (imageMatrix.image.channels() == 4) {
            Imgproc.cvtColor(imageMatrix.image, greyImage, Imgproc.COLOR_BGRA2GRAY)
        } else {
            Imgproc.cvtColor(imageMatrix.image, greyImage, Imgproc.COLOR_BGR2GRAY)
        }
        val src3Channels = Mat()
        Imgproc.cvtColor(greyImage, src3Channels, Imgproc.COLOR_GRAY2BGR)
        greyImage.release()
        val lut = Mat(1, 256, CvType.CV_8UC3)
        val lutData = ByteArray(256 * 3)
        for (i in 0..255) {
            var rOut: Int
            var gOut: Int
            var bOut: Int
            if (i < 128) {
                rOut = (r * i) / 128
                gOut = (g * i) / 128
                bOut = (b * i) / 128
            } else {
                rOut = r + (255 - r) * (i - 128) / 128
                gOut = g + (255 - g) * (i - 128) / 128
                bOut = b + (255 - b) * (i - 128) / 128
            }
            val index = i * 3
            lutData[index] = bOut.toByte()
            lutData[index + 1] = gOut.toByte()
            lutData[index + 2] = rOut.toByte()
        }
        lut.put(0, 0, lutData)
        val resultMat = Mat()
        Core.LUT(src3Channels, lut, resultMat)
        src3Channels.release()
        lut.release()
        return ImageMatrix(resultMat);
    }
}