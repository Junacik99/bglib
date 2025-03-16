package com.example.bglib

import java.util.Timer
import kotlin.concurrent.timer

open class Timer (var time: Int = 0){

    private var _hasStarted : Boolean = false
    var hasStarted = { timerTask != null && _hasStarted }

    // Constructor for minutes and seconds
    constructor(minutes: Int, seconds: Int) : this(minutes * 60 + seconds)

    // Constructor for hours, minutes and seconds
    constructor(hours: Int, minutes: Int, seconds: Int) : this(hours * 3600 + minutes * 60 + seconds)

    fun setTime(minutes: Int, seconds: Int){
        this.time = minutes * 60 + seconds
    }
    fun setTime(hours: Int, minutes: Int, seconds: Int){
        this.time = hours * 3600 + minutes * 60 + seconds
    }

    fun getSeconds(): Int{
        return time % 60
    }
    fun getMinutes(): Int {
        return (time - getHours()*3600) / 60
    }
    fun getHours(): Int{
        return time / 3600
    }

    private var timerTask: java.util.Timer? = null

    // function: () -> Unit function to be called every second
    fun start(function: () -> Unit, period: Long = 1000): Timer? {
        _hasStarted = true
        timerTask = timer(period = period) {
            time--
            function()
            if (time <= 0){
                timerTask?.cancel()
                _hasStarted = false
            }
        }
        return timerTask
    }

}