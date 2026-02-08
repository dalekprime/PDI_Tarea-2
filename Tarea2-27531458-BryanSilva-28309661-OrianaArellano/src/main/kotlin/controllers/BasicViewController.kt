package controllers

import actions.ConvolutionController
import actions.LigthController
import actions.NoLinearController
import actions.PanningController
import actions.RotationController
import actions.UmbralizerController
import actions.TonoController
import actions.ZoomController
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.chart.AreaChart
import javafx.scene.chart.LineChart
import javafx.scene.control.Accordion
import javafx.scene.control.Button
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
import java.awt.Desktop
import java.io.File
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar

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
    private  lateinit var panningController: PanningController

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
        //Controladores de Acción
        umbralizerController = UmbralizerController()
        tonoController = TonoController()
        ligthController = LigthController()
        rotationController = RotationController()
        zoomController = ZoomController()
        controllerConvolution = ConvolutionController()
        noLinearController = NoLinearController()
        panningController = PanningController()
        //Controladores de Imagen, Gráficos e Información
        chartController = ChartStateController(histogramChart, toneCurveChart, perfilAreaChart)
        dataController = DataStateController(dimImage, colorsImage, bppImage)
        imageController = ImageStateController(stage,applicationConsole, mainImageView,
            chartController, dataController, zoomController, rotationController, panningController)
        //Leer Imagen Inicial y crea una copia
        matrixImage = imageController.loadNewImage()
        matrixImage?: return
        originalImage = matrixImage!!.copy()
        originalGeometryImage = matrixImage!!.copy()
        //Restable el Zoom
        currentZoomLevel = 0
        currentZoomMethod =  "NONE"
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
    fun onDownLoadJPG(event: ActionEvent) {
        matrixImage?:return
        imageController.downloadImageJPG(matrixImage!!)
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
                val previewImage = ligthController.brightness(matrixImage!!, newValue.toDouble())
                imageController.changeView(previewImage)
            }
        }
        //Contraste en Tiempo Real
        contrastSlider.valueProperty().addListener { _, _, newValue ->
            if (matrixImage != null) {
                val previewImage = ligthController.contrast(matrixImage!!, newValue.toDouble())
                imageController.changeView(previewImage)
            }
        }
        //Umbral Simple en Tiempo Real
        umbralSlider.valueProperty().addListener { _, _, newValue ->
            if (matrixImage != null) {
                val previewImage = umbralizerController.simpleUmbral(matrixImage!!, umbralSlider.value)
                imageController.changeView(previewImage)
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
        matrixImage = umbralizerController.simpleUmbral(matrixImage!!, umbralSlider.value)
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
        matrixImage =  umbralizerController.multiUmbral(matrixImage!!,
            umbralMultInf.text.toInt(),
            umbralMultiSup.text.toInt())
        imageController.changeView(matrixImage!!)
    }
    //Negativo de la Imagen
    @FXML
    fun onNegativeButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        matrixImage = tonoController.negativeImage(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Escala de Grises
    @FXML
    fun onGreyscaleButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        matrixImage = tonoController.greyScale(matrixImage!!)
        imageController.changeView(matrixImage!!)
    }
    //Escala de Color
    @FXML
    private lateinit var colorScalePicker: ColorPicker
    @FXML
    fun onColorScalePickerClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        matrixImage = tonoController.colorScale(matrixImage!!, colorScalePicker)
        imageController.changeView(matrixImage!!)
    }
    //Cambio de Brillo
    @FXML
    private lateinit var  lightSlider: Slider
    @FXML
    fun onBrigthnessButtonClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        matrixImage = ligthController.brightness(matrixImage!!, lightSlider.value)
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
        matrixImage = ligthController.contrast(matrixImage!!, contrastSlider.value)
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
    //Rotation
    @FXML
    private lateinit var rotationModeGroup: ToggleGroup
    //Update Rotacion Method
    @FXML
    fun onUpdateRotationMethodClick(event: ActionEvent) {
        matrixImage?:return
        val selection = (rotationModeGroup.selectedToggle as RadioButton).text
        val method = when (selection) {
            "Recortar" -> "NOEX"
            "Expandir" -> "EX"
            else -> "NOEX"
        }
        matrixImage!!.currentRotationMethod = method
        imageController.changeView(matrixImage!!)
    }
    //Rotacion Clockwise
    @FXML
    fun onRotationClockClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        matrixImage!!.currentRotationLevel -= 5.0
        imageController.changeView(matrixImage!!)
    }
    //Rotacion AntiClockwise
    @FXML
    fun onRotationClockAntiClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        matrixImage!!.currentRotationLevel += 5.0
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
    private var currentZoomLevel: Int = 0
    private var currentZoomMethod: String = "NONE"
    @FXML lateinit var radioZoomInBilinear: RadioButton
    @FXML lateinit var radioZoomOutSS: RadioButton
    //Actualizacion de Zoom en Tiempo Real
    @FXML
    private lateinit var groupZoomIn: ToggleGroup
    @FXML
    private lateinit var groupZoomOut: ToggleGroup
    @FXML
    fun onZoomSelectionClick(event: ActionEvent) {
        matrixImage?:return
        if(currentZoomMethod == "inBLI" || currentZoomMethod == "INN"){
            val selection = (groupZoomIn.selectedToggle as RadioButton).text
            if(selection == "Bilineal") currentZoomMethod = "inBLI"
            if(selection == "Vecino Próximo") currentZoomMethod = "INN"
            println("${currentZoomMethod}, $selection")
        }else if(currentZoomMethod == "OutSuperS" || currentZoomMethod == "OutN"){
            val selection = (groupZoomOut.selectedToggle as RadioButton).text
            if(selection == "Supersampling") currentZoomMethod = "OutSuperS"
            if(selection == "Vecino Próximo") currentZoomMethod = "OutN"
        }
        imageController.updateZoom(currentZoomLevel, currentZoomMethod)
        imageController.changeView(matrixImage!!)
    }
    //Zoom In
    @FXML
    fun onZoomInClick(event: ActionEvent) {
        matrixImage ?: return
        when (currentZoomLevel) {
            -2 -> currentZoomLevel = 0
            0 -> currentZoomLevel += 2
            else -> currentZoomLevel++
        }
        if(currentZoomLevel > 0) currentZoomMethod = if (radioZoomInBilinear.isSelected) "inBLI" else "INN"
        if(currentZoomLevel < 0) currentZoomMethod = if (radioZoomOutSS.isSelected) "OutSuperS" else "OutN"
        imageController.updateZoom(currentZoomLevel, currentZoomMethod)
        imageController.changeView(matrixImage!!)
    }
    //Zooom Out
    @FXML
    fun onZoomOutClick(event: ActionEvent) {
        matrixImage ?: return
        when (currentZoomLevel) {
            2 -> currentZoomLevel = 0
            0 -> currentZoomLevel -= 2
            else -> currentZoomLevel--
        }
        if(currentZoomLevel > 0) currentZoomMethod = if (radioZoomInBilinear.isSelected) "inBLI" else "INN"
        if(currentZoomLevel < 0) currentZoomMethod = if (radioZoomOutSS.isSelected) "OutSuperS" else "OutN"
        imageController.updateZoom(currentZoomLevel, currentZoomMethod)
        imageController.changeView(matrixImage!!)
    }
    //Reset Zoom
    @FXML
    fun onResetZoomClick() {
        matrixImage ?: return
        currentZoomLevel = 0
        currentZoomMethod = "NONE"
        imageController.updateZoom(currentZoomLevel, currentZoomMethod)
        imageController.changeView(matrixImage!!)
    }
    //Panning
    @FXML
    fun onPanningClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        val dir = (event.source as Button).id
        when (dir) {
            "u" -> {
                    matrixImage!!.currentPanningLevelY0 += 5
                    matrixImage!!.currentPanningLevelY1 += 5
            }
            "d" -> {
                    matrixImage!!.currentPanningLevelY1 -= 5
                    matrixImage!!.currentPanningLevelY0 -= 5
            }
            "l" -> {
                matrixImage!!.currentPanningLevelX0 += 5
                matrixImage!!.currentPanningLevelX1 += 5
            }
            "r" -> {
                matrixImage!!.currentPanningLevelX1 -= 5
                matrixImage!!.currentPanningLevelX0 -= 5
            }
        }
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
        val dest = Mat()
        val alpha = 1.0
        Core.addWeighted(
            matrixImage!!.image, 1.0,
            laplacianImage.image, alpha,
            0.0,
            dest
        )
        val newImage = ImageMatrix(dest, matrixImage!!)
        matrixImage = newImage
        imageController.changeView(newImage)
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
        when {
            radioSobel.isSelected -> {
                val rows = rowsSpinnerSobel.value
                val cols = colsSpinnerSobel.value
                val kernelX = Kernel(rows, cols).generateSobel(rows, cols, "X")
                val kernelY = Kernel(rows, cols).generateSobel(rows, cols, "Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            radioPrewitt.isSelected -> {
                val rows = rowsSpinnerPrewitt.value
                val cols = colsSpinnerPrewitt.value
                val kernelX = Kernel(rows, cols).generatePrewitt(rows, cols, "X")
                val kernelY = Kernel(rows, cols).generatePrewitt(rows, cols, "Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            radioRoberts.isSelected -> {
                val kernelX = Kernel(2, 2).generateRoberts("X")
                val kernelY = Kernel(2, 2).generateRoberts("Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            else -> {
                println("No se ha selecionado ningún filtro antes del gradiente")
                return
            }
        }
        val resultImage = combineGradient(gx, gy)
        matrixImage = resultImage
        imageController.changeView(matrixImage!!)
    }
    @FXML
    fun onApplyGradientAngleClick(event: ActionEvent) {
        matrixImage?:return
        imageController.saveToHistory(matrixImage!!)
        val gx: ImageMatrix
        val gy: ImageMatrix
        when {
            radioSobel.isSelected -> {
                val rows = rowsSpinnerSobel.value
                val cols = colsSpinnerSobel.value
                val kernelX = Kernel(rows, cols).generateSobel(rows, cols, "X")
                val kernelY = Kernel(rows, cols).generateSobel(rows, cols, "Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            radioPrewitt.isSelected -> {
                val rows = rowsSpinnerPrewitt.value
                val cols = colsSpinnerPrewitt.value
                val kernelX = Kernel(rows, cols).generatePrewitt(rows, cols, "X")
                val kernelY = Kernel(rows, cols).generatePrewitt(rows, cols, "Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            radioRoberts.isSelected -> {
                val kernelX = Kernel(2, 2).generateRoberts("X")
                val kernelY = Kernel(2, 2).generateRoberts("Y")
                gx = ConvolutionController().apply(matrixImage!!, kernelX)
                gy = ConvolutionController().apply(matrixImage!!, kernelY)
            }
            else -> {
                println("No se ha selecionado ningún filtro antes del gradiente")
                return
            }
        }
        val resultImage = calculateAngles(gx, gy)
        matrixImage = resultImage
        imageController.changeView(matrixImage!!)
    }
    fun combineGradient(gx: ImageMatrix, gy: ImageMatrix): ImageMatrix{
        val gxFloat = Mat()
        val gyFloat = Mat()
        gx.image.convertTo(gxFloat, CvType.CV_32F)
        gy.image.convertTo(gyFloat, CvType.CV_32F)
        val magnitude = Mat()
        Core.magnitude(gxFloat, gyFloat, magnitude)
        val result = Mat()
        magnitude.convertTo(result, CvType.CV_8U)
        gxFloat.release()
        gyFloat.release()
        magnitude.release()
        return ImageMatrix(result, matrixImage!!)
    }
    fun calculateAngles(gx: ImageMatrix, gy: ImageMatrix): ImageMatrix {
        val gxFloat = Mat()
        val gyFloat = Mat()
        gx.image.convertTo(gxFloat, CvType.CV_32F)
        gy.image.convertTo(gyFloat, CvType.CV_32F)
        val angles = Mat()
        Core.phase(gxFloat, gyFloat, angles, false)
        val scaleFactor = 255.0 / (2.0 * Math.PI)
        Core.multiply(angles, Scalar(scaleFactor), angles)
        val result = Mat()
        angles.convertTo(result, CvType.CV_8U)
        gxFloat.release()
        gyFloat.release()
        angles.release()
        return ImageMatrix(result, matrixImage!!)
    }

}