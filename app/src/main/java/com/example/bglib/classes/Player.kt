package com.example.bglib.classes

import java.util.concurrent.atomic.AtomicInteger

/************************************************
 * A class for a player.                        *
 * Each player has a unique identifier.         *
 ***********************************************/
open class Player (playerName: String = "") : Playable(playerName) {
    companion object {
        val nextId = AtomicInteger(1)
    }
    override val id: Int = nextId.getAndIncrement()

    open var lives: Int = 100

    override var hasLost: () -> Boolean = { lives <= 0 }

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