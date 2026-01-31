package controllers

import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import models.ImageMatrix
import java.awt.image.BufferedImage
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.Stack
import javax.imageio.ImageIO

class ImageStateController {
    private val maxHistorySize = 5
    //Pilas de Control de Version
    private var undoStack: Stack<ImageMatrix>
    private var redoStack: Stack<ImageMatrix>

    //Referencias a Información Externa
    private var stage: Stage
    private var dataLabel: Label
    private var imageView: ImageView

    //Referencias a los controladores de Gráficos e Información
    private var chartController: ChartStateController
    private var dataController: DataStateController

    //Imagen Inicial
    private lateinit var matrixImageOriginal: ImageMatrix

    constructor(stage: Stage, label: Label, view: ImageView,
                chartController: ChartStateController, dataController: DataStateController) {
        this.stage = stage
        this.dataLabel = label
        this.imageView = view
        this.chartController = chartController
        this.dataController = dataController
        this.undoStack = Stack()
        this.redoStack = Stack()
    }
    fun loadNewImage(): ImageMatrix?{
        val fileChooser = FileChooser().apply{
            title = "Selecionar Imagen"
            extensionFilters.add(FileChooser.ExtensionFilter(
                "Imagen",
                "*.png", "*.ppm", "*.pgm", "*.pbm", "*.bmp", "*.rle"))
            initialDirectory = File(System.getProperty("user.dir")+"/imagesTest")
        }
        val file: File? = fileChooser.showOpenDialog(stage)
        if (file == null) {
            dataLabel.text = "No se selecciono imagen"
            return null
        }
        if (!file.exists() or ((file.extension.lowercase()) !in setOf("png", "ppm", "pgm", "pbm", "bmp", "rle"))) {
            dataLabel.text = "Imagen Invalida"
            return null
        }
        dataLabel.text = "Imagen Cargada... ${file.name}"
        undoStack.clear()
        redoStack.clear()
        val matrixImage =  ImageMatrix(file)
        matrixImageOriginal = matrixImage.copy()
        changeView(matrixImage)
        return matrixImage
    }
    fun changeView(imageMatrix: ImageMatrix){
        imageView.image = imageMatrix.matrixToImage()
        //Crear primeros gráficos
        chartController.updateHistogram(imageMatrix, "R")
        chartController.updateCurve(matrixImageOriginal,imageMatrix, "R")
        //Crear la información Inicial
        dataController.update(imageMatrix)
    }
    fun changeOriginalRotatedOrZoom(imageMatrix: ImageMatrix){
        matrixImageOriginal = imageMatrix.copy()
    }
    fun saveToHistory(imageState: ImageMatrix) {
        while (undoStack.size >= maxHistorySize) {
            undoStack.removeAt(0)
        }
        undoStack.push(imageState.copy())
        redoStack.clear()
    }
    fun undo(imageMatrix: ImageMatrix): ImageMatrix{
        if (undoStack.isEmpty()) {
            dataLabel.text = "No hay más acciones para deshacer"
            return imageMatrix
        }
        redoStack.push(imageMatrix.copy())
        val image = undoStack.pop()
        changeView(image)
        return image
    }
    fun redo(imageMatrix: ImageMatrix): ImageMatrix{
        if (redoStack.isEmpty()) {
            dataLabel.text = "No hay más acciones para deshacer"
            return imageMatrix
        }
        undoStack.push(imageMatrix.copy())
        val image = redoStack.pop()
        changeView(image)
        return image
    }
    fun downloadImageNetpbm(imageMatrix: ImageMatrix){
        val fileChooser = FileChooser()
        fileChooser.title = "Guardar Imagen"
        fileChooser.initialFileName = "imagen_editada"
        fileChooser.apply {
            initialDirectory = File(System.getProperty("user.dir")+"/imagesTest")
        }
        when (imageMatrix.header) {
            "P1" -> fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("NetPBM Bitmap (P1)", "*.pbm"))
            "P2" -> fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("NetPBM Graymap (P2)", "*.pgm"))
            "P3", "PNG/BMP" -> fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("NetPBM Pixmap (P3)", "*.ppm"))
        }
        val file = fileChooser.showSaveDialog(stage)?: return
        try {
            when (imageMatrix.header) {
                "P1" -> saveAsPBM(imageMatrix, file)
                "P2" -> saveAsPGM(imageMatrix, file)
                "P3", "PNG/BMP" -> saveAsPPM(imageMatrix, file)
            }
            dataLabel.text = "Guardado NetPBM exitoso: ${file.name}"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar: ${e.message}"
            e.printStackTrace()
        }
    }
    fun downloadImagePNG(imageMatrix: ImageMatrix){
        val fileChooser = FileChooser()
        fileChooser.title = "Guardar Imagen"
        fileChooser.initialFileName = "imagen_editada"
        fileChooser.apply {
            initialDirectory = File(System.getProperty("user.dir")+"/imagesTest")
        }
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("PNG", "*.png"))
        val file = fileChooser.showSaveDialog(stage)?: return
        try {
            saveAsStandardImage(imageMatrix, file)
            dataLabel.text = "Guardado PNG exitoso: ${file.name}"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar: ${e.message}"
        }
    }
    fun downloadImagebmp(imageMatrix: ImageMatrix){
        val fileChooser = FileChooser()
        fileChooser.title = "Guardar Imagen"
        fileChooser.initialFileName = "imagen_editada"
        fileChooser.apply {
            initialDirectory = File(System.getProperty("user.dir")+"/imagesTest")
        }
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("BMP", "*.bmp"))
        val file = fileChooser.showSaveDialog(stage)?: return
        try {
            saveAsStandardImage(imageMatrix, file)
            dataLabel.text = "Guardado BMP exitoso: ${file.name}"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar: ${e.message}"
        }
    }
    fun downloadImageRLE(imageMatrix: ImageMatrix) {
        val fileChooser = FileChooser().apply {
            title = "Guardar comprimido RLE"
            initialFileName = "imagen_comprimida.rle"
            extensionFilters.add(FileChooser.ExtensionFilter("Run Length Encoding", "*.rle"))
            initialDirectory = File(System.getProperty("user.dir")+"/imagesTest")
        }
        val file = fileChooser.showSaveDialog(stage) ?: return
        try {
            saveAsRLE(imageMatrix, file)
            dataLabel.text = "Guardado RLE exitoso: ${file.name}"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar RLE: ${e.message}"
            e.printStackTrace()
        }
    }
    //Guardar como P1
    private fun saveAsPBM(matrix: ImageMatrix, file: File) {
        val writer = BufferedWriter(FileWriter(file))
        writer.write("P1\n")
        writer.write("# Creado por ImageEditor\n")
        writer.write("${matrix.width} ${matrix.height}\n")
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                val p = matrix.pixels[y][x]
                val value = if (p.r > 127) "0" else "1"
                writer.write("$value ")
            }
            writer.write("\n")
        }
        writer.close()
    }
    //Guardar como P2
    private fun saveAsPGM(matrix: ImageMatrix, file: File) {
        val writer = BufferedWriter(FileWriter(file))
        // Header P2
        writer.write("P2\n")
        writer.write("# Creado por ImageEditor\n")
        writer.write("${matrix.width} ${matrix.height}\n")
        writer.write("${matrix.maxVal}\n")
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                val p = matrix.pixels[y][x]
                val gray = p.r
                writer.write("$gray ")
            }
            writer.write("\n")
        }
        writer.close()
    }
    //Guardar como P3
    private fun saveAsPPM(matrix: ImageMatrix, file: File) {
        val writer = BufferedWriter(FileWriter(file))
        writer.write("P3\n")
        writer.write("# Creado por ImageEditor\n")
        writer.write("${matrix.width} ${matrix.height}\n")
        writer.write("255\n") // Asumimos 255 siempre para exportación estándar
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                val p = matrix.pixels[y][x]
                writer.write("${p.r} ${p.g} ${p.b}  ")
            }
            writer.write("\n")
        }
        writer.close()
    }
    //Guardar como PNG o BMP
    private fun saveAsStandardImage(matrix: ImageMatrix, file: File) {
        val width = matrix.width
        val height = matrix.height
        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val p = matrix.pixels[y][x]
                val rgb = (p.r shl 16) or (p.g shl 8) or p.b
                bufferedImage.setRGB(x, y, rgb)
            }
        }
        val ext = file.extension.ifEmpty { "bmp" }
        ImageIO.write(bufferedImage, ext, file)
    }
    //Guardar como RLE
    private fun saveAsRLE(matrix: ImageMatrix, file: File) {
        val writer = BufferedWriter(FileWriter(file))
        val format = when (matrix.header) {
            "P1" -> "P1"
            "P2" -> "P2"
            else -> "P3"
        }
        writer.write("$format\n")
        writer.write("# RLE Compressed by ImageEditor\n")
        val maxValOut = if (format == "P1") 1 else 255
        writer.write("${matrix.width} ${matrix.height}\n")
        writer.write("$maxValOut\n")
        var count = 0
        var lastR = -1
        var lastG = -1
        var lastB = -1
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                val p = matrix.pixels[y][x]
                val currentR: Int
                val currentG: Int
                val currentB: Int
                when (format) {
                    "P1" -> {
                        val binary = if (p.r < 128) 0 else 1
                        currentR = binary; currentG = 0; currentB = 0
                    }
                    "P2" -> {
                        currentR = p.r; currentG = 0; currentB = 0
                    }
                    else -> {
                        currentR = p.r; currentG = p.g; currentB = p.b
                    }
                }
                if (count == 0) {
                    lastR = currentR
                    lastG = currentG
                    lastB = currentB
                    count = 1
                } else {
                    val areEqual = when(format) {
                        "P3" -> (currentR == lastR && currentG == lastG && currentB == lastB)
                        else -> (currentR == lastR)
                    }
                    if (areEqual) {
                        count++
                    } else {
                        writeRLEGroup(writer, format, lastR, lastG, lastB, count)
                        lastR = currentR
                        lastG = currentG
                        lastB = currentB
                        count = 1
                    }
                }
            }
        }
        if (count > 0) {
            writeRLEGroup(writer, format, lastR, lastG, lastB, count)
        }
        writer.close()
    }
    private fun writeRLEGroup(writer: BufferedWriter, format: String, r: Int, g: Int, b: Int, count: Int) {
        when (format) {
            "P1" -> {
                writer.write("$r $count\n")
            }
            "P2" -> {
                writer.write("$r $count\n")
            }
            "P3" -> {
                writer.write("$r $g $b $count\n")
            }
        }
    }
}