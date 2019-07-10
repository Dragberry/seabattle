package org.dragberry.seabattle.client.tornado

import org.dragberry.seabattle.engine.Battle
import org.dragberry.seabattle.engine.Commander
import tornadofx.Controller

class GameController : Controller() {

    var commander: Commander? = null

    var enemy: Commander? = null

    var battle: Battle? = null

    fun createBattle() {
        val commander = commander
        val enemy =  enemy
        if (commander != null && enemy != null) {
            battle = Battle(commander, enemy)
        } else {
            log("Unable to start game: commander and/or enemy are not initialized")
        }
    }

    suspend fun initializeBattle() {
        val battle = battle
        if (battle != null) {
            battle?.initialize()
        } else {
            log("Unable initialize game.")
        }
    }

    fun log(msg: String) {
        println(msg)
    }
}