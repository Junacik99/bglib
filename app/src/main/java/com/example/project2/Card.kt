package com.example.project2

import android.os.Parcel
import android.os.Parcelable
import org.opencv.core.Rect

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