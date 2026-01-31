package models

import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.StreamTokenizer

class ImageMatrix {
    var width = 0
    var height = 0
    var maxVal = 0
    var header = ""
    lateinit var pixels: Array<Array<Pixel>>

    constructor(width: Int, height: Int) {
        this.width = width
        this.height = height
        this.pixels = Array(height) { Array(width) { Pixel(0,0,0) } }
    }
    constructor(file: File){
        var ext = file.extension
        when(ext.lowercase()){
            "png" -> loadImageFromPNG(file)
            "bmp" -> loadImageFromPNG(file)
            "pgm" -> loadImageFromPGM(file)
            "pbm" -> loadImageFromPBM(file)
            "ppm" -> loadImageFromPPM(file)
            "rle" -> loadImageFromRLE(file)
            else -> throw IllegalArgumentException("Extensión no soportada: $ext")
        }
    }
    private fun loadImageFromPNG(file: File){
        val image = Image(file.toURI().toString())
        width = image.width.toInt()
        height = image.height.toInt()
        maxVal = 255
        header = "PNG/BMP"
        val pixelReader = image.pixelReader
        val matrix = Array(height) { Array(width) { Pixel(0,0,0) } }
        for(y in 0 until height){
            for(x in 0 until width){
                val argb = pixelReader.getArgb(x, y)
                val pixelR = (argb shr 16) and 0xFF
                val pixelG = (argb shr 8) and 0xFF
                val pixelB = argb and 0xFF
                matrix[y][x] = Pixel(pixelR, pixelG, pixelB)
            }
        }
        pixels = matrix
    }
    private fun loadImageFromPGM(file: File){
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
        header = nextString()
        if(header != "P2"){
            throw Exception("Codificacion no Soportada")
        }
        width = nextInt()
        height = nextInt()
        maxVal = nextInt()
        val matrix = Array(height) { Array(width) { Pixel(0,0,0) } }
        for(y in 0 until height){
            for(x in 0 until width){
                val pixel = (nextInt()*255)/maxVal
                matrix[y][x] = Pixel(pixel, pixel, pixel)
            }
        }
        reader.close()
        pixels = matrix
    }
    private fun loadImageFromPBM(file: File){
        val reader = BufferedReader(FileReader(file))
        val tokenizer = StreamTokenizer(reader)
        maxVal = 1
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
        header = nextString()
        if(header != "P1"){
            throw Exception("Codificacion no Soportada")
        }
        width = nextInt()
        height = nextInt()
        val matrix = Array(height) { Array(width) { Pixel(0,0,0) } }
        for(y in 0 until height){
            for(x in 0 until width){
                val pixelR = nextInt()
                if (pixelR == 0){
                    matrix[y][x] = Pixel(255,255,255)
                }else{
                    matrix[y][x] = Pixel(0,0,0)
                }
            }
        }
        reader.close()
        pixels = matrix
    }
    private fun loadImageFromPPM(file: File){
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
        header = nextString()
        if(header != "P3"){
            throw Exception("Codificacion no Soportada")
        }
        width = nextInt()
        height = nextInt()
        maxVal = nextInt()
        val matrix = Array(height) { Array(width) { Pixel(0,0,0) } }
        for(y in 0 until height){
            for(x in 0 until width){
                val pixelR = nextInt()
                val pixelG = nextInt()
                val pixelB = nextInt()
                matrix[y][x] = Pixel(pixelR, pixelG, pixelB)
            }
        }
        reader.close()
        pixels = matrix
    }
    private fun loadImageFromRLE(file: File){
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

        width = nextInt()
        height = nextInt()
        maxVal = nextInt()

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
    }
    fun matrixToImage(): Image{
        val outputImage = WritableImage(width, height)
        val writer = outputImage.pixelWriter
        for(y in 0 until height){
            for(x in 0 until width){
                val pixel = pixels[y][x]
                val argb = (0xFF shl 24) or (pixel.r shl 16) or (pixel.g shl 8) or pixel.b
                writer.setArgb(x, y, argb)
            }
        }
        return outputImage
    };
    fun print(){
        println("${width}x${height}")
        println(maxVal)
        for (row in 0 until height) {
            for (col in 0 until width) {
                print("${pixels[row][col].r} ")
            }
            println()
        }
    }
    fun copy(): ImageMatrix{
        val matrixCopy = ImageMatrix(width, height)
        matrixCopy.pixels = Array(height) {
            y -> Array(width) {
                x -> val p = pixels[y][x]
                Pixel(p.r, p.g, p.b)
            }
        }
        matrixCopy.maxVal = maxVal
        matrixCopy.header = header
        return matrixCopy
    }
    operator fun get(y: Int, x: Int): Double {
        val pixel = pixels[y][x]
        // Puedes usar solo el canal R si estás trabajando en escala de grises
        return pixel.r.toDouble()
    }

    operator fun set(y: Int, x: Int, value: Double) {
        val v = value.toInt().coerceIn(0, 255)
        pixels[y][x] = Pixel(v, v, v)
    }

}