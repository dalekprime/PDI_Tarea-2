package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class FrequencyController {

    fun applyDFT(input: ImageMatrix): ImageMatrix {
        val src = input.image

        //Convertir a gris
        val gray = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        val floatImg = Mat()
        gray.convertTo(floatImg, CvType.CV_32F)

        val complex = ArrayList<Mat>()
        complex.add(floatImg)
        complex.add(Mat.zeros(floatImg.size(), CvType.CV_32F))
        val complexI = Mat()
        Core.merge(complex, complexI)

        //DFT
        Core.dft(complexI, complexI)

        //Magnitud
        Core.split(complexI, complex)
        val mag = Mat()
        Core.magnitude(complex[0], complex[1], mag)

        //log(1 + mag)
        Core.add(mag, Scalar.all(1.0), mag)
        Core.log(mag, mag)

        //Centrar cuadrantes
        shiftQuadrants(mag)

        //Normalizar
        Core.normalize(mag, mag, 0.0, 255.0, Core.NORM_MINMAX)

        //Convertir a 8 bits
        val result = Mat()
        mag.convertTo(result, CvType.CV_8U)

        //Fondo blanco y detalles oscuros
        Core.bitwise_not(result, result)

        return ImageMatrix(result)
    }

    fun applyDFTPhase(input: ImageMatrix): ImageMatrix {
        val src = input.image
        val gray = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        val floatImg = Mat()
        gray.convertTo(floatImg, CvType.CV_32F)
        val complex = ArrayList<Mat>()
        complex.add(floatImg)
        complex.add(Mat.zeros(floatImg.size(), CvType.CV_32F))
        val complexI = Mat()
        Core.merge(complex, complexI)
        Core.dft(complexI, complexI)
        Core.split(complexI, complex)
        val phaseMat = Mat()
        Core.phase(complex[0], complex[1], phaseMat)
        shiftQuadrants(phaseMat)
        Core.normalize(phaseMat, phaseMat, 0.0, 255.0, Core.NORM_MINMAX)
        val result = Mat()
        phaseMat.convertTo(result, CvType.CV_8U)
        return ImageMatrix(result)
    }

    fun applyDCT(input: ImageMatrix): ImageMatrix {
        val src = input.image

        //Convertir a Gris
        val gray = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        val floatImg = Mat()
        gray.convertTo(floatImg, CvType.CV_32F)

        //Tamaño par sin padding
        val newWidth = floatImg.cols() and -2
        val newHeight = floatImg.rows() and -2
        val evenImg = floatImg.submat(Rect(0, 0, newWidth, newHeight))

        //DCT
        val dctMat = Mat()
        Core.dct(evenImg, dctMat)

        //Logaritmo
        Core.absdiff(dctMat, Scalar.all(0.0), dctMat)
        Core.add(dctMat, Scalar.all(1.0), dctMat)
        Core.log(dctMat, dctMat)
        //Normalizar
        Core.normalize(dctMat, dctMat, 0.0, 255.0, Core.NORM_MINMAX)

        //Convertir a 8 bits
        val result = Mat()
        dctMat.convertTo(result, CvType.CV_8U)

        //Fondo blanco y detalles oscuros
        Core.bitwise_not(result, result)

        return ImageMatrix(result)
    }

    fun applyDFTFilter(input: ImageMatrix, radius: Double, isLowPass: Boolean): ImageMatrix {
        val src = input.image

        //Separar Canales
        val channels = ArrayList<Mat>()
        Core.split(src, channels)
        val resultChannels = ArrayList<Mat>()

        //Padding
        val rows = Core.getOptimalDFTSize(src.rows())
        val cols = Core.getOptimalDFTSize(src.cols())

        //Máscara Gaussiana
        val mask = Mat(rows, cols, CvType.CV_32F)
        val cx = cols / 2.0
        val cy = rows / 2.0
        val twoSigmaSq = 2 * radius * radius

        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy)
                var value = kotlin.math.exp(-distSq / twoSigmaSq)

                if (!isLowPass) value = 1.0 - value
                mask.put(y, x, value)
            }
        }
        shiftQuadrants(mask)

        val maskComplexArr = ArrayList<Mat>()
        maskComplexArr.add(mask)
        maskComplexArr.add(mask)
        val maskMulti = Mat()
        Core.merge(maskComplexArr, maskMulti)

        //Procesar Canales
        for (channel in channels) {
            val floatImg = Mat()
            channel.convertTo(floatImg, CvType.CV_32F)

            val padded = Mat()
            Core.copyMakeBorder(floatImg, padded, 0, rows - floatImg.rows(), 0, cols - floatImg.cols(), Core.BORDER_CONSTANT, Scalar.all(0.0))

            val complex = ArrayList<Mat>()
            complex.add(padded)
            complex.add(Mat.zeros(padded.size(), CvType.CV_32F))
            val complexI = Mat()
            Core.merge(complex, complexI)

            Core.dft(complexI, complexI)

            if (isLowPass) {
                Core.multiply(complexI, maskMulti, complexI)
            } else {
                Core.mulSpectrums(complexI, maskMulti, complexI, 0)
            }

            //IDFT
            Core.idft(complexI, complexI, Core.DFT_SCALE or Core.DFT_REAL_OUTPUT)

            if (!isLowPass) {
                Core.normalize(complexI, complexI, 0.0, 255.0, Core.NORM_MINMAX)
            }

            val result = Mat()
            complexI.convertTo(result, CvType.CV_8U) // Aquí se ajustan los colores automáticamente
            val finalChannel = result.submat(Rect(0, 0, src.cols(), src.rows()))
            resultChannels.add(finalChannel)
        }

        val finalImage = Mat()
        Core.merge(resultChannels, finalImage)

        return ImageMatrix(finalImage)
    }

    fun applyDCTFilter(input: ImageMatrix, radius: Double, isLowPass: Boolean): ImageMatrix {
        val src = input.image

        //Separar Canales
        val channels = ArrayList<Mat>()
        Core.split(src, channels)
        val resultChannels = ArrayList<Mat>()

        //Padding
        val rows = src.rows()
        val cols = src.cols()
        val paddedRows = if (rows % 2 != 0) rows + 1 else rows
        val paddedCols = if (cols % 2 != 0) cols + 1 else cols

        //Máscara Gaussiana
        val mask = Mat(paddedRows, paddedCols, CvType.CV_32F)
        val twoSigmaSq = 2 * radius * radius

        for (y in 0 until paddedRows) {
            for (x in 0 until paddedCols) {
                val distSq = (x * x).toDouble() + (y * y).toDouble()
                var value = kotlin.math.exp(-distSq / twoSigmaSq)

                if (!isLowPass) {
                    value = 1.0 - value
                }
                mask.put(y, x, value)
            }
        }

        //Procesar Canales
        for (channel in channels) {
            val floatImg = Mat()
            channel.convertTo(floatImg, CvType.CV_32F)

            val padded = Mat()
            Core.copyMakeBorder(floatImg, padded, 0, paddedRows - rows, 0, paddedCols - cols, Core.BORDER_CONSTANT, Scalar.all(0.0))

            //DCT
            val dctMat = Mat()
            Core.dct(padded, dctMat)

            Core.multiply(dctMat, mask, dctMat)

            //IDCT
            Core.idct(dctMat, dctMat)

            if (!isLowPass) {
                Core.normalize(dctMat, dctMat, 0.0, 255.0, Core.NORM_MINMAX)
            }

            val result = Mat()
            dctMat.convertTo(result, CvType.CV_8U) // OpenCV recorta automáticamente a 0-255

            val finalChannel = result.submat(Rect(0, 0, cols, rows))
            resultChannels.add(finalChannel)
        }

        //Unir Canales
        val finalImage = Mat()
        Core.merge(resultChannels, finalImage)

        return ImageMatrix(finalImage)
    }

    private fun shiftQuadrants(image: Mat) {
        val cx = image.cols() / 2
        val cy = image.rows() / 2
        val q0 = image.submat(Rect(0, 0, cx, cy))
        val q1 = image.submat(Rect(cx, 0, cx, cy))
        val q2 = image.submat(Rect(0, cy, cx, cy))
        val q3 = image.submat(Rect(cx, cy, cx, cy))

        val tmp = Mat()
        q0.copyTo(tmp)
        q3.copyTo(q0)
        tmp.copyTo(q3)

        q1.copyTo(tmp)
        q2.copyTo(q1)
        tmp.copyTo(q2)
    }

}