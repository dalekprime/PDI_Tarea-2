package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class NoLinearController {
    //Filtro de Mediana
    fun applyMedianFilter(imageMatrix: ImageMatrix, size: Int): ImageMatrix {
        val src = imageMatrix.image
        val dest = Mat()
        var kSize = size
        if (kSize % 2 == 0) {
            kSize++
        }
        if (kSize < 1) kSize = 1
        Imgproc.medianBlur(src, dest, kSize)
        return ImageMatrix(dest, imageMatrix)
    }

    //Filtro de Wiener
    fun applyWiener(input: ImageMatrix, kernelSize: Int = 5, noiseVariance: Double = 400.0): ImageMatrix {
        val src = input.image

        //Separar los canales
        val channels = ArrayList<Mat>()
        Core.split(src, channels)
        val resultChannels = ArrayList<Mat>()

        //Procesar canales
        for (channel in channels) {
            val imgFloat = Mat()
            channel.convertTo(imgFloat, CvType.CV_32F)
            val h = imgFloat.rows()
            val w = imgFloat.cols()
            val kernel = Size(kernelSize.toDouble(), kernelSize.toDouble())
            val mu = Mat()
            Imgproc.blur(imgFloat, mu, kernel)
            val imgSqr = Mat()
            Core.multiply(imgFloat, imgFloat, imgSqr)
            val muSqr = Mat()
            Imgproc.blur(imgSqr, muSqr, kernel)
            val muMu = Mat()
            Core.multiply(mu, mu, muMu)
            val sigma2 = Mat()
            Core.subtract(muSqr, muMu, sigma2)

            val result = Mat(h, w, CvType.CV_32F)

            for (i in 0 until h) {
                for (j in 0 until w) {
                    val s2 = sigma2.get(i, j)[0]
                    val m = mu.get(i, j)[0]
                    val p = imgFloat.get(i, j)[0]
                    var k = 0.0
                    if (s2 > noiseVariance) {
                        k = (s2 - noiseVariance) / s2
                    }
                    val newVal = m + k * (p - m)
                    result.put(i, j, newVal)
                }
            }

            //Convertir a 8 bits
            val finalChannel = Mat()
            result.convertTo(finalChannel, CvType.CV_8U)
            resultChannels.add(finalChannel)
        }

        //Unir los canales
        val finalImage = Mat()
        Core.merge(resultChannels, finalImage)

        return ImageMatrix(finalImage)
    }

    companion object
}