package actions

import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.TermCriteria
import org.opencv.imgproc.Imgproc
import java.util.PriorityQueue
import kotlin.math.floor

class QuantizationController {

    private fun ensureBGR(imageMatrix: Mat): Mat {
        val bgr = Mat()
        if (imageMatrix.channels() == 4) {
            Imgproc.cvtColor(imageMatrix, bgr, Imgproc.COLOR_BGRA2BGR)
        } else {
            imageMatrix.copyTo(bgr)
        }
        return bgr
    }

    fun applyUniform(imageMatrix: ImageMatrix, levels: Int): ImageMatrix {
        val src = ensureBGR(imageMatrix.image)
        val dst = Mat()
        val safeLevels = if (levels < 2) 2 else levels
        val div = 256.0 / safeLevels
        val lut = Mat(1, 256, CvType.CV_8U)
        val data = ByteArray(256)
        for (i in 0..255) {
            var newVal = (floor(i / div) * div + (div / 2.0)).toInt()
            if (newVal > 255) newVal = 255
            data[i] = newVal.toByte()
        }
        lut.put(0, 0, data)
        Core.LUT(src, lut, dst)
        lut.release()
        src.release()
        return ImageMatrix(dst, imageMatrix)
    }

    fun applyKMeans(imageMatrix: ImageMatrix, k: Int): ImageMatrix {
        val src = ensureBGR(imageMatrix.image)
        val samples = src.reshape(1, src.rows() * src.cols())
        val samples32f = Mat()
        samples.convertTo(samples32f, CvType.CV_32F)
        val labels = Mat()
        val centers = Mat()
        val criteria = TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10, 1.0)
        Core.kmeans(
            samples32f,
            k,
            labels,
            criteria,
            3,
            Core.KMEANS_RANDOM_CENTERS,
            centers
        )
        val dst = Mat(src.size(), src.type())
        val centers8u = Mat()
        centers.convertTo(centers8u, CvType.CV_8U)
        val rows = src.rows()
        val cols = src.cols()
        val resBuffer = ByteArray(rows * cols * 3)
        val centersData = ByteArray(k * 3)
        centers8u.get(0, 0, centersData)
        val labelsData = IntArray(rows * cols)
        labels.get(0, 0, labelsData)
        var pxIdx = 0
        for (i in 0 until (rows * cols)) {
            val clusterIdx = labelsData[i]
            val baseIdx = clusterIdx * 3
            resBuffer[pxIdx++] = centersData[baseIdx]
            resBuffer[pxIdx++] = centersData[baseIdx + 1]
            resBuffer[pxIdx++] = centersData[baseIdx + 2]
        }
        dst.put(0, 0, resBuffer)
        samples.release()
        samples32f.release()
        labels.release()
        centers.release()
        centers8u.release()
        src.release()
        return ImageMatrix(dst, imageMatrix)
    }

    fun applyPopularity(imageMatrix: ImageMatrix, k: Int): ImageMatrix {
        val src = ensureBGR(imageMatrix.image)
        val width = src.width()
        val height = src.height()
        val colorCounts = HashMap<Int, Int>()
        val buffer = ByteArray(width * height * 3)
        src.get(0, 0, buffer)
        for (i in buffer.indices step 3) {
            val b = buffer[i].toInt() and 0xFF
            val g = buffer[i+1].toInt() and 0xFF
            val r = buffer[i+2].toInt() and 0xFF
            val key = ((r and 0xF8) shl 8) or ((g and 0xF8) shl 3) or ((b and 0xF8) shr 3)
            colorCounts[key] = colorCounts.getOrDefault(key, 0) + 1
        }
        val pq = PriorityQueue<Pair<Int, Int>> { a, b -> a.second - b.second }
        for ((color, count) in colorCounts) {
            pq.add(Pair(color, count))
            if (pq.size > k) {
                pq.poll()
            }
        }
        val palette = ArrayList<Int>()
        while (pq.isNotEmpty()) {
            palette.add(pq.poll().first)
        }
        val dst = Mat(src.size(), src.type())
        val outBuffer = ByteArray(buffer.size)
        val palR = IntArray(k)
        val palG = IntArray(k)
        val palB = IntArray(k)
        for (i in 0 until k) {
            val c = palette[i]
            palR[i] = (c shr 8) and 0xF8
            palG[i] = (c shr 3) and 0xF8
            palB[i] = (c shl 3) and 0xF8
        }
        for (i in buffer.indices step 3) {
            val b = buffer[i].toInt() and 0xFF
            val g = buffer[i+1].toInt() and 0xFF
            val r = buffer[i+2].toInt() and 0xFF
            var minDist = Int.MAX_VALUE
            var bestIdx = 0
            for (p in 0 until k) {
                val dr = r - palR[p]
                val dg = g - palG[p]
                val db = b - palB[p]
                val dist = dr*dr + dg*dg + db*db
                if (dist < minDist) {
                    minDist = dist
                    bestIdx = p
                    if (dist == 0) break
                }
            }
            outBuffer[i] = palB[bestIdx].toByte()
            outBuffer[i+1] = palG[bestIdx].toByte()
            outBuffer[i+2] = palR[bestIdx].toByte()
        }
        dst.put(0, 0, outBuffer)
        src.release()
        return ImageMatrix(dst, imageMatrix)
    }
}