package com.example.bglib.classes

import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable
import com.google.mlkit.vision.text.Text
import org.opencv.core.Rect
import kotlin.collections.filterNotNull
import kotlin.collections.toTypedArray

data class screenPos(var x: Int, var y: Int) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<screenPos> {
        override fun createFromParcel(parcel: Parcel): screenPos {
            return screenPos(parcel)
        }

        override fun newArray(size: Int): Array<screenPos?> {
            return arrayOfNulls(size)
        }
    }
}

data class size(var width: Int, var height: Int) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<size> {
        override fun createFromParcel(parcel: Parcel): size {
            return size(parcel)
        }

        override fun newArray(size: Int): Array<size?> {
            return arrayOfNulls(size)
        }
    }
}

data class gridPos(var row: Int, var col: Int) : Parcelable {
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

    companion object CREATOR : Parcelable.Creator<gridPos> {
        override fun createFromParcel(parcel: Parcel): gridPos {
            return gridPos(parcel)
        }

        override fun newArray(size: Int): Array<gridPos?> {
            return arrayOfNulls(size)
        }
    }
}

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
}

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
}

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
}

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

fun convertMLKitTextToParcelable(mlKitText: Text): ParcelableText {
    val parcelableTextBlocks = mlKitText.textBlocks.map { convertTextBlock(it) }
    return ParcelableText(mlKitText.text, parcelableTextBlocks)
}

fun convertTextBlock(textBlock: Text.TextBlock): ParcelableTextBlock {
    val parcelableLines = textBlock.lines.map { convertTextLine(it) }
    return ParcelableTextBlock(
        textBlock.text,
        androidRectToOpenCVRect(textBlock.boundingBox)?.let { RectParcelable(it) },
        convertPointArrayToIntArray(textBlock.cornerPoints),
        parcelableLines
    )
}

fun convertTextLine(textLine: Text.Line): ParcelableTextLine {
    val parcelableElements = textLine.elements.map { convertTextElement(it) }
    return ParcelableTextLine(
        textLine.text,
        androidRectToOpenCVRect(textLine.boundingBox)?.let { RectParcelable(it) },
        convertPointArrayToIntArray(textLine.cornerPoints),
        parcelableElements
    )
}

fun convertTextElement(textElement: Text.Element): ParcelableTextElement {
    return ParcelableTextElement(
        textElement.text,
        androidRectToOpenCVRect(textElement.boundingBox)?.let { RectParcelable(it) },
        convertPointArrayToIntArray(textElement.cornerPoints)
    )
}
fun androidRectToOpenCVRect(androidRect: android.graphics.Rect?): Rect? {
    return if (androidRect != null) {
        Rect(androidRect.left, androidRect.top, androidRect.width(), androidRect.height())
    } else {
        null
    }
}

fun openCVRectToAndroidRect(openCVRect: Rect?): android.graphics.Rect? {
    return if (openCVRect != null) {
        android.graphics.Rect(openCVRect.x, openCVRect.y, openCVRect.x + openCVRect.width, openCVRect.y + openCVRect.height)
    } else {
        null
    }
}
fun convertPointArrayToIntArray(points: Array<Point?>?): Array<IntArray>? {
    return points?.map { point ->
        if (point != null) {
            intArrayOf(point.x.toInt(), point.y.toInt())
        } else {
            null // Handle null points if needed
        }
    }?.filterNotNull()?.toTypedArray()
}

data class Card(public var boundingBox: Rect, public var text: String = "") : Parcelable {
    public var screenPos: screenPos = screenPos(boundingBox.x, boundingBox.y)
    public var size: size = size(boundingBox.width, boundingBox.height)
    public var gridPos: gridPos = gridPos(0, 0)

    constructor(parcel: Parcel) : this(
        Rect(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt()),
        parcel.readString() ?: ""
    ) {
        screenPos = parcel.readParcelable(screenPos::class.java.classLoader)!!
        size = parcel.readParcelable(size::class.java.classLoader)!!
        gridPos = parcel.readParcelable(gridPos::class.java.classLoader)!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(boundingBox.x)
        parcel.writeInt(boundingBox.y)
        parcel.writeInt(boundingBox.width)
        parcel.writeInt(boundingBox.height)
        parcel.writeString(text)
        parcel.writeParcelable(screenPos, flags)
        parcel.writeParcelable(size, flags)
        parcel.writeParcelable(gridPos, flags)
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