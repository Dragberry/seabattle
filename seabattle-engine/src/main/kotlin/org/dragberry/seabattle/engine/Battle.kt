package org.dragberry.seabattle.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.system.exitProcess

class Battle(
    commander: Commander,
    enemyCommander: Commander
) {

    private val logger: Logger by LoggerDelegate()

    val commander: Commander = commander

    val enemyCommander: Commander = enemyCommander

    val settings: BattleSettings by lazy { commander.settings }

    inner class Roles {

        var isEnemyAggressor = false

        fun aggressor(): Commander = if (isEnemyAggressor) enemyCommander else commander

        fun victim(): Commander = if (isEnemyAggressor) commander else enemyCommander

        fun swap() {
            isEnemyAggressor = !isEnemyAggressor
        }
    }

    var round = 1

    val roles: Roles = Roles()

    suspend fun initialize() = coroutineScope {
        val initialized = async {
            commander.shakeHandsWith(enemyCommander)
        }

        val enemyInitialized = async {
            enemyCommander.shakeHandsWith(commander)
        }

        if (initialized.await() && enemyInitialized.await()) {
            roles.isEnemyAggressor = enemyCommander.isAggressor
        } else {
            exitProcess(0)
        }
    }

    suspend fun play(onEveryStep: suspend (Battle) -> Unit = { }) {
        onEveryStep(this)
        do {
            playRound()
            onEveryStep(this)
            round++
        } while (isGoingOn())
    }

    private suspend fun playRound() {
        coroutineScope {
            val aggressorJob = async(Dispatchers.IO) { attack() }
            val victimJob = async(Dispatchers.IO) { underAttack() }
            val response = aggressorJob.await().value
            victimJob.await()
            when (response) {
                is MissResponse -> onMiss()
                is HitResponse -> onHit()
                is ShipDestroyedResponse -> onShipDestroy(response.ship)
                is DefeatResponse -> onDefeat(response.ship)
                is SystemResponse -> {
                    when (response.event) {
                        SystemEvent.EXIT -> exitProcess(0)
                        SystemEvent.TIMEOUT -> exitProcess(0)
                        SystemEvent.RESTART -> exitProcess(0)
                    }
                }
            }
        }
    }

    private suspend fun attack(): FireResponseEvent {
        logger.log("${roles.aggressor().name}:\tI'm attacking...")
        return roles.aggressor().attack()
    }

    private suspend fun underAttack() {
        logger.log("${roles.victim().name}:\tI'm under attack...")
        println()
        roles.victim().underAttack()
    }

    private fun onMiss() {
        logger.log("${roles.aggressor().name}:\tI missed")
        roles.swap()
    }

    private fun onHit() {
        logger.log("${roles.aggressor().name}:\tI hit")
    }

    private fun onShipDestroy(ship: Ship) {
        logger.log("${roles.aggressor().name}:\tI destroyed a ${ship.sections.size}-sized ship")
    }

    private fun onDefeat(ship: Ship) {
        logger.log("${roles.aggressor().name}:\tI destroyed a ${ship.sections.size}-sized ship")
        logger.log("${roles.aggressor().name}:\tI won!")
        logger.log("${roles.victim().name}:\tI lost!")
        exitProcess(0)
    }

    private fun isGoingOn(): Boolean = commander.isAlive && enemyCommander.isAlive

}