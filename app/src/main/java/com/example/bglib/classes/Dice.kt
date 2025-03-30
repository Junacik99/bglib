package com.example.bglib.classes

class Dice (val d : Int = 6) {

    val name = "d$d"

    init {
        if (d<1){
            throw IllegalArgumentException("Dice must have at least 1 side")
        }
    }

    fun roll(): Int {
        return (1..d).random()
    }
}