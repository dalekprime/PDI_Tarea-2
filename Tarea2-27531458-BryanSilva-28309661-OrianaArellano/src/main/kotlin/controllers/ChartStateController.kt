package controllers

import javafx.scene.chart.AreaChart
import javafx.scene.chart.LineChart
import javafx.scene.chart.XYChart
import models.ImageMatrix

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
        imageMatrix?:return
        val getVal: (Int, Int) -> Int = when (channel) {
            "R" -> { y, x -> imageMatrix.pixels[y][x].r }
            "G" -> { y, x -> imageMatrix.pixels[y][x].g }
            "B" -> { y, x -> imageMatrix.pixels[y][x].b }
            else -> { _, _ -> 0 }
        }
        val frequency = IntArray(256)
        val width = imageMatrix.width
        val height = imageMatrix.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val color = getVal(y, x).coerceIn(0, 255)
                frequency[color]++
            }
        }
        val dataList = ArrayList<XYChart.Data<Number, Number>>(256)
        for (i in 0 until 256) {
            dataList.add(XYChart.Data(i, frequency[i]))
        }
        val series = XYChart.Series<Number, Number>()
        series.data.setAll(dataList)
        histogramChart.data.clear()
        histogramChart.data.add(series)
    }
    //Actualiza la Curva Tonal
    fun updateCurve(originalImage: ImageMatrix?, actualImage: ImageMatrix?, channel: String) {
        originalImage ?: return
        actualImage ?: return
        /*if (originalImage.width != actualImage.width || originalImage.height != actualImage.height) {
            toneCurveChart.data.clear()
            return
        }*/
        val getOriginal: (Int, Int) -> Int = when (channel) {
            "R" -> { y, x -> originalImage.pixels[y][x].r }
            "G" -> { y, x -> originalImage.pixels[y][x].g }
            "B" -> { y, x -> originalImage.pixels[y][x].b }
            else -> { _, _ -> -1 }
        }
        val getActual: (Int, Int) -> Int = when (channel) {
            "R" -> { y, x -> actualImage.pixels[y][x].r }
            "G" -> { y, x -> actualImage.pixels[y][x].g }
            "B" -> { y, x -> actualImage.pixels[y][x].b }
            else -> { _, _ -> -1 }
        }
        val lookupTable = IntArray(256) { -1 }
        val width = originalImage.width
        val height = originalImage.height
        val step = if (width * height > 1_000_000) 4 else 1
        for (y in 0 until height step step) {
            for (x in 0 until width step step) {
                val valOriginal = getOriginal(y, x)
                val valNuevo = getActual(y, x)

                if (valOriginal in 0..255 && valNuevo != -1) {
                    lookupTable[valOriginal] = valNuevo.coerceIn(0, 255)
                }
            }
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
    }
    //Actuliza el Perfil de la Imagen
    fun updatePerfil(imageMatrix: ImageMatrix?, line: Int, channel: String) {
        imageMatrix?: return
        val width = imageMatrix.width
        if (line < 0 || line >= (imageMatrix.height)) {
            println("Error: La línea $line no existe")
            return
        }
        val series = XYChart.Series<Number, Number>()
        series.name = "Fila $line"
        for (x in 0 until width) {
            val color: Int = when (channel) {
                "R" -> imageMatrix.pixels[line][x].r
                "G" -> imageMatrix.pixels[line][x].g
                "B" -> imageMatrix.pixels[line][x].b
                else -> -1
            }
            series.data.add(XYChart.Data(x, color))
        }
        perfilerChart.data.clear()
        perfilerChart.data.add(series)
    }
}