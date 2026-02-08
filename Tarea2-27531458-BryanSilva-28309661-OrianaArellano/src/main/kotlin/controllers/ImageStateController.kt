package controllers

import actions.PanningController
import actions.RotationController
import actions.ZoomController
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import models.ImageMatrix
import org.opencv.imgcodecs.Imgcodecs
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.Stack
import kotlin.math.abs

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

    //Referencia a Controladores de Transformacion
    private var zoomController: ZoomController
    private var rotationController: RotationController
    private var panningController: PanningController

    //Imagen Inicial
    private lateinit var matrixImageOriginal: ImageMatrix

    //Zoom data
    private var currentZoomLevel: Int = 0
    private var currentZoomMethod: String = "NONE"

    constructor(stage: Stage, label: Label, view: ImageView,
                chartController: ChartStateController, dataController: DataStateController,
                zoomController: ZoomController, rotationController: RotationController,
                panningController: PanningController) {
        this.stage = stage
        this.dataLabel = label
        this.imageView = view
        this.chartController = chartController
        this.dataController = dataController
        this.undoStack = Stack()
        this.redoStack = Stack()
        this.zoomController = zoomController
        this.rotationController = rotationController
        this.panningController = panningController
    }
    fun loadNewImage(): ImageMatrix?{
        val fileChooser = FileChooser().apply{
            title = "Selecionar Imagen"
            extensionFilters.add(FileChooser.ExtensionFilter(
                "Imagen",
                "*.png", "*.ppm", "*.pgm", "*.pbm", "*.bmp", "*.jpg", "*.rle"))
            initialDirectory = File(System.getProperty("user.dir")+"/imagesTest")
        }
        val file: File? = fileChooser.showOpenDialog(stage)
        if (file == null) {
            dataLabel.text = "No se selecciono imagen"
            return null
        }
        if (!file.exists() or ((file.extension.lowercase()) !in setOf("png", "ppm", "pgm", "pbm", "bmp", "jpg", "rle"))) {
            dataLabel.text = "Imagen Invalida"
            return null
        }
        undoStack.clear()
        redoStack.clear()
        val matrixImage =  ImageMatrix(file)
        matrixImageOriginal = matrixImage.copy()
        currentZoomLevel = 0
        currentZoomMethod = "NONE"
        changeView(matrixImage)
        dataLabel.text = "Imagen Cargada... ${file.name}"
        return matrixImage
    }
    //Update Zoom Data
    fun updateZoom(czl: Int, czm: String) {
        this.currentZoomLevel = czl
        this.currentZoomMethod = czm
    }
    //Transformation Pipeline
    fun transform(imageMatrix: ImageMatrix, download: Boolean): ImageMatrix{
        var imageToShow: ImageMatrix = imageMatrix.copy()
        //Calculo de Panning
        if(imageToShow.currentPanningLevelX0 != 0.0 || imageToShow.currentPanningLevelY0 != 0.0 ||
            imageToShow.currentPanningLevelX1 != imageToShow.image.width().toDouble() ||
            imageToShow.currentPanningLevelY1 != imageToShow.image.height().toDouble()) {
            imageToShow = when (imageMatrix.currentPanningMethod) {
                "EX" -> panningController.panningNOEX(imageToShow, imageToShow.currentPanningLevelX0,
                    imageToShow.currentPanningLevelY0, imageToShow.currentPanningLevelX1,
                    imageToShow.currentPanningLevelY1)
                "NOEX" -> panningController.panningNOEX(imageToShow, imageToShow.currentPanningLevelX0,
                    imageToShow.currentPanningLevelY0, imageToShow.currentPanningLevelX1,
                    imageToShow.currentPanningLevelY1)
                else -> imageMatrix
            }
        }
        //Calculo de Rotacion
        if(imageToShow.currentRotationLevel != 0.0) {
            imageToShow = when(imageToShow.currentRotationMethod) {
                "EX" -> rotationController.rotationEX(imageToShow, imageToShow.currentRotationLevel)
                "NOEX" -> rotationController.rotationNoEX(imageToShow, imageToShow.currentRotationLevel)
                else -> imageToShow
            }
        }
        if(!download) {
            //Calculo de Zoom
            if (currentZoomLevel != 0) {
                val currentZoomLevelAbs = abs(currentZoomLevel)
                imageToShow = when(currentZoomMethod) {
                    "INN" -> zoomController.zoomINN(imageToShow, currentZoomLevelAbs)
                    "inBLI" -> zoomController.zoomInBLI(imageToShow, currentZoomLevelAbs)
                    "OutN" -> zoomController.zoomOutN(imageToShow, currentZoomLevelAbs)
                    "OutSuperS" -> zoomController.zoomOutSupersampling(imageToShow, currentZoomLevelAbs)
                    else -> imageToShow
                }
            }
        }
        return imageToShow
    }
    fun changeView(imageMatrix: ImageMatrix){
        val imageToShow: ImageMatrix = transform(imageMatrix, false)
        //Imagen
        imageView.image = imageToShow.matrixToImage()
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
        val imageToDown: ImageMatrix = transform(imageMatrix, true)
        val fileChooser = FileChooser()
        fileChooser.title = "Guardar Imagen NetPBM"
        fileChooser.initialFileName = "imagen_editada"
        fileChooser.initialDirectory = File(System.getProperty("user.dir") + "/imagesTest")
        val channels = imageToDown.image.channels()
        if (channels == 1) {
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("NetPBM Graymap (P2)", "*.pgm"))
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("NetPBM Bitmap (P1)", "*.pbm"))
        } else {
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("NetPBM Pixmap (P3)", "*.ppm"))
        }
        val file = fileChooser.showSaveDialog(stage) ?: return
        try {
            val ext = file.extension.lowercase()
            when (ext) {
                "pbm" -> saveAsPBM(imageToDown, file)
                "pgm" -> saveAsPGM(imageToDown, file)
                "ppm" -> saveAsPPM(imageToDown, file)
                else -> saveAsPPM(imageToDown, file)
            }
            dataLabel.text = "Guardado NetPBM exitoso: ${file.name}"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar: ${e.message}"
            e.printStackTrace()
        }
    }
    fun downloadImagePNG(imageMatrix: ImageMatrix) = saveStandard(imageMatrix, "png")
    fun downloadImageJPG(imageMatrix: ImageMatrix) = saveStandard(imageMatrix, "jpg")
    fun downloadImagebmp(imageMatrix: ImageMatrix) = saveStandard(imageMatrix, "bmp")
    private fun saveStandard(imageMatrix: ImageMatrix, ext: String) {
        val imageToDown: ImageMatrix = transform(imageMatrix, true)
        val fileChooser = FileChooser()
        fileChooser.title = "Guardar Imagen $ext"
        fileChooser.initialFileName = "imagen_editada.$ext"
        fileChooser.initialDirectory = File(System.getProperty("user.dir") + "/imagesTest")
        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter(ext.uppercase(), "*.$ext"))
        val file = fileChooser.showSaveDialog(stage) ?: return
        try {
            val success = Imgcodecs.imwrite(file.absolutePath, imageToDown.image)
            if (success) dataLabel.text = "Guardado $ext exitoso: ${file.name}"
            else dataLabel.text = "Error interno de OpenCV al guardar"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar: ${e.message}"
        }
    }
    fun downloadImageRLE(imageMatrix: ImageMatrix) {
        val imageToDown: ImageMatrix = transform(imageMatrix, true)
        val fileChooser = FileChooser().apply {
            title = "Guardar comprimido RLE"
            initialFileName = "imagen_comprimida.rle"
            extensionFilters.add(FileChooser.ExtensionFilter("Run Length Encoding", "*.rle"))
            initialDirectory = File(System.getProperty("user.dir") + "/imagesTest")
        }
        val file = fileChooser.showSaveDialog(stage) ?: return
        try {
            saveAsRLE(imageToDown, file)
            dataLabel.text = "Guardado RLE exitoso: ${file.name}"
        } catch (e: Exception) {
            dataLabel.text = "Error al guardar RLE: ${e.message}"
            e.printStackTrace()
        }
    }
    private fun saveAsPBM(matrix: ImageMatrix, file: File) {
        val mat = matrix.image
        val width = mat.cols()
        val height = mat.rows()
        val buffer = ByteArray(width * height * mat.channels())
        mat.get(0, 0, buffer)
        val writer = BufferedWriter(FileWriter(file))
        writer.write("P1\n")
        writer.write("$width $height\n")
        var idx = 0
        val step = mat.channels()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val grayVal = buffer[idx].toInt() and 0xFF
                val bit = if (grayVal > 127) "0" else "1"
                writer.write("$bit ")
                idx += step
            }
            writer.write("\n")
        }
        writer.close()
    }
    private fun saveAsPGM(matrix: ImageMatrix, file: File) {
        val mat = matrix.image
        val width = mat.cols()
        val height = mat.rows()
        val channels = mat.channels()
        val buffer = ByteArray(width * height * channels)
        mat.get(0, 0, buffer)
        val writer = BufferedWriter(FileWriter(file))
        writer.write("P2\n")
        writer.write("$width $height\n")
        writer.write("255\n")
        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val valGray = buffer[idx].toInt() and 0xFF
                writer.write("$valGray ")
                idx += channels
            }
            writer.write("\n")
        }
        writer.close()
    }
    private fun saveAsPPM(matrix: ImageMatrix, file: File) {
        val mat = matrix.image
        val width = mat.cols()
        val height = mat.rows()
        val channels = mat.channels()
        if (channels < 3) {
            throw Exception("Intentando guardar imagen de 1 canal como PPM (Color)")
        }
        val buffer = ByteArray(width * height * channels)
        mat.get(0, 0, buffer)
        val writer = BufferedWriter(FileWriter(file))
        writer.write("P3\n")
        writer.write("$width $height\n")
        writer.write("255\n")
        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val b = buffer[idx].toInt() and 0xFF
                val g = buffer[idx + 1].toInt() and 0xFF
                val r = buffer[idx + 2].toInt() and 0xFF
                writer.write("$r $g $b  ")
                idx += channels
            }
            writer.write("\n")
        }
        writer.close()
    }
    private fun saveAsRLE(matrix: ImageMatrix, file: File) {
        val mat = matrix.image
        val width = mat.cols()
        val height = mat.rows()
        val channels = mat.channels()
        val format = if (channels >= 3) "P3" else "P2"
        val writer = BufferedWriter(FileWriter(file))
        writer.write("$format\n")
        writer.write("$width $height\n")
        writer.write("255\n")
        val buffer = ByteArray(width * height * channels)
        mat.get(0, 0, buffer)
        var idx = 0
        var count = 0
        var lastR = -1
        var lastG = -1
        var lastB = -1
        val totalPixels = width * height
        for (i in 0 until totalPixels) {
            var currR = 0
            var currG = 0
            var currB = 0
            if (channels >= 3) {
                val b = buffer[idx].toInt() and 0xFF
                val g = buffer[idx + 1].toInt() and 0xFF
                val r = buffer[idx + 2].toInt() and 0xFF
                currR = r; currG = g; currB = b
            } else {
                val v = buffer[idx].toInt() and 0xFF
                currR = v; currG = 0; currB = 0
            }
            if (i == 0) {
                lastR = currR; lastG = currG; lastB = currB
                count = 1
            } else {
                val areEqual = if (format == "P3") {
                    (currR == lastR && currG == lastG && currB == lastB)
                } else {
                    (currR == lastR)
                }
                if (areEqual) {
                    count++
                } else {
                    writeRLEGroup(writer, format, lastR, lastG, lastB, count)
                    lastR = currR; lastG = currG; lastB = currB
                    count = 1
                }
            }
            idx += channels
        }
        if (count > 0) {
            writeRLEGroup(writer, format, lastR, lastG, lastB, count)
        }
        writer.close()
    }
    private fun writeRLEGroup(writer: BufferedWriter, format: String, r: Int, g: Int, b: Int, count: Int) {
        if (format == "P3") {
            writer.write("$r $g $b $count\n")
        } else {
            writer.write("$r $count\n")
        }
    }
}