package org.dragberry.seabattle.client.tornado

import javafx.application.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dragberry.seabattle.engine.Battle
import org.dragberry.seabattle.engine.Commander
import org.dragberry.seabattle.engine.Logger
import org.dragberry.seabattle.engine.LoggerDelegate
import tornadofx.Controller

class GameController : Controller() {

    private val logger by LoggerDelegate()

    var commander: Commander? = null

    var enemy: Commander? = null

    var battle: Battle? = null

    fun createBattle() {
        val commander = commander
        val enemy =  enemy
        if (commander != null && enemy != null) {
            battle = Battle(commander, enemy)
        } else {
            logger.log("Unable start game: commander and/or enemy are not initialized")
        }
    }

    suspend fun initializeBattle(onInitialized: (Battle) -> Unit) {
        val battle = battle
        if (battle != null) {
            battle.initialize()
            withContext(Dispatchers.Main) { onInitialized(battle) }
        } else {
            logger.log("Unable initialize game.")
        }
    }

    suspend fun play(onEveryStep: (Battle) -> Unit) {
        val battle = battle
        if (battle != null) {
            battle.play {
                withContext(Dispatchers.Main) { onEveryStep(battle) }
            }
        } else {
            logger.log("Unable play game.")
        }
    }

}