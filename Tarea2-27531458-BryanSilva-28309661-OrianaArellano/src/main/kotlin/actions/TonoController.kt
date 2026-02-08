package actions

import javafx.scene.control.ColorPicker
import models.ImageMatrix
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class TonoController {
    //Negativo
    fun negativeImage(imageMatrix: ImageMatrix): ImageMatrix {
        val newImage = imageMatrix.copy()
        Core.bitwise_not(imageMatrix.image, newImage.image)
        return newImage;
    }
    //Escala de Grises
    fun greyScale(imageMatrix: ImageMatrix): ImageMatrix {
        if (imageMatrix.image.channels() == 1) {
            return imageMatrix.copy()
        }
        val newImage = Mat()
        if (imageMatrix.image.channels() == 4) {
            Imgproc.cvtColor(imageMatrix.image, newImage, Imgproc.COLOR_BGRA2GRAY)
        } else {
            Imgproc.cvtColor(imageMatrix.image, newImage, Imgproc.COLOR_BGR2GRAY)
        }
        return ImageMatrix(newImage, imageMatrix);
    }
    //Escala de Color
    fun colorScale(imageMatrix: ImageMatrix, colorScalePicker: ColorPicker): ImageMatrix {
        val r = (colorScalePicker.value.red * 255.0).toInt()
        val g = (colorScalePicker.value.green * 255.0).toInt()
        val b = (colorScalePicker.value.blue * 255.0).toInt()
        val greyImage = Mat()
        if (imageMatrix.image.channels() == 4) {
            Imgproc.cvtColor(imageMatrix.image, greyImage, Imgproc.COLOR_BGRA2GRAY)
        } else {
            Imgproc.cvtColor(imageMatrix.image, greyImage, Imgproc.COLOR_BGR2GRAY)
        }
        val src3Channels = Mat()
        Imgproc.cvtColor(greyImage, src3Channels, Imgproc.COLOR_GRAY2BGR)
        greyImage.release()
        val lut = Mat(1, 256, CvType.CV_8UC3)
        val lutData = ByteArray(256 * 3)
        for (i in 0..255) {
            var rOut: Int
            var gOut: Int
            var bOut: Int
            if (i < 128) {
                rOut = (r * i) / 128
                gOut = (g * i) / 128
                bOut = (b * i) / 128
            } else {
                rOut = r + (255 - r) * (i - 128) / 128
                gOut = g + (255 - g) * (i - 128) / 128
                bOut = b + (255 - b) * (i - 128) / 128
            }
            val index = i * 3
            lutData[index] = bOut.toByte()
            lutData[index + 1] = gOut.toByte()
            lutData[index + 2] = rOut.toByte()
        }
        lut.put(0, 0, lutData)
        val resultMat = Mat()
        Core.LUT(src3Channels, lut, resultMat)
        src3Channels.release()
        lut.release()
        return ImageMatrix(resultMat, imageMatrix);
    }

    fun equalizeHistogram(imageMatrix: ImageMatrix): ImageMatrix {
        val src = imageMatrix.image
        val dst = Mat()
        if (src.channels() == 1) {
            Imgproc.equalizeHist(src, dst)
        } else {
            val ycrcb = Mat()
            Imgproc.cvtColor(src, ycrcb, Imgproc.COLOR_BGR2YCrCb)
            val channels = ArrayList<Mat>()
            Core.split(ycrcb, channels)
            Imgproc.equalizeHist(channels[0], channels[0])
            Core.merge(channels, ycrcb)
            Imgproc.cvtColor(ycrcb, dst, Imgproc.COLOR_YCrCb2BGR)
            ycrcb.release()
            channels.forEach { it.release() }
        }
        return ImageMatrix(dst, imageMatrix)
    }

    fun adjustHLS(imageMatrix: ImageMatrix, hueShift: Double, satOffset: Double, lumOffset: Double): ImageMatrix {
        val src = imageMatrix.image
        val hls = Mat()
        Imgproc.cvtColor(src, hls, Imgproc.COLOR_BGR2HLS)
        val channels = ArrayList<Mat>()
        Core.split(hls, channels)
        if (hueShift != 0.0) {
            val lut = Mat(1, 256, CvType.CV_8U)
            val data = ByteArray(256)
            for (i in 0..255) {
                var newVal = (i + hueShift.toInt()) % 180
                if (newVal < 0) newVal += 180
                data[i] = newVal.toByte()
            }
            lut.put(0, 0, data)
            Core.LUT(channels[0], lut, channels[0])
            lut.release()
        }
        if (lumOffset != 0.0) {
            Core.add(channels[1], Scalar(lumOffset), channels[1])
        }
        if (satOffset != 0.0) {
            Core.add(channels[2], Scalar(satOffset), channels[2])
        }
        Core.merge(channels, hls)
        val dst = Mat()
        Imgproc.cvtColor(hls, dst, Imgproc.COLOR_HLS2BGR)
        hls.release()
        channels.forEach { it.release() }
        return ImageMatrix(dst, imageMatrix)
    }

    fun whiteBalanceYUV(imageMatrix: ImageMatrix, uGain: Double, vGain: Double): ImageMatrix {
        val src = imageMatrix.image
        val yuv = Mat()
        Imgproc.cvtColor(src, yuv, Imgproc.COLOR_BGR2YUV)
        val channels = ArrayList<Mat>()
        Core.split(yuv, channels)
        if (uGain != 1.0) {
            Core.multiply(channels[1], Scalar(uGain), channels[1])
        }
        if (vGain != 1.0) {
            Core.multiply(channels[2], Scalar(vGain), channels[2])
        }
        Core.merge(channels, yuv)
        val dst = Mat()
        Imgproc.cvtColor(yuv, dst, Imgproc.COLOR_YUV2BGR)
        yuv.release()
        channels.forEach { it.release() }
        return ImageMatrix(dst, imageMatrix)
    }
}