package actions

import models.ImageMatrix
import models.Pixel
import org.opencv.core.Core
import org.opencv.core.Core.flip
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RotationController {
    //Espejo Horizontal
    fun mirrorH(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        flip(imageMatrix.image, newImage.image, 1)
        return newImage;
    }
    //Espejo Vertical
    fun mirrorV(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        flip(imageMatrix.image, newImage.image, 0)
        return newImage;
    }
    //Rotacion
    fun rotation(imageMatrix: ImageMatrix, angle: Double): ImageMatrix {
        val src = imageMatrix.image
        val width = src.cols()
        val height = src.rows()
        // 1. Calcular el centro de la imagen original
        val center = Point(width / 2.0, height / 2.0)
        // 2. Obtener la matriz de rotación (2x3)
        // El '1.0' es la escala (sin zoom)
        val rotationMat = Imgproc.getRotationMatrix2D(center, angle, 1.0)
        // 3. CALCULO TRIGONOMÉTRICO PARA EL NUEVO TAMAÑO (Para que no se corte)
        // Obtenemos el seno y coseno absolutos de la matriz
        val absCos = abs(rotationMat.get(0, 0)[0])
        val absSin = abs(rotationMat.get(0, 1)[0])
        // Nueva anchura y altura del lienzo
        val newWidth = (height * absSin + width * absCos).toInt()
        val newHeight = (height * absCos + width * absSin).toInt()
        // 4. Ajustar el centro de rotación al nuevo centro del lienzo
        // (Si no hacemos esto, la imagen rota pero se desplaza fuera de vista)
        val oldCenterX = center.x
        val oldCenterY = center.y
        val newCenterX = newWidth / 2.0
        val newCenterY = newHeight / 2.0
        // Modificamos la columna de traslación de la matriz (índice 2)
        rotationMat.put(0, 2, rotationMat.get(0, 2)[0] + (newCenterX - oldCenterX))
        rotationMat.put(1, 2, rotationMat.get(1, 2)[0] + (newCenterY - oldCenterY))
        // 5. Aplicar la transformación (warpAffine)
        val dest = Mat()
        Imgproc.warpAffine(
            src,
            dest,
            rotationMat,
            Size(newWidth.toDouble(), newHeight.toDouble()),
            Imgproc.INTER_LANCZOS4, // Interpolación (Linear es rápido y decente)
            Core.BORDER_CONSTANT, // Rellenar espacio vacío
            Scalar(0.0, 0.0, 0.0, 0.0) // Color de fondo (Transparente/Negro)
        )
        // Liberar la matriz de transformación para no ocupar memoria
        rotationMat.release()
        return ImageMatrix(dest)
    }
    //Rotacion 180
    fun rotation180(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        flip(imageMatrix.image, newImage.image, -1)
        return newImage;
    }
}