package com.example.bglib

import java.util.concurrent.atomic.AtomicInteger

open class Player (playerName: String = "") : Playable(playerName) {
    companion object {
        val nextId = AtomicInteger(1)
    }
    override val id: Int = nextId.getAndIncrement()

    // Move count
    open var moveCount: Int = 0
    // Cards
    open val cards: MutableList<Card> = mutableListOf()

    open fun addCard(card: Card) {
        this.cards.add(card)
    }
    open fun removeCard(card: Card) {
        this.cards.remove(card)
    }

    open fun updateMove(){
        this.moveCount++
        // TODO: Add logic after each move
    }

}