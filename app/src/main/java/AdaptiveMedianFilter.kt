data class Pixel(val red: UByte, val green: UByte, val blue: UByte)

fun encode(img:  Array<Array<Pixel>>): IntArray {
    val encodedImg = mutableListOf<Int>()

    for (row in img){
        for (pixel in row) {
            val r = pixel.red.toInt()
            val g = pixel.green.toInt()
            val b = pixel.blue.toInt()

            val rBits = Integer.toBinaryString(r).padStart(8, '0')
            val gBits = Integer.toBinaryString(g).padStart(8, '0')
            val bBits = Integer.toBinaryString(b).padStart(8, '0')

            var combinedChannel = ""
            for (k in 0 until 8) {
                combinedChannel += rBits[k]
                combinedChannel += gBits[k]
                combinedChannel += bBits[k]
            }

            encodedImg.add(Integer.parseInt(combinedChannel, 2))
        }
    }

    return encodedImg.toIntArray()
}

fun decode(combinedImage: IntArray, originalShape: IntArray): Array<Array<Pixel>> {
    val H = originalShape[0]
    val W = originalShape[1]

    val decodedImage = Array(H) { Array(W) { Pixel(0.toUByte(), 0.toUByte(), 0.toUByte()) } }

    for (j in 0 until H * W) {
        val encodedBits = combinedImage[j]

        var rByte = ""
        var gByte = ""
        var bByte = ""

        for (i in 0 until 8) {
            val bBit = (encodedBits shr (i * 3)) and 1
            val gBit = (encodedBits shr (i * 3 + 1)) and 1
            val rBit = (encodedBits shr (i * 3 + 2)) and 1

            rByte += rBit
            gByte += gBit
            bByte += bBit
        }

        rByte = rByte.reversed()
        gByte = gByte.reversed()
        bByte = bByte.reversed()

        val row = j / W
        val col = j % W

        decodedImage[row][col] = Pixel(
            Integer.parseInt(rByte, 2).toUByte(),
            Integer.parseInt(gByte, 2).toUByte(),
            Integer.parseInt(bByte, 2).toUByte()
        )
    }

    return decodedImage
}

fun main(){
    val img: Array<Array<Pixel>> = Array(3) {
        Array(3) {
            Pixel(
                (0..255).random().toUByte(),
                (0..255).random().toUByte(),
                (0..255).random().toUByte()
            )
        }
    }

    for (row in img){
        for (pixel in row)
            println(pixel)
        println()
    }

    println("Encoding")
    val encodedImg = encode(img)
    println(encodedImg.contentToString())
    println()



    println("Decoding")
    // System.loadLibrary( Core.NATIVE_LIBRARY_NAME )
    val decodedImg = decode(encodedImg, intArrayOf(3, 3))
    for (row in decodedImg) {
        for (pixel in row)
            println(pixel)
        println()
    }

    println("Two images are equal: ${img.contentDeepEquals(decodedImg)}")
}