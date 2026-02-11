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

        // 1. Separar los canales (Blue, Green, Red)
        val channels = ArrayList<Mat>()
        Core.split(src, channels)
        val resultChannels = ArrayList<Mat>()

        // 2. Procesar cada canal individualmente
        for (channel in channels) {
            // A. Convertir a Float para cálculos precisos
            val imgFloat = Mat()
            channel.convertTo(imgFloat, CvType.CV_32F)

            val h = imgFloat.rows()
            val w = imgFloat.cols()

            // B. Calcular la Media Local (mu)
            val kernel = Size(kernelSize.toDouble(), kernelSize.toDouble())
            val mu = Mat()
            Imgproc.blur(imgFloat, mu, kernel)

            // C. Calcular la Media de Cuadrados (E[x^2])
            val imgSqr = Mat()
            Core.multiply(imgFloat, imgFloat, imgSqr)
            val muSqr = Mat()
            Imgproc.blur(imgSqr, muSqr, kernel)

            // D. Calcular la Varianza Local (sigma^2 = E[x^2] - mu^2)
            val muMu = Mat()
            Core.multiply(mu, mu, muMu)
            val sigma2 = Mat()
            Core.subtract(muSqr, muMu, sigma2)

            // E. Aplicar la fórmula de Wiener pixel a pixel
            val result = Mat(h, w, CvType.CV_32F)

            // Optimizamos obteniendo arrays de java para no llamar a JNI en cada pixel (es mucho más rápido)
            // Nota: Si la imagen es muy grande, esto consume memoria, pero para tu proyecto está bien.
            // Si prefieres la versión segura pixel a pixel, puedes usar el doble for loop anterior.

            for (i in 0 until h) {
                for (j in 0 until w) {
                    // Obtenemos valores
                    val s2 = sigma2.get(i, j)[0]
                    val m = mu.get(i, j)[0]
                    val p = imgFloat.get(i, j)[0]

                    // Fórmula: dst = mu + (max(0, sigma2 - noise) / max(sigma2, noise)) * (src - mu)
                    // Si la varianza es mayor al ruido, preservamos el detalle.
                    // Si es menor, suavizamos.
                    var k = 0.0
                    if (s2 > noiseVariance) {
                        k = (s2 - noiseVariance) / s2
                    }

                    // Calculamos nuevo valor
                    val newVal = m + k * (p - m)
                    result.put(i, j, newVal)
                }
            }

            // F. Convertir de vuelta a 8 bits (Sin Normalizar, solo convertir)
            // convertTo con CV_8U hace el "clamping" (recorta <0 a 0 y >255 a 255) automáticamente.
            val finalChannel = Mat()
            result.convertTo(finalChannel, CvType.CV_8U)
            resultChannels.add(finalChannel)
        }

        // 3. Unir los canales procesados
        val finalImage = Mat()
        Core.merge(resultChannels, finalImage)

        return ImageMatrix(finalImage)
    }

    companion object
}