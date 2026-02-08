package actions

import models.ImageMatrix
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class SegmentationController {
    fun regionGrowing(
        imageMatrix: ImageMatrix,
        seedX: Int,
        seedY: Int,
        threshold: Double,
        isFixedRange: Boolean,
        // 4 u 8
        connectivity: Int
    ): ImageMatrix {
        val src = imageMatrix.image
        val mask = Mat(src.rows() + 2, src.cols() + 2, CvType.CV_8UC1, Scalar.all(0.0))
        val lowerDiff = Scalar.all(threshold)
        val upperDiff = Scalar.all(threshold)
        var flags = connectivity
        if (isFixedRange) {
            flags = flags or Imgproc.FLOODFILL_FIXED_RANGE
        }
        flags = flags or Imgproc.FLOODFILL_MASK_ONLY
        flags = flags or (255 shl 8)
        Imgproc.floodFill(
            src,
            mask,
            Point(seedX.toDouble(), seedY.toDouble()),
            Scalar(255.0, 255.0, 255.0),
            Rect(),
            lowerDiff,
            upperDiff,
            flags
        )
        val roi = Rect(1, 1, src.cols(), src.rows())
        val finalMask = mask.submat(roi)
        val result = finalMask.clone()
        mask.release()
        finalMask.release()
        return ImageMatrix(result, imageMatrix)
    }
}