package com.example.bglib

import java.util.concurrent.atomic.AtomicInteger

open class Team (teamName: String = "") : Playable(teamName) {
    companion object {
        val nextId = AtomicInteger(1)
    }
    override val id: Int = nextId.getAndIncrement()

    open val players = mutableListOf<Player>()

    // TODO: Override these methods as needed
    open fun updateScore(){
        this.score = players.sumOf { it.score }
    }
}