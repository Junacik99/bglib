package com.example.bglib.imgproc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.bglib.classes.Card
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.sqrt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.mediapipe.examples.imagesegmenter.ImageSegmenterHelper
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.ByteBufferExtractor
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter
import com.google.mediapipe.tasks.vision.imagesegmenter.ImageSegmenter.ImageSegmenterOptions
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


class ImageProcessing {
    companion object{

        data class Pixel(val red: UByte, val green: UByte, val blue: UByte)
        data class Vector2i(val x: Int, val y: Int)

        fun medianFilter(img: Mat, kernelSize: Int = 7): Mat {
            val median = Mat()
            Imgproc.medianBlur(img, median, kernelSize)
            return median
        }

        fun gaussFilter(img: Mat, kernelSize: Int = 7): Mat {
            val gauss = Mat()
            Imgproc.GaussianBlur(img, gauss, Size(kernelSize.toDouble(), kernelSize.toDouble()), 0.0)
            return gauss
        }

        fun sharpenConv2d(img: Mat, kernelData: FloatArray = floatArrayOf(0.0f, -1.0f, 0.0f, -1.0f, 5.0f, -1.0f, 0.0f, -1.0f, 0.0f)): Mat {
            val kernel = Mat(3, 3, CvType.CV_32F)
            kernel.put(0,0, kernelData)

            val sharpened = Mat()
            Imgproc.filter2D(img, sharpened, -1, kernel)
            return sharpened
        }


        // Retrieves the color of a pixel at the specified coordinates.
        // ColorInt - RGB can be easily represented as Int (for example blue = 0x0000ff)
        fun getPixelColor(frame: Mat, x: Int, y: Int): Int {
            val pixel = frame.get(y, x)
            val blue = pixel[2].toInt()
            val green = pixel[1].toInt()
            val red = pixel[0].toInt()
            return Color.rgb(red, green, blue)
        }

        // Get average color of the frame/subframe (roi)
        fun getAvgColor(frame: Mat): Int{
            val pixelCount = frame.height() * frame.width()
            if (pixelCount == 0)
                return Color.TRANSPARENT

            var redSum = 0
            var greenSum = 0
            var blueSum = 0

            for (x in 0 until frame.width()){
                for (y in 0 until frame.height()){
                    val color = getPixelColor(frame, x, y)
                    redSum += color.red
                    greenSum += color.green
                    blueSum += color.blue
                }
            }

            val avgRed = redSum / pixelCount
            val avgGreen = greenSum / pixelCount
            val avgBlue = blueSum / pixelCount

            return Color.rgb(avgRed, avgGreen, avgBlue)
        }

        fun divideFrameIntoGrid(frame: Mat, numRows: Int, numCols: Int): List<Mat> {
            val subframeWidth = frame.width() / numCols
            val subframeHeight = frame.height() / numRows
            val subframes = mutableListOf<Mat>()

            for (row in 0 until numRows) {
                for (col in 0 until numCols) {
                    val x = col * subframeWidth
                    val y = row * subframeHeight
                    val region = Rect(x, y, subframeWidth, subframeHeight)
                    val subframe = frame.submat(region)
                    subframes.add(subframe)
                }
            }

            return subframes
        }

        fun getColorDistance(color1: Int, color2: Int): Int {
            val redDiff = abs(color1.red - color2.red)
            val greenDiff = abs(color1.green - color2.green)
            val blueDiff = abs(color1.blue - color2.blue)
            return redDiff + greenDiff + blueDiff
        }

        fun getClosestColor(color: Int, targetColors: List<Int>): Int {

            var minDistance = Int.MAX_VALUE
            var closestColor = 0

            for (colorValue in targetColors) {
                val distance = getColorDistance(color, colorValue)
                if (distance < minDistance) {
                    minDistance = distance
                    closestColor = colorValue
                }
            }

            return closestColor
        }


        // Sorts unordered list of cards into uniform grid structure
        fun cards2grid(cards: List<Rect>, numRows: Int, numCols: Int): List<Card> {
            val sortedCards = cards.sortedBy { it.y }
            val grid = mutableListOf<Card>()

            for (row in 0 until numRows) {
                val cardsInRow = sortedCards.subList(row * numCols, minOf((row + 1) * numCols, sortedCards.size))
                    .sortedBy { it.x } // Sort by x within each row

                for (col in 0 until cardsInRow.size) {
                    val gridPos = Vector2i(row, col)
                    val newCard = Card(cardsInRow[col], "")
                    newCard.gridPos.col = col
                    newCard.gridPos.row = row
                    grid.add(newCard)
                }
            }

            return grid
        }

        fun rotateImage(source: Mat, degrees: Double): Mat {
            val sourceSize = source.size()
            val center = Point(sourceSize.width / 2, sourceSize.height / 2)

            // Create the rotation matrix
            val rotationMatrix = Imgproc.getRotationMatrix2D(center, degrees, 1.0)

            // Calculate the new size of the rotated image
            val cos = abs(rotationMatrix.get(0, 0)[0])
            val sin = abs(rotationMatrix.get(0, 1)[0])
            val newWidth = (sourceSize.height * sin) + (sourceSize.width * cos)
            val newHeight = (sourceSize.width * sin) + (sourceSize.height * cos)
            val newSize = Size(newWidth, newHeight)

            // Adjust the rotation matrix to take into account the translation
            rotationMatrix.put(0, 2, rotationMatrix.get(0, 2)[0] + newWidth / 2 - center.x)
            rotationMatrix.put(1, 2, rotationMatrix.get(1, 2)[0] + newHeight / 2 - center.y)

            // Create the destination Mat
            val destination = Mat(newSize, source.type())

            // Apply the rotation
            Imgproc.warpAffine(source, destination, rotationMatrix, newSize)

            return destination
        }

        /* SEGMENTATION */

        // Feature data class with 5 attributes - RGB and x, y coordinates
        data class Feature(val r: Int, val g: Int, val b: Int, val x: Int, val y: Int){
            fun normalized_euclidean_distance(f2: Feature, maxColor: Int, maxX: Int, maxY: Int): Double {
                val nr = r.toDouble() / maxColor
                val ng = g.toDouble() / maxColor
                val nb = b.toDouble() / maxColor
                val nx = x.toDouble() / maxX
                val ny = y.toDouble() / maxY

                val dx = nx - f2.x.toDouble()
                val dy = ny - f2.y
                val dr = nr - f2.r
                val dg = ng - f2.g
                val db = nb - f2.b

                return sqrt(dx * dx + dy * dy + dr * dr + dg * dg + db * db)
            }

            // Euclidean distance between two features
            fun euclidean_distance(f2: Feature): Double{
                val dx = x - f2.x.toDouble()
                val dy = y - f2.y
                val dr = r - f2.r
                val dg = g - f2.g
                val db = b - f2.b

                return sqrt(dx * dx + dy * dy + dr * dr + dg * dg + db * db)
            }
        }

        // Data class for segmented image
        data class SegmentedImage(val img: Bitmap, val labels: List<Feature>)

        // K-Means image segmentation based on five features
        // RGB values and x, y coordinates
        fun segment_kmeans(img: Bitmap, k: Int, maxIters: Int = 100): SegmentedImage {

            val width = img.width
            val height = img.height

            // Init centroids
            val centroids = MutableList(k) {
                val x = (0 until width).random()
                val y = (0 until height).random()
                val pixel = img[x, y]
                Feature(pixel.red, pixel.green, pixel.blue, x, y)
            }

            val labels = IntArray(width * height)
            val dst = createBitmap(width, height, img.config!!)

            // Repeat until convergence or max iterations
            var changes = 0
            var iterations = 0
            do {
                iterations++
                changes = 0

                // Assign features to centroids
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val pixel = img[x, y]
                        val feature = Feature(pixel.red, pixel.green, pixel.blue, x, y)

                        val distances = centroids.map { it.euclidean_distance(feature) }
                        val label = distances.indexOf(distances.min())

                        val oldLabel = labels[y * width + x]
                        if (oldLabel != label) {
                            changes++
                        }

                        labels[y * width + x] = label
                    }
                }

                // Compute new centroids
                val newCentroids = MutableList(k) { Feature(0, 0, 0, 0, 0) } // Initialize with default values
                val counts = IntArray(k) { 0 }

                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val label = labels[y * width + x]
                        val pixel = img[x, y]
                        val feature = Feature(pixel.red, pixel.green, pixel.blue, x, y)

                        newCentroids[label] = newCentroids[label].copy(
                            r = newCentroids[label].r + feature.r,
                            g = newCentroids[label].g + feature.g,
                            b = newCentroids[label].b + feature.b,
                            x = newCentroids[label].x + feature.x,
                            y = newCentroids[label].y + feature.y
                        )

                        counts[label]++
                    }
                }

                // Divide to get the average values for new centroids
                for (i in 0 until k) {
                    if (counts[i] > 0) {
                        newCentroids[i] = newCentroids[i].copy(
                            r = newCentroids[i].r / counts[i],
                            g = newCentroids[i].g / counts[i],
                            b = newCentroids[i].b / counts[i],
                            x = newCentroids[i].x / counts[i],
                            y = newCentroids[i].y / counts[i]
                        )
                    }
                }

                // Update centroids with new values
                for(i in 0 until k){
                    centroids[i] = newCentroids[i]
                }
            } while (changes > 0 && iterations < maxIters)

            // Assign the cluster center color to each pixel
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val label = labels[y * width + x]
                    val centroid = centroids[label]
                    dst[x, y] = Color.rgb(centroid.r, centroid.g, centroid.b)
                }
            }

            return SegmentedImage(dst, centroids)
        }

        /// Functions for selecting the right window size
        // Scott's Rule:
        //  n - number of points (pixels)
        //  d - number of dimensions
        fun scottsRule(n: Int, d: Int): Int{
            return n.toDouble().pow(-1.0 / (d + 4)).toInt()
        }

        // Silverman's Rule
        fun silvermansRule(n: Int, d: Int): Int{
            val v1 = 4.0 / (d + 2)
            val v2 = n.toDouble().pow(-1.0 / (d + 4))
            return (v1.pow(1.0 / (d + 4)) * v2).toInt()
        }

        // Compute gaussian kernel of the window
        fun gaussianKernel(distance: Double, bandwidth: Double): Double {
            return exp(-(distance.pow(2)) / (2 * bandwidth.pow(2)))
        }

        // Mean Shift image segmentation
        fun segment_meanshift(
            img: Bitmap,
            colorBandwidth: Double = 30.0,
            spatialBandwidth: Int = 21,
            threshold: Double = 0.001,
            maxIters: Int = 100,
        ): SegmentedImage {
            val width = img.width
            val height = img.height

            // Init means with pixel values
            val means = MutableList(width * height) { index ->
                val x = index % width
                val y = index / width
                val pixel = img[x, y]
                Feature(pixel.red, pixel.green, pixel.blue, x, y)
            }

            val resultBitmap = createBitmap(width, height, img.config!!)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val idx = y * width + x
                    var mean = means[idx]

                    var iterations = 0
                    var hasShifted: Boolean
                    do {
                        iterations++
                        hasShifted = false

                        var totalWeight = 0.0
                        var weightedSumR = 0
                        var weightedSumG = 0
                        var weightedSumB = 0
                        var weightedSumX = 0
                        var weightedSumY = 0

                        // Window around the mean
                        val windowMinX = maxOf(0, mean.x - spatialBandwidth)
                        val windowMaxX = minOf(width - 1, mean.x + spatialBandwidth)
                        val windowMinY = maxOf(0, mean.y - spatialBandwidth)
                        val windowMaxY = minOf(height - 1, mean.y + spatialBandwidth)

                        for (winY in windowMinY..windowMaxY) {
                            for (winX in windowMinX..windowMaxX) {
                                val pixel = img[winX, winY]
                                val currentFeature = Feature(pixel.red, pixel.green, pixel.blue, winX, winY)

                                // Split the distance in two components: spatial and color
                                val spatialDistance = sqrt(
                                    ((winX - mean.x).toDouble().pow(2) + (winY - mean.y).toDouble().pow(2))
                                )
                                val colorDistance = sqrt(
                                    (mean.r - currentFeature.r).toDouble()
                                        .pow(2) + (mean.g - currentFeature.g).toDouble()
                                        .pow(2) + (mean.b - currentFeature.b).toDouble().pow(2)
                                )

                                // Compute weights
                                val spatialWeight =
                                    gaussianKernel(spatialDistance, spatialBandwidth.toDouble())
                                val colorWeight = gaussianKernel(colorDistance, colorBandwidth)
                                val finalWeight = spatialWeight * colorWeight

                                totalWeight += finalWeight
                                weightedSumR += (currentFeature.r * finalWeight).toInt()
                                weightedSumG += (currentFeature.g * finalWeight).toInt()
                                weightedSumB += (currentFeature.b * finalWeight).toInt()
                                weightedSumX += (currentFeature.x * finalWeight).toInt()
                                weightedSumY += (currentFeature.y * finalWeight).toInt()
                            }
                        }

                        // New mean
                        val newMean = if (totalWeight > 0) {
                            Feature(
                                (weightedSumR / totalWeight).toInt(),
                                (weightedSumG / totalWeight).toInt(),
                                (weightedSumB / totalWeight).toInt(),
                                (weightedSumX / totalWeight).toInt(),
                                (weightedSumY / totalWeight).toInt()
                            )
                        } else {
                            mean
                        }


                        // Has mean shifted
                        if (mean.euclidean_distance(newMean) > threshold) {
                            hasShifted = true
                        }
                        mean = newMean
                    } while (hasShifted && iterations < maxIters)
                    means[idx] = mean
                    resultBitmap[x, y] = Color.rgb(mean.r, mean.g, mean.b)
                }
            }


            return SegmentedImage(resultBitmap, means)
        }

        // Image segmentation using deeplabV3 and mediapipe
        fun segment_deeplab(img: Bitmap, context: Context): ByteArray {
            // Create the segmenter object from options
            // set running mode, output type and model path
            val options =
                ImageSegmenterOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder().setModelAssetPath("deeplab_v3.tflite").build())
                    .setRunningMode(RunningMode.IMAGE)
                    .setOutputCategoryMask(true)
                    .setOutputConfidenceMasks(false)
                    .build()
            val imageSegmenter = ImageSegmenter.createFromOptions(context, options)

            // Convert input bitmap to MPImage
            val mpImage = BitmapImageBuilder(img).build()
            val width = mpImage.width
            val height = mpImage.height

            // Image Segmentation
            val segmenterResult = imageSegmenter.segment(mpImage)

            // Obtain category mask (MPImage)
            val categoryMask = segmenterResult.categoryMask().get()

            // Obtain results
            val finishTimeMs = SystemClock.uptimeMillis()
            val inferenceTime = finishTimeMs - segmenterResult.timestampMs()
            val bundle = ImageSegmenterHelper.ResultBundle(
                ByteBufferExtractor.extract(categoryMask),
                width,
                height,
                inferenceTime
            )

            // Get mask as byte buffer
            val mask = bundle.results
            mask.rewind()

            // Print distinct category masks
            val maskArray = ByteArray(mask.capacity())
            mask.get(maskArray)

            return maskArray
        }

        // Data class for bounding boxes of objects
        data class BoundingBox(val category: Byte, val xMin: Int, val yMin: Int, val xMax: Int, val yMax: Int)

        fun createBoundingBox(category: Byte, img: ByteArray, width: Int, height: Int) : BoundingBox{
            var minX = width
            var minY = height
            var maxX = -1
            var maxY = -1

            for (i in img.indices){
                if (img[i] != category) continue

                val x = i % width
                val y = i / width

                minX = min(minX, x)
                minY = min(minY, y)
                maxX = max(maxX, x)
                maxY = max(maxY, y)
            }

            return BoundingBox(category, minX, minY, maxX, maxY)
        }
    }
}