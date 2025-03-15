package com.example.bglib

class Player (id: Int) {
    // Name
    var name: String = ""
    // Score
    var score: Int = 0
    // Move count
    var moveCount: Int = 0
    // Cards
    val cards: MutableList<Card> = mutableListOf()

    var isWinner = this.winCondition()

    init {

    }

    fun addCard(card: Card) {
        this.cards.add(card)
    }
    fun removeCard(card: Card) {
        this.cards.remove(card)
    }

    fun updateMove(){
        this.moveCount++
        // TODO: Add logic after each move
    }

    fun updateScore(score: Int) {
        this.score += score
    }

    fun resetScore() {
        this.score = 0
    }

    fun winCondition(): Boolean {
        // TODO: Add win condition logic
        return false
    }

}