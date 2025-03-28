package com.example.bglib

import android.graphics.Color
import androidx.core.graphics.blue
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

class ImageProcessing {
    companion object{



        // TODO: Think of various useful image processing methods, such as:
        /*
         Adaptive Median Filter - removes noise
         Image sharpening
         Morphological operations (erosion, dilation, opening, closing)
         */
        /*

        Here are some potential methods categorized by functionality:

        * Game Interaction:

        captureScreen(region: Rectangle? = null): Bitmap: Captures a screenshot of the game window or a specific region.

        findImage(template: Bitmap, threshold: Double = 0.9): Point?: Locates an image template within the game screen and returns its coordinates.

        sendKeystrokes(text: String): Simulates keyboard input to the game.

        sendMouseClick(x: Int, y: Int, button: MouseButton = MouseButton.LEFT): Simulates a mouse click at the specified coordinates.

        readGameMemory(address: Int, size: Int): ByteArray: Reads data from the game's memory at a specific address.

        writeGameMemory(address: Int, data: ByteArray): Writes data to the game's memory at a specific address.


        * Game Data Processing:

        ocr(image: Bitmap, language: String = "eng"): String: Performs optical character recognition (OCR) on an image to extract text.

        detectObjects(image: Bitmap, model: ObjectDetectionModel): List<DetectedObject>: Detects objects within an image using a pre-trained object detection model.

        analyzeGameLog(logPath: String, patterns: List<Regex>): List<GameEvent>: Parses the game's log file and extracts relevant events based on regular expression patterns.


        * UI and User Interaction:

        showOverlay(content: @Composable () -> Unit): Displays an overlay window on top of the game with custom content using Jetpack Compose.

        playSound(soundFile: String): Plays a sound file to provide feedback or notifications.

        speakText(text: String): Uses text-to-speech to speak information to the user.

        listenForVoiceCommands(keywords: List<String>): String?: Listens for voice commands from the user and returns the recognized keyword.


        * Utility Functions:

        getGameWindow(): Window?: Retrieves the game window handle.

        isGameRunning(): Boolean: Checks if the game is currently running.

        waitForImage(template: Bitmap, timeout: Long = 5000): Boolean: Waits for a specific image to appear on the screen within a timeout period.

        delay(milliseconds: Long): Pauses execution for a specified duration.

         */

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
            val newSize = org.opencv.core.Size(newWidth, newHeight)

            // Adjust the rotation matrix to take into account the translation
            rotationMatrix.put(0, 2, rotationMatrix.get(0, 2)[0] + newWidth / 2 - center.x)
            rotationMatrix.put(1, 2, rotationMatrix.get(1, 2)[0] + newHeight / 2 - center.y)

            // Create the destination Mat
            val destination = Mat(newSize, source.type())

            // Apply the rotation
            Imgproc.warpAffine(source, destination, rotationMatrix, newSize)

            return destination
        }
    }
}