package controllers

import actions.ConvolutionController
import actions.LigthController
import actions.NoLinearController
import actions.RotationController
import actions.UmbralizerController
import actions.TonoController
import actions.ZoomController
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.chart.AreaChart
import javafx.scene.chart.LineChart
import javafx.scene.control.Accordion
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.control.RadioButton
import javafx.scene.control.Slider
import javafx.scene.control.Spinner
import javafx.scene.control.SpinnerValueFactory
import javafx.scene.control.TextField
import javafx.scene.control.TitledPane
import javafx.scene.control.ToggleGroup
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import javafx.stage.Stage
import models.ImageMatrix
import models.Kernel
import models.Pixel
import java.awt.Desktop
import java.io.File
import kotlin.math.roundToInt

class BasicViewController {

    private lateinit var stage: Stage
    fun setStage(stage: Stage) {
        this.stage = stage
    }
    //Estados Varios
    @FXML
    private lateinit var applicationConsole: Label
    @FXML
    private lateinit var mainImageView: ImageView
    @FXML
    private lateinit var bppImage: Label
    @FXML
    private lateinit var colorsImage: Label
    @FXML
    private lateinit var dimImage: Label

    //Core Controllers
    private lateinit var chartController: ChartStateController
    private lateinit var imageController: ImageStateController
    private lateinit var dataController: DataStateController

    //Action Controllers
    private lateinit var umbralizerController: UmbralizerController
    private lateinit var tonoController: TonoController
    private lateinit var ligthController: LigthController
    private lateinit var rotationController: RotationController
    private lateinit var zoomController: ZoomController
    private lateinit var controllerConvolution: ConvolutionController
    private  lateinit var noLinearController: NoLinearController

    //Graficos
    @FXML
    private lateinit var histogramChart: AreaChart<Number, Number>
    @FXML
    private lateinit var toneCurveChart: LineChart<Number, Number>
    @FXML
    private lateinit var perfilAreaChart: AreaChart<Number, Number>

    //Data
    private var matrixImage: ImageMatrix? = null
    private var originalImage: ImageMatrix? = null
    private var originalGeometryImage: ImageMatrix? = null

    //Estado del canal visualizado en el Histograma
    @FXML
    private lateinit var histogram: ToggleGroup
    @FXML
    fun onRadioHistogramClick(event: ActionEvent) {
        matrixImage?:return
        val channel = (histogram.selectedToggle as RadioButton).text
        chartController.updateHistogram(matrixImage, channel)
    }
    //Estado del canal visualizado en la Curva Tonal
    @FXML
    private lateinit var tonalCurve: ToggleGroup
    @FXML
    fun onRadioCurveClick(event: ActionEvent) {
        matrixImage?:return
        val channel = (tonalCurve.selectedToggle as RadioButton).text
        chartController.updateCurve(originalGeometryImage,matrixImage, channel)
    }
    //Estado del canal visualizado en el Perfil de Imagen
    @FXML
    private lateinit var imagePerfil: ToggleGroup
    @FXML
    private lateinit var perfilText: TextField
    @FXML
    fun onRadioPerfilClick(event: ActionEvent) {
        matrixImage?:return
        val channel = (imagePerfil.selectedToggle as RadioButton).text
        chartController.updatePerfil(matrixImage,perfilText.text.toInt(), channel)
    }
    //Estado del Perfil de la Imagen
    @FXML
    fun onPerfilButtonClick(event: ActionEvent) {
        matrixImage?:return
        chartController.updatePerfil(matrixImage,perfilText.text.toInt(), "R")
    }

