package com.example.bglib.classes

/************************************************
 * Class representing a playing dice            *
 ***********************************************/
class Dice (val d : Int = 6) {

    val name = "d$d"

    init {
        if (d<1){
            throw IllegalArgumentException("Dice must have at least 1 side")
        }
    }

    // Roll a dice - generate random number in the range 1 to d
    fun roll(): Int {
        return (1..d).random()
    }
}