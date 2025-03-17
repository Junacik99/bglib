package com.example.bglib

import java.util.concurrent.atomic.AtomicInteger

open class Playable (val name: String) {
    // A unique identifier for the instances of this class
    companion object {
        protected val nextId = AtomicInteger(1)
    }
    open val id: Int = nextId.getAndIncrement()

    open var score: Int = 0
    open var deltaScore: Int = 0

    // TODO: Override this function variable to check if the team has won
    // Example:
    // val teamA = Team("Team A")
    // teamA.isWinner = { teamA.score >= 1000 }
    open var isWinner: () -> Boolean = { false }

    open var hasLost: () -> Boolean = { false }


    open fun updateScore(score: Int) {
        this.deltaScore = score
        this.score += score
    }

    open fun resetScore() {
        this.score = 0
    }
}