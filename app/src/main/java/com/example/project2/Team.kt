package com.example.project2

class Team (id: Int) {
    val players = mutableListOf<Player>()

    var score: Int = 0

    fun updateScore(score: Int) {
        this.score += score
    }

    fun resetScore() {
        this.score = 0
    }
}