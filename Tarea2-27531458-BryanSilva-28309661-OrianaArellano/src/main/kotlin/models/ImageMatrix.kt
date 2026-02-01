package models

import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import org.opencv.imgproc.Imgproc
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.StreamTokenizer
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs;

class ImageMatrix {
    var image: Mat
    constructor(content: Mat){
        image = content
    }
    constructor(file: File){
        image = Imgcodecs.imread(file.absolutePath, Imgcodecs.IMREAD_UNCHANGED)
        if (image.empty()) {
            throw Exception("No se pudo cargar la imagen: ${file.absolutePath}")
        }
    }

    /*private fun loadImageFromRLE(file: File){
        val width = image.width()
        val height = image.height()
        val reader = BufferedReader(FileReader(file))
        val tokenizer = StreamTokenizer(reader)
        tokenizer.commentChar('#'.code)

        fun nextInt(): Int {
            if (tokenizer.nextToken() == StreamTokenizer.TT_EOF)
                throw Exception("Archivo incompleto")
            return tokenizer.nval.toInt()
        }

        fun nextString(): String {
            if (tokenizer.nextToken() == StreamTokenizer.TT_EOF)
                throw Exception("Archivo incompleto")
            return tokenizer.sval ?: ""
        }

        val header = nextString()
        if(header !in listOf("P1","P2","P3")){
            throw Exception("Codificación RLE no soportada")
        }
        image.reshape(nextInt(), nextInt())

        val matrix = Array(height) { Array(width) { Pixel(0,0,0) } }
        var x = 0
        var y = 0
        var filled = 0
        val totalPixels = width * height

        while(filled < totalPixels){
            when(header){
                "P1" -> {
                    val value = nextInt()
                    val count = nextInt()
                    val pixel = if (value == 0) Pixel(0, 0, 0) else Pixel(255, 255, 255)

                    repeat(count){
                        if (filled >= totalPixels) throw Exception("Archivo excede dimensiones declaradas")
                        matrix[y][x] = pixel
                        x++; filled++
                        if (x >= width) { x = 0; y++ }
                    }
                }
                "P2" -> {
                    val gray = nextInt()
                    val count = nextInt()
                    val pixelVal = (gray*255)/maxVal
                    repeat(count){
                        if(filled >= totalPixels) throw Exception("Archivo excede dimensiones declaradas")
                        matrix[y][x] = Pixel(pixelVal,pixelVal,pixelVal)
                        x++; filled++
                        if(x >= width){ x = 0; y++ }
                    }
                }
                "P3" -> {
                    val r = nextInt()
                    val g = nextInt()
                    val b = nextInt()
                    val count = nextInt()
                    repeat(count){
                        if(filled >= totalPixels) throw Exception("Archivo excede dimensiones declaradas")
                        matrix[y][x] = Pixel(r,g,b)
                        x++; filled++
                        if(x >= width){ x = 0; y++ }
                    }
                }
            }
        }

        reader.close()
        pixels = matrix
    }*/
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