    //Leer la imagen y setea el estado inicial de la aplicacion
    @FXML
    fun onLoadImageClick(event: ActionEvent) {
        //Controladores de Imagen, Gráficos e Información
        chartController = ChartStateController(histogramChart, toneCurveChart, perfilAreaChart)
        dataController = DataStateController(dimImage, colorsImage, bppImage)
        imageController = ImageStateController(stage,applicationConsole, mainImageView, chartController, dataController)
        //Controladores de Acción
        umbralizerController = UmbralizerController()
        tonoController = TonoController()
        ligthController = LigthController()
        rotationController = RotationController()
        zoomController = ZoomController()
        controllerConvolution = ConvolutionController()
        noLinearController = NoLinearController()
        //Leer Imagen Inicial y crea una copia
        matrixImage = imageController.loadNewImage()
        matrixImage?: return
        originalImage = matrixImage!!.copy()
        originalGeometryImage = matrixImage!!.copy()
    }
    //Descargar imagenes
    @FXML
    fun onDownLoadNetpbm(event: ActionEvent) {
        matrixImage?:return
        imageController.downloadImageNetpbm(matrixImage!!)
    }
    @FXML
    fun onDownLoadBMP(event: ActionEvent) {
        matrixImage?:return
        imageController.downloadImagebmp(matrixImage!!)
    }
    @FXML
    fun onDownLoadPNG(event: ActionEvent) {
        matrixImage?:return
        imageController.downloadImagePNG(matrixImage!!)
    }
    @FXML
    fun onDownLoadRLE(event: ActionEvent) {
        matrixImage?:return
        imageController.downloadImageRLE(matrixImage!!)
    }
    //Cargar Imagen original
    @FXML
    fun onOriginalButtton(event: ActionEvent) {
        originalImage?:return
        matrixImage = originalImage!!.copy()
        originalGeometryImage = originalImage!!.copy()
        imageController.changeView(matrixImage!!)
    }

    //Control de Undo y Redo
    @FXML
    fun onUndoButtonClick(event: ActionEvent) {
        matrixImage?:return
        matrixImage = imageController.undo(matrixImage!!)
    }
    @FXML
    fun onRedoButtonClick(event: ActionEvent) {
        matrixImage?:return
        matrixImage = imageController.redo(matrixImage!!)
    }

