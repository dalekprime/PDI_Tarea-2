package controllers

import javafx.scene.chart.AreaChart
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.imgproc.Imgproc

class ChartStateController {

    //Referencias a los gráficos
    private var histogramChart: AreaChart<Number, Number>
    private var toneCurveChart: LineChart<Number, Number>
    private var perfilerChart: AreaChart<Number, Number>

    constructor(histogramChart: AreaChart<Number, Number>,
                toneCurveChart: LineChart<Number, Number>,
                perfilerChart: AreaChart<Number, Number>) {
        this.histogramChart = histogramChart
        this.toneCurveChart = toneCurveChart
        this.perfilerChart = perfilerChart
        histogramChart.animated = false
        histogramChart.createSymbols = false
        toneCurveChart.animated = false
        toneCurveChart.createSymbols = false
        perfilerChart.animated = false
        perfilerChart.createSymbols = false
    }
    //Actualiza el Histograma
    fun updateHistogram(imageMatrix: ImageMatrix?, channel: String) {
        val mat = imageMatrix?.image ?: return
        val channelIdx = when (channel) {
            "B" -> 0
            "G" -> 1
            "R" -> 2
            else -> return
        }
        val images = ArrayList<Mat>()
        images.add(mat)
        val channels = MatOfInt(channelIdx)
        val hist = Mat()
        val histSize = MatOfInt(256)
        val ranges = MatOfFloat(0f, 256f)
        Imgproc.calcHist(images, channels, Mat(), hist, histSize, ranges)
        val histData = FloatArray(256)
        hist.get(0, 0, histData)
        val dataList = ArrayList<XYChart.Data<Number, Number>>(256)
        val hisCopy = histData.copyOf()
        hisCopy.sort()
        val base: Int = if(hisCopy[255] / hisCopy[254] > 3 ) 1 else 0
        for (i in base until 256) {
            dataList.add(XYChart.Data(i, histData[i]))
        }
        val series = XYChart.Series<Number, Number>()
        series.data.setAll(dataList)
        histogramChart.data.clear()
        histogramChart.data.add(series)
        hist.release()
        channels.release()
        histSize.release()
        ranges.release()
    }
    //Actualiza la Curva Tonal
    fun updateCurve(originalImage: ImageMatrix?, actualImage: ImageMatrix?, channel: String) {
        val matOrig = originalImage?.image ?: return
        val matCurr = actualImage?.image ?: return
        if (matOrig.size() != matCurr.size()) return
        val channelIdx = when (channel) {
            "B" -> 0
            "G" -> 1
            "R" -> 2
            else -> return
        }
        val chOrig = Mat()
        val chCurr = Mat()
        Core.extractChannel(matOrig, chOrig, channelIdx)
        Core.extractChannel(matCurr, chCurr, channelIdx)
        val size = (chOrig.total()).toInt()
        val buffOrig = ByteArray(size)
        val buffCurr = ByteArray(size)
        chOrig.get(0, 0, buffOrig)
        chCurr.get(0, 0, buffCurr)
        val lookupTable = IntArray(256) { -1 }
        val step = if (size > 1_000_000) 4 else 1
        for (i in 0 until size step step) {
            val valOriginal = buffOrig[i].toInt() and 0xFF
            val valNuevo = buffCurr[i].toInt() and 0xFF
            lookupTable[valOriginal] = valNuevo
        }
        val dataList = ArrayList<XYChart.Data<Number, Number>>()
        for (inputVal in 0 until 256) {
            val outputVal = lookupTable[inputVal]
            if (outputVal != -1) {
                dataList.add(XYChart.Data(inputVal, outputVal))
            }
        }
        val series = XYChart.Series<Number, Number>()
        series.data.setAll(dataList)
        toneCurveChart.data.clear()
        toneCurveChart.data.add(series)
        chOrig.release()
        chCurr.release()
    }
    //Actuliza el Perfil de la Imagen
    fun updatePerfil(imageMatrix: ImageMatrix?, line: Int, channel: String) {
        val mat = imageMatrix?.image ?: return
        val width = mat.cols()
        val height = mat.rows()
        var rline = line
        if(line > height) rline = height
        if (line < 0) rline = 0
        val channelIdx = when (channel) {
            "B" -> 0
            "G" -> 1
            "R" -> 2
            else -> return
        }
        val rowMat = mat.row(rline)
        val channels = mat.channels()
        val totalBytes = width * channels
        val buffer = ByteArray(totalBytes)
        rowMat.get(0, 0, buffer)
        val series = XYChart.Series<Number, Number>()
        series.name = "Fila $rline ($channel)"
        var idx = 0
        for (x in 0 until width) {
            val value = buffer[idx + channelIdx].toInt() and 0xFF
            series.data.add(XYChart.Data(x, value))
            idx += channels
        }
        perfilerChart.data.clear()
        perfilerChart.data.add(series)
    }
}