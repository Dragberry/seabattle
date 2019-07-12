package org.dragberry.seabattle.client.tornado

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dragberry.seabattle.engine.Battle
import org.dragberry.seabattle.engine.Commander
import org.dragberry.seabattle.engine.LoggerDelegate
import org.dragberry.seabattle.engine.Response
import tornadofx.Controller

class GameController : Controller() {

    val logger by LoggerDelegate()

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

    suspend fun play(onEveryStep: (Battle) -> Unit, onResponse: (Response) -> Unit) {
        val battle = battle
        if (battle != null) {
            battle.onEveryStep = { withContext(Dispatchers.Main) { onEveryStep(it) } }
            battle.onResponse = { withContext(Dispatchers.Main) { onResponse(it) } }
            battle.play()
        } else {
            logger.log("Unable play game.")
        }
    }

}