    //Boton About
    @FXML
    fun onAboutClick(event: ActionEvent) {
        val currentDir = File(System.getProperty("user.dir"))
        val parentDir = currentDir.parentFile
        val file = File(parentDir ?: currentDir, "README.md")
        if (file.exists()) {
            try {
                val desktop = Desktop.getDesktop()
                desktop.open(file)
            } catch (e: Exception) {
                e.printStackTrace()
                applicationConsole.text = "No se pudo abrir el archivo..."
            }
        } else {
            applicationConsole.text = "Archivo no encontrado..."
        }
    }
    //Inicializar estado
    @FXML
    private lateinit var dataPanel: TitledPane
    @FXML
    private lateinit var rightAccordion: Accordion
    @FXML
    fun initialize() {
        rightAccordion.expandedPane = dataPanel
        rowsSpinnerSobel.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, 3)
        colsSpinnerSobel.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, 3)

        rowsSpinnerPrewitt.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, 3)
        colsSpinnerPrewitt.valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 7, 3)

        // Validación cruzada para Sobel
        rowsSpinnerSobel.valueProperty().addListener { _, _, newValue ->
            if (newValue == 1 && colsSpinnerSobel.value == 1) {
                colsSpinnerSobel.valueFactory.value = 2
            }
        }
        colsSpinnerSobel.valueProperty().addListener { _, _, newValue ->
            if (newValue == 1 && rowsSpinnerSobel.value == 1) {
                rowsSpinnerSobel.valueFactory.value = 2
            }
        }

        // Validación cruzada para Prewitt
        rowsSpinnerPrewitt.valueProperty().addListener { _, _, newValue ->
            if (newValue == 1 && colsSpinnerPrewitt.value == 1) {
                colsSpinnerPrewitt.valueFactory.value = 2
            }
        }
        colsSpinnerPrewitt.valueProperty().addListener { _, _, newValue ->
            if (newValue == 1 && rowsSpinnerPrewitt.value == 1) {
                rowsSpinnerPrewitt.valueFactory.value = 2
            }
        }
        //Brillo en Tiempo Real
        lightSlider.valueProperty().addListener { _, _, newValue ->
            if (matrixImage != null) {
                val previewImage = matrixImage!!.copy()
                ligthController.brightness(previewImage, newValue.toDouble())
                mainImageView.image = previewImage.matrixToImage()
            }
        }
        //Contraste en Tiempo Real
        contrastSlider.valueProperty().addListener { _, _, newValue ->
            if (matrixImage != null) {
                val previewImage = matrixImage!!.copy()
                ligthController.contrast(previewImage, newValue.toDouble())
                mainImageView.image = previewImage.matrixToImage()
            }
        }
    }

    //Umbral Simple
    @FXML
    private lateinit var umbralSlider: Slider
    @FXML
    fun onUmbralButtonClick(event: ActionEvent){
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        umbralizerController.simpleUmbral(matrixImage!!, umbralSlider.value)
        imageController.changeView(matrixImage!!)
    }
    //Umbral Multiple
    @FXML
    private lateinit var umbralMultInf: TextField
    @FXML
    private lateinit var umbralMultiSup: TextField
    fun onMultiUmbralButtonClick(event: ActionEvent){
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        umbralizerController.multiUmbral(matrixImage!!,
            umbralMultInf.text.toInt(),
            umbralMultiSup.text.toInt())
        imageController.changeView(matrixImage!!)
    }
    //Negativo de la Imagen
    @FXML
    fun onNegativeButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        tonoController.negativeImage(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Escala de Grises
    @FXML
    fun onGreyscaleButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        tonoController.greyScale(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Escala de Color
    @FXML
    private lateinit var colorScalePicker: ColorPicker
    @FXML
    fun onColorScalePickerClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        tonoController.colorScale(matrixImage!!, colorScalePicker)
        imageController.changeView(matrixImage!!)
    }
    //Cambio de Brillo
    @FXML
    private lateinit var  lightSlider: Slider
    @FXML
    fun onBrigthnessButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        ligthController.brightness(matrixImage!!, lightSlider.value)
        imageController.changeView(matrixImage!!)
        lightSlider.value = 0.0
    }
    //Cambio de Contraste
    @FXML
    private lateinit var contrastSlider: Slider
    @FXML
    fun onConstrastButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        ligthController.contrast(matrixImage!!, contrastSlider.value)
        imageController.changeView(matrixImage!!)
        contrastSlider.value = 1.0
    }
    //Espejo Horizontal
    @FXML
    fun onMirrorHClick(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = rotationController.mirrorH(originalGeometryImage!!)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = rotationController.mirrorH(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Espejo Vertical
    @FXML
    fun onMirrorVClick(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = rotationController.mirrorV(originalGeometryImage!!)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = rotationController.mirrorV(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Rotacion 90 grados
    @FXML
    fun onRotation90Click(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = rotationController.rotation90(originalGeometryImage!!)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = rotationController.rotation90(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Rotacion 180 grados
    @FXML
    fun onRotation180Click(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = rotationController.rotation180(originalGeometryImage!!)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = rotationController.rotation180(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Rotacion 270 grados
    @FXML
    fun onRotation270Click(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = rotationController.rotation270(originalGeometryImage!!)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = rotationController.rotation270(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Zoom In
    @FXML
    fun onZoomInNearestClick(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = zoomController.zoomINN(originalGeometryImage!!, 2)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = zoomController.zoomINN(matrixImage!!, 2)
        imageController.changeView(matrixImage!!)
    }
    @FXML
    fun onZoomInBilinearClick(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = zoomController.zoomInBLI(originalGeometryImage!!, 2)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = zoomController.zoomInBLI(matrixImage!!, 2)
        imageController.changeView(matrixImage!!)
    }
    //Zooom Out
    @FXML
    fun onZoomOutNearestClick(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = zoomController.zoomOutN(originalGeometryImage!!, 2)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = zoomController.zoomOutN(matrixImage!!, 2)
        imageController.changeView(matrixImage!!)
    }
    @FXML
    fun onZoomOutSuperSampling(event: ActionEvent) {
        matrixImage?:return
        originalGeometryImage = zoomController.zoomOutSupersampling(originalGeometryImage!!, 2)
        imageController.changeOriginalRotatedOrZoom(originalGeometryImage!!)
        imageController.saveToHistory(matrixImage!!)
        matrixImage = zoomController.zoomOutSupersampling(matrixImage!!, 2)
        imageController.changeView(matrixImage!!)
    }

    //Convoluciones
    @FXML
    fun onCustomKernelButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        val fileChooser = FileChooser().apply{
            title = "Selecionar Imagen"
            extensionFilters.add(FileChooser.ExtensionFilter(
                "Kernel",
                "*.txt"))
            initialDirectory = File(System.getProperty("user.dir") + "/imagesTest")
        }
        val file: File? = fileChooser.showOpenDialog(stage)
        file?:return
        val kernel = Kernel(file)
        matrixImage = controllerConvolution.apply(matrixImage!!, kernel)
        imageController.changeView(matrixImage!!)
    }
    @FXML
    private lateinit var meanFilterH: TextField
    @FXML
    private lateinit var meanFilterW: TextField
    @FXML
    fun onMeanButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        if (meanFilterH.text == "" || meanFilterW.text == ""){
            applicationConsole.text = "Debe introducir un valor..."
            return
        }
        val height = meanFilterH.text.toInt()
        val width = meanFilterW.text.toInt()
        if (height == 0 ||width == 0){
            applicationConsole.text = "Fuera de Rango..."
            return
        }
        val kernel = Kernel(height, width)
        kernel.generateMean(height, width)
        matrixImage = controllerConvolution.apply(matrixImage!!, kernel)
        imageController.changeView(matrixImage!!)
    }
    @FXML
    private lateinit var gaussFilterSize: TextField
    @FXML
    fun onGaussButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        if (gaussFilterSize.text == ""){
            applicationConsole.text = "Debe introducir un valor..."
            return
        }
        val size = gaussFilterSize.text.toInt()
        if (size == 0 ||size > 7){
            applicationConsole.text = "Fuera de Rango..."
            return
        }
        val kernel = Kernel(size,size)
        kernel.generateGaussian(size)
        matrixImage = controllerConvolution.apply(matrixImage!!, kernel)
        imageController.changeView(matrixImage!!)
    }
    @FXML
    private lateinit var medianFilterSize: TextField
    @FXML
    fun onMedianButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        if (medianFilterSize.text == ""){
            applicationConsole.text = "Debe introducir un valor..."
            return
        }
        val size = medianFilterSize.text.toInt()
        if (size == 0 || size > 7){
            applicationConsole.text = "Fuera de Rango"
            return
        }
        matrixImage = noLinearController.applyMedianFilter(matrixImage!!, size)
        imageController.changeView(matrixImage!!)
    }

    private fun aplicarPerfilado(kernel: Kernel) {
        matrixImage?:return
        val convolutionController = ConvolutionController()
        val laplacianImage = convolutionController.apply(matrixImage!!, kernel)
        val width = matrixImage!!.width
        val height = matrixImage!!.height
        val newImage = ImageMatrix(width, height)
        newImage.maxVal = matrixImage!!.maxVal
        newImage.header = matrixImage!!.header

        val alpha = 1.0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val orig = matrixImage!!.pixels[y][x]
                val lap = laplacianImage.pixels[y][x]

                val r = (orig.r + alpha * lap.r).roundToInt().coerceIn(0, 255)
                val g = (orig.g + alpha * lap.g).roundToInt().coerceIn(0, 255)
                val b = (orig.b + alpha * lap.b).roundToInt().coerceIn(0, 255)

                newImage.pixels[y][x] = Pixel(r, g, b)
            }
        }
        matrixImage = newImage
        imageController.changeView(matrixImage!!)
    }

    @FXML
    fun onPerfilado8Click(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        aplicarPerfilado(Kernel(3,3).perfilado8())
    }

    @FXML
    fun onPerfilado4Click(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        aplicarPerfilado(Kernel(3,3).perfilado4())
    }

    @FXML
    lateinit var radioXRoberts: RadioButton
    @FXML
    lateinit var radioYRoberts: RadioButton
    @FXML
    lateinit var orientationGroupRoberts: ToggleGroup

    private var lastFilter: Int = 0 // 0 = ninguno, 1 = Sobel, 2 = Prewitt, 3 = Roberts

    @FXML
    fun onApplyRobertsClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        val orientation = if (radioXRoberts.isSelected) "X" else "Y"
        val kernel = Kernel(2,2).generateRoberts(orientation)
        val result = ConvolutionController().apply(matrixImage!!, kernel)
        matrixImage = result
        imageController.changeView(matrixImage!!)
        lastFilter = 3
    }

    @FXML
    lateinit var radioXSobel: RadioButton
    @FXML
    lateinit var radioYSobel: RadioButton
    @FXML
    lateinit var orientationGroupSobel: ToggleGroup
    @FXML
    lateinit var rowsSpinnerSobel: Spinner<Int>
    @FXML
    lateinit var colsSpinnerSobel: Spinner<Int>

    @FXML
    fun onApplySobelClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        val rows = rowsSpinnerSobel.value
        val cols = colsSpinnerSobel.value
        val orientation = if (radioXSobel.isSelected) "X" else "Y"
        val kernel = Kernel(rows,cols).generateSobel(rows, cols, orientation)
        val result = ConvolutionController().apply(matrixImage!!, kernel)
        matrixImage = result
        imageController.changeView(matrixImage!!)
        lastFilter = 1
    }

    @FXML
    lateinit var radioXPrewitt: RadioButton
    @FXML
    lateinit var radioYPrewitt: RadioButton
    @FXML
    lateinit var orientationGroupPrewitt: ToggleGroup
    @FXML
    lateinit var rowsSpinnerPrewitt: Spinner<Int>
    @FXML
    lateinit var colsSpinnerPrewitt: Spinner<Int>

    @FXML
    fun onApplyPrewittClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        val rows = rowsSpinnerPrewitt.value
        val cols = colsSpinnerPrewitt.value
        val orientation = if (radioXPrewitt.isSelected) "X" else "Y"
        val kernel = Kernel(rows,cols).generatePrewitt(rows, cols, orientation)
        val result = ConvolutionController().apply(matrixImage!!, kernel)
        matrixImage = result
        imageController.changeView(matrixImage!!)
        lastFilter = 2
    }

    @FXML
    lateinit var radioSobel: RadioButton
    @FXML
    lateinit var radioPrewitt: RadioButton
    @FXML
    lateinit var radioRoberts: RadioButton

    @FXML
    fun onApplyGradientClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)

        val gx: ImageMatrix
        val gy: ImageMatrix

        when (lastFilter) {
            1 -> {
                val rows = rowsSpinnerSobel.value
                val cols = colsSpinnerSobel.value
                val kernelX = Kernel(rows, cols).generateSobel(rows, cols, "X")
                val kernelY = Kernel(rows, cols).generateSobel(rows, cols, "Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            2 -> {
                val rows = rowsSpinnerPrewitt.value
                val cols = colsSpinnerPrewitt.value
                val kernelX = Kernel(rows, cols).generatePrewitt(rows, cols, "X")
                val kernelY = Kernel(rows, cols).generatePrewitt(rows, cols, "Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            3 -> {
                val kernelX = Kernel(2, 2).generateRoberts("X")
                val kernelY = Kernel(2, 2).generateRoberts("Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            else -> {
                println("No se ha aplicado ningún filtro antes del gradiente")
                return
            }
        }
        val gradient = combineGradient(gx, gy)
        matrixImage = gradient
        imageController.changeView(matrixImage!!)
    }

    fun combineGradient(gx: ImageMatrix, gy: ImageMatrix): ImageMatrix {
        val result = ImageMatrix(width = gx.width, height = gx.height)
        for (y in 0 until gx.height) {
            for (x in 0 until gx.width) {
                val valueX = gx[y, x]
                val valueY = gy[y, x]
                val magnitude = kotlin.math.sqrt(valueX * valueX + valueY * valueY)
                result[y, x] = magnitude
            }
        }
        return result
    }

}