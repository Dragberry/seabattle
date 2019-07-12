package org.dragberry.seabattle.engine

import kotlinx.coroutines.delay
import java.util.*
import kotlin.random.Random

open class AICommander : LocalCommander(
    object : NameProvider {
        override suspend fun getName(): String = listOf("AI Max", "AI Alex", "AI Sonya", "AI Jax").random()
    },
    object : BattleSettingsProvider {
        override suspend fun getSettings(): BattleSettings = BattleSettings(15, 15, listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1))
    },
    object : RoleProvider {
        override suspend fun getRole(): Boolean = Random.nextBoolean()
    },
    false
) {

    private val availableTargets: MutableList<Coordinate> = LinkedList()

    override fun onSettingsProvided(settings: BattleSettings) {
        super.onSettingsProvided(settings)
        enemyFleet.field.forEach { row ->
            row.forEach { availableTargets.add(it.coordinate) }
        }
    }

    override val shipPlacementStrategy: ShipPlacementStrategy = RandomShipPlacementStrategy()

    private val unfinishedHits: LinkedList<Coordinate> = LinkedList()

    override suspend fun awaitOrder(): Coordinate {
        delay(1000)
        val targets = getPotentialTargets()
        val target = targets[Random.nextInt(0, targets.size)]
        availableTargets.remove(target)
        return target
    }

    override suspend fun attack(): FireResponseEvent {
        val response = super.attack()
        when (response.value) {
            is HitResponse -> {
                unfinishedHits.add(response.value.target)
            }
            is ShipHolderResponse -> {
                unfinishedHits.clear()
                enemyFleet.getShipOccupation(response.value.ship)
                    .map { it.coordinate }
                    .distinct()
                    .forEach { availableTargets.remove(it) }
            }
        }
        return response
    }

    private fun getPotentialTargets(): List<Coordinate> {
        return if (unfinishedHits.isNotEmpty()) {
            unfinishedHits.flatMap { hit ->
                val x = hit.x
                val y = hit.y
                sequence {
//                    if (x > 1 && y > 1) {
//                        yield(enemyFleet.getSector(x - 1, y - 1))
//                    }
                    if (y > 1) {
                        yield(enemyFleet.getSector(x, y - 1))
                    }
//                    if (x < settings.width && y > 1) {
//                        yield(enemyFleet.getSector(x + 1, y - 1))
//                    }
                    if (x < settings.width) {
                        yield(enemyFleet.getSector(x + 1, y))
                    }
//                    if (x < settings.width && y < settings.height) {
//                        yield(enemyFleet.getSector(x + 1, y + 1))
//                    }
                    if (y < settings.height) {
                        yield(enemyFleet.getSector(x, y + 1))
                    }
//                    if (x > 1 && y < settings.height) {
//                        yield(enemyFleet.getSector(x - 1, y + 1))
//                    }
                    if (x > 1) {
                        yield(enemyFleet.getSector(x - 1, y))
                    }
                }.filter { !it.isHit }.map { it.coordinate }.toList()
            }
        } else {
            availableTargets
        }
    }
}