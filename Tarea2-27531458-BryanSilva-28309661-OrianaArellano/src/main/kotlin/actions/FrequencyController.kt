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

        //Convertir a gris y flotante
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

        //Normalizar como Matplotlib
        Core.normalize(mag, mag, 0.0, 255.0, Core.NORM_MINMAX)

        //Convertir a 8 bits
        val result = Mat()
        mag.convertTo(result, CvType.CV_8U)

        //Invertir intensidades
        Core.bitwise_not(result, result)

        return ImageMatrix(result)
    }
        // Ver Espectro DCT
        fun applyDCT1(input: ImageMatrix): ImageMatrix {
            val gray = ensureGrayAndFloat(input.image)
            val padded = getPaddedImage(gray) // DCT prefiere tamaños pares

            val dctMat = Mat()
            Core.dct(padded, dctMat)

            // Visualización Logarítmica
            Core.absdiff(dctMat, Scalar.all(0.0), dctMat)
            Core.add(dctMat, Scalar.all(1.0), dctMat)
            Core.log(dctMat, dctMat)
            Core.normalize(dctMat, dctMat, 0.0, 255.0, Core.NORM_MINMAX)

            val result = Mat()
            dctMat.convertTo(result, CvType.CV_8U)
            Core.bitwise_not(result, result) // Invertir colores

            // Recortar al tamaño original
            return ImageMatrix(result.submat(Rect(0, 0, input.image.width(), input.image.height())))
        }

        // Calcula el tamaño óptimo y añade bordes (padding)
        private fun getPaddedImage(img: Mat): Mat {
            val m = Core.getOptimalDFTSize(img.rows())
            val n = Core.getOptimalDFTSize(img.cols())
            val padded = Mat()
            Core.copyMakeBorder(
                img, padded, 0, m - img.rows(), 0, n - img.cols(),
                Core.BORDER_CONSTANT, Scalar.all(0.0)
            )
            return padded
        }

    fun applyDCT(input: ImageMatrix): ImageMatrix {
        val src = input.image

        // 1. Convertir a Gris y Flotante (Necesario para DCT)
        val gray = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        val floatImg = Mat()
        gray.convertTo(floatImg, CvType.CV_32F)

        // 2. ASEGURAR TAMAÑO PAR (Sin Padding)
        // En lugar de agregar bordes negros, recortamos 1 pixel si sobra.
        // La operación 'and -2' convierte cualquier número impar en el par anterior (501 -> 500).
        val newWidth = floatImg.cols() and -2
        val newHeight = floatImg.rows() and -2

        // Creamos una submatriz (referencia) con tamaño par
        val evenImg = floatImg.submat(Rect(0, 0, newWidth, newHeight))

        // 3. Aplicar DCT Directamente
        val dctMat = Mat()
        Core.dct(evenImg, dctMat)

        // 4. Visualización Logarítmica (Para ver algo, porque la DCT cruda es muy oscura)
        // |dct|
        Core.absdiff(dctMat, Scalar.all(0.0), dctMat)
        // log(1 + |dct|)
        Core.add(dctMat, Scalar.all(1.0), dctMat)
        Core.log(dctMat, dctMat)
        // Normalizar a 0-255
        Core.normalize(dctMat, dctMat, 0.0, 255.0, Core.NORM_MINMAX)

        // 5. Convertir a imagen visible
        val result = Mat()
        dctMat.convertTo(result, CvType.CV_8U)

        // Invertir (Fondo blanco, detalles oscuros) - Opcional, pero se ve mejor
        Core.bitwise_not(result, result)

        return ImageMatrix(result)
    }

    fun applyDFTFilterCommon(input: ImageMatrix, radius: Double, isLowPass: Boolean): ImageMatrix {
        val src = input.image

        // 1. Separar Canales
        val channels = ArrayList<Mat>()
        Core.split(src, channels)
        val resultChannels = ArrayList<Mat>()

        // 2. Padding
        val rows = Core.getOptimalDFTSize(src.rows())
        val cols = Core.getOptimalDFTSize(src.cols())

        // 3. Máscara Gaussiana
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

        // 4. Procesar Canales
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
            Core.mulSpectrums(complexI, maskMulti, complexI, 0)

            // D. IDFT (Usando SCALE para recuperar la intensidad real)
            Core.idft(complexI, complexI, Core.DFT_SCALE or Core.DFT_REAL_OUTPUT)

            // E. --- CORRECCIÓN DEL COLOR AZUL ---
            if (!isLowPass) {
                // Paso Alto: SÍ normalizamos (estiramos contraste) para ver los bordes.
                Core.normalize(complexI, complexI, 0.0, 255.0, Core.NORM_MINMAX)
            }
            // Paso Bajo: NO hacemos nada. Dejamos los valores originales.
            // Al convertir a CV_8U abajo, OpenCV recorta automáticamente lo que sobra.

            // F. Recortar y guardar
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

        //Separar la imagen en 3 canales
        val channels = ArrayList<Mat>()
        Core.split(src, channels)
        val resultChannels = ArrayList<Mat>()

        //Calcular padding ya que DCT necesita tamaño par
        val rows = src.rows()
        val cols = src.cols()
        val paddedRows = if (rows % 2 != 0) rows + 1 else rows
        val paddedCols = if (cols % 2 != 0) cols + 1 else cols

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

            // Normalizar
            Core.normalize(dctMat, dctMat, 0.0, 255.0, Core.NORM_MINMAX)

            val result = Mat()
            dctMat.convertTo(result, CvType.CV_8U)

            val finalChannel = result.submat(Rect(0, 0, cols, rows))
            resultChannels.add(finalChannel)
        }

        //Unir los canales procesados
        val finalImage = Mat()
        Core.merge(resultChannels, finalImage)

        return ImageMatrix(finalImage)
    }

    private fun ensureGrayAndFloat(src: Mat): Mat {
        val gray = Mat()
        if (src.channels() > 1) {
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
        } else {
            src.copyTo(gray)
        }
        val floatImg = Mat()
        gray.convertTo(floatImg, CvType.CV_32F)
        return floatImg
    }

    private fun shiftQuadrants(image: Mat) {
        val cx = image.cols() / 2
        val cy = image.rows() / 2
        val q0 = image.submat(Rect(0, 0, cx, cy))      // Top-Left
        val q1 = image.submat(Rect(cx, 0, cx, cy))     // Top-Right
        val q2 = image.submat(Rect(0, cy, cx, cy))     // Bottom-Left
        val q3 = image.submat(Rect(cx, cy, cx, cy))    // Bottom-Right

        val tmp = Mat()
        q0.copyTo(tmp)
        q3.copyTo(q0)
        tmp.copyTo(q3)

        q1.copyTo(tmp)
        q2.copyTo(q1)
        tmp.copyTo(q2)
    }

}