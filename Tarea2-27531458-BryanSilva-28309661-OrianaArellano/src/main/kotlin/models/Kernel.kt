package models

import java.io.File
import java.util.Scanner
import kotlin.Double
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow

class Kernel {

    var height: Int = 0
    var width: Int = 0
    var matrix: Array<Array<Double>>

    constructor(height: Int, width: Int) {
        this.height = height
        this.width = width
        matrix = Array(height) { Array(width) { 0.0 } }
    }
    constructor(file: File){
        var ele = Scanner(file)
        this.height = ele.nextInt()
        this.width = ele.nextInt()
        matrix = Array(height) { Array(width) { 0.0 } }
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (ele.hasNext()) {
                    val value = ele.nextDouble()
                    matrix[y][x] = value
                }
            }
        }
        ele.close()

    }

    fun generateMean(height: Int, width: Int){
        this.height = height
        this.width = width
        val totalPixels = (height * width).toDouble()
        val value = 1.0 / totalPixels
        for (y in 0 until height) {
            for (x in 0 until width) {
                matrix[y][x] = value
            }
        }
    }
    fun generateGaussian(size: Int, sigma: Double = 1.0){
        val safeSize = if (size % 2 == 0) size + 1 else size
        this.height = safeSize
        this.width = safeSize
        val s = if (sigma <= 0.0) (safeSize / 2.0) / 3.0 else sigma
        val center = safeSize / 2
        var sum = 0.0
        for (y in 0 until safeSize) {
            for (x in 0 until safeSize) {
                val distanceX = x - center
                val distanceY = y - center
                val exponent = -(distanceX.toDouble().pow(2) + distanceY.toDouble().pow(2)) / (2 * s.pow(2))
                val value = (1 / (2 * PI * s.pow(2))) * exp(exponent)
                matrix[y][x] = value
                sum += value
            }
        }
        for (y in 0 until safeSize) {
            for (x in 0 until safeSize) {
                matrix[y][x] /= sum
            }
        }
    }
    fun perfilado4(): Kernel {
        val k = Kernel(3,3)
        k.matrix = arrayOf(
            arrayOf(0.0, -1.0, 0.0),
            arrayOf(-1.0, 4.0, -1.0),
            arrayOf(0.0, -1.0, 0.0)
        )
        return k
    }
    fun perfilado8(): Kernel {
        val k = Kernel(3,3)
        k.matrix = arrayOf(
            arrayOf(-1.0, -1.0, -1.0),
            arrayOf(-1.0, 8.0, -1.0),
            arrayOf(-1.0, -1.0, -1.0)
        )
        return k
    }

    fun generateRoberts(orientation: String): Kernel {
        val k = Kernel(2, 2)
        if (orientation == "X") {
            k.matrix = arrayOf(
                arrayOf(1.0, 0.0),
                arrayOf(0.0, -1.0)
            )
        } else {
            k.matrix = arrayOf(
                arrayOf(0.0, 1.0),
                arrayOf(-1.0, 0.0)
            )
        }
        return k
    }
    fun generatePrewitt(rows: Int, cols: Int, orientation: String): Kernel {
        val k = Kernel(rows, cols)
        val centerRow = rows / 2
        val centerCol = cols / 2

        if (orientation.uppercase() == "X") {
            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    k.matrix[y][x] = (x - centerCol).toDouble()
                }
            }
        } else {
            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    k.matrix[y][x] = (y - centerRow).toDouble()
                }
            }
        }
        return k
    }

    fun generateSobel(rows: Int, cols: Int, orientation: String): Kernel {
        val k = Kernel(rows, cols)
        val centerRow = rows / 2
        val centerCol = cols / 2
        val scale = when {
            rows == 3 && cols == 3 -> 2.0
            rows == 5 && cols == 5 -> 20.0
            rows == 7 && cols == 7 -> 4680.0
            else -> (rows * cols * 4).toDouble()
        }

        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val i = x - centerCol
                val j = y - centerRow
                val denom = (i * i + j * j).toDouble()

                val value = when {
                    denom == 0.0 -> 0.0
                    orientation.uppercase() == "X" -> (i / denom) * scale
                    orientation.uppercase() == "Y" -> (j / denom) * scale
                    else -> throw IllegalArgumentException("Orientación inválida: usa 'X' o 'Y'")
                }

                k.matrix[y][x] = Math.round(value).toDouble()
            }
        }

        return k
    }

}