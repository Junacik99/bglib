package com.example.bglib

// Achievement class designed as in the article from Juho Hamari and Veikko Eranti
// Framework for Designing and Evaluating Game Achievements (2011)
class Achievement (
    val name: String,                                       // Name of the achievement
    val description: String = "",                           // Description of the achievement
    val imageLocked: Int,                                   // Image to be displayed when the achievement is still locked
    val imageUnlocked: Int,                                 // Image to be displayed when the achievement is unlocked
    val trigger: () -> Boolean,                             // Action to check if the achievement is triggered
    val requirements: List<() -> Boolean> = emptyList(),    // List of requirements of availability or because the achievement requires it
    val conditions: List<() -> Boolean> = emptyList(),      // List of conditions of the achievement (eg. is player using weapon x?)
    val multiplier: Int = 1,                                // How many times should the trigger be triggered in order to unlock the achievement
    val reward: () -> Unit = {}                             // An award action to be performed when the achievement is unlocked
    ) {

    var unlocked = false
    var image = imageLocked
    var progress = 0

    fun checkRequirements(): Boolean {
        if (requirements.isEmpty()) return true
        for (requirement in requirements) {
            if (!requirement()) {
                return false
            }
        }
        return true
    }

    fun checkConditions(): Boolean {
        if (conditions.isEmpty()) return true
        for (condition in conditions) {
            if (!condition()) {
                return false
            }
        }
        return true
    }

    fun checkTrigger(): Boolean {
        return checkRequirements() && checkConditions() && trigger()
    }

    fun unlock() {
        unlocked = true
        image = imageUnlocked
        reward()
    }

    fun lock() {
        unlocked = false
        image = imageLocked
    }

    fun check(){
        if (unlocked) return
        if (checkTrigger()) {
            progress++
        }
        if (progress >= multiplier) {
            unlock()
        }
    }

}