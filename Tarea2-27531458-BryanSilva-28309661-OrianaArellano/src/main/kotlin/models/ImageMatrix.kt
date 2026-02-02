package models

import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import org.opencv.core.CvType
import org.opencv.imgproc.Imgproc
import java.io.File
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs;
import java.io.BufferedReader
import java.io.FileReader
import java.io.StreamTokenizer

class ImageMatrix {
    var image: Mat
    constructor(content: Mat){
        image = content
    }
    constructor(file: File){
        if (file.extension.equals("rle", ignoreCase = true)) {
            image = loadFromRLE(file)
        } else {
            image = Imgcodecs.imread(file.absolutePath, Imgcodecs.IMREAD_UNCHANGED)
        }
        if (image.empty()) {
            throw Exception("No se pudo cargar la imagen: ${file.absolutePath}")
        }
    }
    private fun loadFromRLE(file: File): Mat {
        val reader = BufferedReader(FileReader(file))
        val tokenizer = StreamTokenizer(reader)
        tokenizer.commentChar('#'.code)
        fun nextToken(): Int {
            val token = tokenizer.nextToken()
            if (token == StreamTokenizer.TT_EOF) throw Exception("Archivo incompleto (EOF inesperado)")
            return token
        }
        fun nextString(): String {
            nextToken()
            return tokenizer.sval ?: ""
        }
        fun nextInt(): Int {
            nextToken()
            return tokenizer.nval.toInt()
        }
        val header = nextString()
        if (header !in listOf("P1", "P2", "P3")) {
            reader.close()
            throw Exception("Formato RLE no soportado: $header")
        }
        val width = nextInt()
        val height = nextInt()
        val maxVal = if (header != "P1") nextInt() else 1
        val totalPixels = width * height
        var filledPixels = 0
        val channels = if (header == "P3") 3 else 1
        val type = if (header == "P3") CvType.CV_8UC3 else CvType.CV_8UC1
        val buffer = ByteArray(totalPixels * channels)
        var bufferIdx = 0
        try {
            while (filledPixels < totalPixels) {
                when (header) {
                    "P1" -> {
                        val value = nextInt()
                        val count = nextInt()
                        val pixelByte = (if (value == 0) 0 else 255).toByte()
                        repeat(count) {
                            if (bufferIdx < buffer.size) {
                                buffer[bufferIdx++] = pixelByte
                                filledPixels++
                            }
                        }
                    }
                    "P2" -> {
                        val gray = nextInt()
                        val count = nextInt()
                        val pixelVal = ((gray * 255) / maxVal).toByte()
                        repeat(count) {
                            if (bufferIdx < buffer.size) {
                                buffer[bufferIdx++] = pixelVal
                                filledPixels++
                            }
                        }
                    }
                    "P3" -> {
                        val r = nextInt()
                        val g = nextInt()
                        val b = nextInt()
                        val count = nextInt()
                        repeat(count) {
                            if (bufferIdx < buffer.size - 2) {
                                buffer[bufferIdx++] = b.toByte()
                                buffer[bufferIdx++] = g.toByte()
                                buffer[bufferIdx++] = r.toByte()
                                filledPixels++
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            reader.close()
            throw Exception("Error parseando datos RLE: ${e.message}")
        }
        reader.close()
        val mat = Mat(height, width, type)
        mat.put(0, 0, buffer)
        return mat
    }
    fun matrixToImage(): Image{
        val width = image.width()
        val height = image.height()
        val channels = image.channels()
        val outputImage = WritableImage(width, height)
        val writer = outputImage.pixelWriter
        val format = PixelFormat.getByteBgraInstance()
        val bufferSize = width * height * 4
        val buffer = ByteArray(bufferSize)
        val tempMat = Mat()
        when (channels) {
            1 -> {
                //Un canal
                Imgproc.cvtColor(image, tempMat, Imgproc.COLOR_GRAY2BGRA)
                tempMat.get(0, 0, buffer)
            }
            3 -> {
                //3 Canales
                Imgproc.cvtColor(image, tempMat, Imgproc.COLOR_BGR2BGRA)
                tempMat.get(0, 0, buffer)
            }
            4 -> {
                //4 Canales
                image.get(0, 0, buffer)
            }
        }
        writer.setPixels(0, 0, width, height, format, buffer, 0, width * 4)
        tempMat.release()
        return outputImage
    };
    fun copy() : ImageMatrix{
        val newMatrix = ImageMatrix(image.clone())
        return newMatrix
    }
}