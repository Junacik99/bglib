package com.example.bglib

class Team (id: Int) {
    val players = mutableListOf<Player>()

    var score: Int = 0

    var isWinner = this.winCondition()

    // TODO: Override these methods as needed
    fun updateScore(){
        this.score = players.sumOf { it.score }
    }

    fun updateScore(score: Int) {
        this.score += score
    }

    fun resetScore() {
        this.score = 0
    }

    fun winCondition(): Boolean {
        return false
    }
}