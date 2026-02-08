package actions

import models.ImageMatrix
import org.opencv.core.Rect
import kotlin.math.max
import kotlin.math.min

class PanningController {
    fun panningNOEX(imageMatrix: ImageMatrix, x1: Double, y1: Double, x2: Double, y2: Double): ImageMatrix {
        val mat = imageMatrix.image
        val imgW = mat.cols()
        val imgH = mat.rows()

        // 1. Normalización: Asegurar que (x1,y1) sea Top-Left y (x2,y2) sea Bottom-Right
        // Esto previene errores si los puntos se cruzan accidentalmente.
        var startX = min(x1, x2)
        var startY = min(y1, y2)
        var endX = max(x1, x2)
        var endY = max(y1, y2)

        // 2. Clamping (Recorte de coordenadas):
        // Los puntos no pueden ser menores a 0 ni mayores al tamaño de la imagen.
        // Esto es lo que evita que el programa falle si arrastras el mouse fuera de la ventana.
        startX = startX.coerceIn(0.0, imgW.toDouble())
        startY = startY.coerceIn(0.0, imgH.toDouble())
        endX = endX.coerceIn(0.0, imgW.toDouble())
        endY = endY.coerceIn(0.0, imgH.toDouble())

        // 3. Calcular Dimensiones del ROI (Region of Interest)
        val width = (endX - startX).toInt()
        val height = (endY - startY).toInt()

        // 4. Validación de Seguridad
        // Si la ventana se cerró por completo (width=0) o la imagen es nula, devolvemos la original.
        if (width <= 0 || height <= 0) {
            return imageMatrix
        }

        // 5. Recorte con OpenCV
        val roi = Rect(startX.toInt(), startY.toInt(), width, height)
        val crop = mat.submat(roi)

        // 6. Clonar para evitar problemas de referencia de memoria
        val result = crop.clone()
        crop.release()

        return ImageMatrix(result, imageMatrix)
    }
}