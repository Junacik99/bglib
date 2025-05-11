package com.library.bglib.classes

import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable
import com.google.mlkit.vision.text.Text
import org.opencv.core.Rect
import kotlin.collections.toTypedArray

/************************************************
 * A parcelable class of a Card                 *
 ***********************************************/
data class Card(public var boundingBox: Rect, public var text: String = "") : Parcelable {
    public var ScreenPosition: ScreenPosition = ScreenPosition(boundingBox.x, boundingBox.y)    // Screen position
    public var Dimensions: Dimensions = Dimensions(boundingBox.width, boundingBox.height)                         // Axis-aligned Dimensions
    public var GridPosition: GridPosition = GridPosition(0, 0)                                                 // Position in the grid

    constructor(parcel: Parcel) : this(
        Rect(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt()),
        parcel.readString() ?: ""
    ) {
        ScreenPosition = parcel.readParcelable(ScreenPosition::class.java.classLoader)!!
        Dimensions = parcel.readParcelable(Dimensions::class.java.classLoader)!!
        GridPosition = parcel.readParcelable(GridPosition::class.java.classLoader)!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(boundingBox.x)
        parcel.writeInt(boundingBox.y)
        parcel.writeInt(boundingBox.width)
        parcel.writeInt(boundingBox.height)
        parcel.writeString(text)
        parcel.writeParcelable(ScreenPosition, flags)
        parcel.writeParcelable(Dimensions, flags)
        parcel.writeParcelable(GridPosition, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }
}

// Parcelable ScreenPosition
data class ScreenPosition(var x: Int, var y: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(x)
        parcel.writeInt(y)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScreenPosition> {
        override fun createFromParcel(parcel: Parcel): ScreenPosition {
            return ScreenPosition(parcel)
        }

        override fun newArray(size: Int): Array<ScreenPosition?> {
            return arrayOfNulls(size)
        }
    }
}

// Parcelable Dimensions
data class Dimensions(var width: Int, var height: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Dimensions> {
        override fun createFromParcel(parcel: Parcel): Dimensions {
            return Dimensions(parcel)
        }

        override fun newArray(size: Int): Array<Dimensions?> {
            return arrayOfNulls(size)
        }
    }
}

// Parcelable GridPosition
data class GridPosition(var row: Int, var col: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(row)
        parcel.writeInt(col)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GridPosition> {
        override fun createFromParcel(parcel: Parcel): GridPosition {
            return GridPosition(parcel)
        }

        override fun newArray(size: Int): Array<GridPosition?> {
            return arrayOfNulls(size)
        }
    }
}

// Parcelable opencv.core.Rect
data class RectParcelable(val rect: Rect) :Parcelable{
    constructor(parcel: Parcel) : this (
        Rect(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt())
    ){

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(rect.x)
        parcel.writeInt(rect.y)
        parcel.writeInt(rect.width)
        parcel.writeInt(rect.height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RectParcelable> {
        override fun createFromParcel(parcel: Parcel): RectParcelable {
            return RectParcelable(parcel)
        }

        override fun newArray(size: Int): Array<RectParcelable?> {
            return arrayOfNulls(size)
        }
    }

}

/////// PARCELABLE TEXT ///////

// Parcelable TextElement
data class ParcelableTextElement(
    val text: String,
    val boundingBox: RectParcelable?,
    val cornerPoints: Array<IntArray>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(RectParcelable::class.java.classLoader),
        readCornerPoints(parcel)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeParcelable(boundingBox, flags)
        writeCornerPoints(parcel, cornerPoints)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableTextElement> {
            override fun createFromParcel(parcel: Parcel): ParcelableTextElement {
                return ParcelableTextElement(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableTextElement?> {
                return arrayOfNulls(size)
            }
        }

        fun readCornerPoints(parcel: Parcel): Array<IntArray>? {
            val size = parcel.readInt()
            return if (size == -1) {
                null
            } else {
                Array(size) {
                    parcel.createIntArray()!!
                }
            }
        }

        fun writeCornerPoints(parcel: Parcel, cornerPoints: Array<IntArray>?) {
            if (cornerPoints == null) {
                parcel.writeInt(-1)
            } else {
                parcel.writeInt(cornerPoints.size)
                cornerPoints.forEach { parcel.writeIntArray(it) }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableTextElement

        if (text != other.text) return false
        if (boundingBox != other.boundingBox) return false
        if (!cornerPoints.contentDeepEquals(other.cornerPoints)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (cornerPoints?.contentDeepHashCode() ?: 0)
        return result
    }
}

// Parcelable TextLine
data class ParcelableTextLine(
    val text: String,
    val boundingBox: RectParcelable?,
    val cornerPoints: Array<IntArray>?,
    val elements: List<ParcelableTextElement>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(RectParcelable::class.java.classLoader),
        ParcelableTextElement.readCornerPoints(parcel),
        parcel.createTypedArrayList(ParcelableTextElement.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeParcelable(boundingBox, flags)
        ParcelableTextElement.writeCornerPoints(parcel, cornerPoints)
        parcel.writeTypedList(elements)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableTextLine> {
            override fun createFromParcel(parcel: Parcel): ParcelableTextLine {
                return ParcelableTextLine(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableTextLine?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableTextLine

        if (text != other.text) return false
        if (boundingBox != other.boundingBox) return false
        if (!cornerPoints.contentDeepEquals(other.cornerPoints)) return false
        if (elements != other.elements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (cornerPoints?.contentDeepHashCode() ?: 0)
        result = 31 * result + elements.hashCode()
        return result
    }
}

// Parcelable TextBlock
data class ParcelableTextBlock(
    val text: String,
    val boundingBox: RectParcelable?,
    val cornerPoints: Array<IntArray>?,
    val lines: List<ParcelableTextLine>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(RectParcelable::class.java.classLoader),
        ParcelableTextElement.readCornerPoints(parcel),
        parcel.createTypedArrayList(ParcelableTextLine.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeParcelable(boundingBox, flags)
        ParcelableTextElement.writeCornerPoints(parcel, cornerPoints)
        parcel.writeTypedList(lines)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableTextBlock> {
            override fun createFromParcel(parcel: Parcel): ParcelableTextBlock {
                return ParcelableTextBlock(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableTextBlock?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableTextBlock

        if (text != other.text) return false
        if (boundingBox != other.boundingBox) return false
        if (!cornerPoints.contentDeepEquals(other.cornerPoints)) return false
        if (lines != other.lines) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + (boundingBox?.hashCode() ?: 0)
        result = 31 * result + (cornerPoints?.contentDeepHashCode() ?: 0)
        result = 31 * result + lines.hashCode()
        return result
    }
}

// Parcelable Text
data class ParcelableText(
    val text: String,
    val textBlocks: List<ParcelableTextBlock>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createTypedArrayList(ParcelableTextBlock.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
        parcel.writeTypedList(textBlocks)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<ParcelableText> {
            override fun createFromParcel(parcel: Parcel): ParcelableText {
                return ParcelableText(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableText?> {
                return arrayOfNulls(size)
            }
        }
    }
}

// Convert MLKitText to ParcelableText
fun convertMLKitTextToParcelable(mlKitText: Text): ParcelableText {
    val parcelableTextBlocks = mlKitText.textBlocks.map { convertTextBlock(it) }
    return ParcelableText(mlKitText.text, parcelableTextBlocks)
}

// Convert TextBlock to ParcelableTextBlock
fun convertTextBlock(textBlock: Text.TextBlock): ParcelableTextBlock {
    val parcelableLines = textBlock.lines.map { convertTextLine(it) }
    return ParcelableTextBlock(
        textBlock.text,
        androidRectToOpenCVRect(textBlock.boundingBox)?.let { RectParcelable(it) },
        convertPointArrayToIntArray(textBlock.cornerPoints),
        parcelableLines
    )
}

// Convert TextLine to ParcelableTextLine
fun convertTextLine(textLine: Text.Line): ParcelableTextLine {
    val parcelableElements = textLine.elements.map { convertTextElement(it) }
    return ParcelableTextLine(
        textLine.text,
        androidRectToOpenCVRect(textLine.boundingBox)?.let { RectParcelable(it) },
        convertPointArrayToIntArray(textLine.cornerPoints),
        parcelableElements
    )
}

// Convert TextElement to ParcelableTextElement
fun convertTextElement(textElement: Text.Element): ParcelableTextElement {
    return ParcelableTextElement(
        textElement.text,
        androidRectToOpenCVRect(textElement.boundingBox)?.let { RectParcelable(it) },
        convertPointArrayToIntArray(textElement.cornerPoints)
    )
}

// Convert android.graphics.Rect to org.opencv.core.Rect
fun androidRectToOpenCVRect(androidRect: android.graphics.Rect?): Rect? {
    return if (androidRect != null) {
        Rect(androidRect.left, androidRect.top, androidRect.width(), androidRect.height())
    } else {
        null
    }
}

// Convert org.opencv.core.Rect to android.graphics.Rect
fun openCVRectToAndroidRect(openCVRect: Rect?): android.graphics.Rect? {
    return if (openCVRect != null) {
        android.graphics.Rect(openCVRect.x, openCVRect.y, openCVRect.x + openCVRect.width, openCVRect.y + openCVRect.height)
    } else {
        null
    }
}

// Convert Array<Point?> to Array<IntArray>
fun convertPointArrayToIntArray(points: Array<Point?>?): Array<IntArray>? {
    return points?.mapNotNull { point ->
        if (point != null) {
            intArrayOf(point.x.toInt(), point.y.toInt())
        } else {
            null // Handle null points if needed
        }
    }?.toTypedArray()
}