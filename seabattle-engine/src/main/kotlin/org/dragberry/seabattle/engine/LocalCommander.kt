package org.dragberry.seabattle.engine

import java.lang.IllegalStateException

abstract class LocalCommander(
    protected open val nameProvider: NameProvider,
    protected open val settingsProvider: BattleSettingsProvider,
    protected open val roleProvider: RoleProvider,
    override val isHidden: Boolean
) : Commander(), FleetOwner {

    override val fleet: Fleet<LocalSector> by lazy {
        if (tempFleet != null) tempFleet as Fleet else throw IllegalStateException("Fleet hasn't been initialized")
    }

    private var tempFleet: Fleet<LocalSector>? = null

    override val enemyFleet: Fleet<EnemySector> by lazy {
        if (tempEnemyFleet != null) tempEnemyFleet as Fleet else throw IllegalStateException("Enemy fleet hasn't been initialized")
    }

    private var tempEnemyFleet: Fleet<EnemySector>? = null

    override fun onSettingsProvided(settings: BattleSettings) {
        tempFleet = object : Fleet<LocalSector>(settings, { LocalSector(it) }) {
            override val ships: MutableList<Ship> by lazy { shipPlacementStrategy.placeShips(settings, this) }
        }
        tempEnemyFleet = object : Fleet<EnemySector>(settings, { EnemySector(it) }) {
            override val ships: MutableList<Ship> = mutableListOf()
        }
    }

    override suspend fun provideName(): String = nameProvider.getName()

    override suspend fun provideSettings(): BattleSettings = settingsProvider.getSettings()

    override suspend fun provideRole(): Boolean = roleProvider.getRole()

    override val isAlive: Boolean
        get() = fleet.isAlive && enemyFleet.isAlive

    protected abstract val shipPlacementStrategy: ShipPlacementStrategy

    override suspend fun attack(order: FireEvent) { }

    override suspend fun onResponse(event: FireResponseEvent) {
        val response = event.value
        when (response) {
            is FireResponse -> {
                when (response) {
                    is MissResponse,
                    is HitResponse,
                    is ShipHolderResponse -> {
                        val attackedSector = enemyFleet.getSector(response.target)
                        attackedSector.isHit = true
                        when (response) {
                            is HitResponse -> {
                                attackedSector.isOccupied = true
                            }
                            is ShipHolderResponse -> {
                                enemyFleet.ships.add(response.ship)
                                response.ship.sections.forEach {
                                    enemyFleet.getSector(it.coordinate).isOccupied = true
                                }
                                enemyFleet.destroyShip(response.ship)
                                when (response) {
                                    is DefeatResponse -> {
                                        enemyFleet.isAlive = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is SystemResponse -> {
                println("System response")
            }
        }
    }

    override suspend fun underAttack(event: FireEvent) {
        val target = event.value
        events.send(FireResponseEvent(try {
            val sector = fleet.getSector(target)
            if (sector.isHit) {
                RepeatResponse(target)
            } else {
                sector.isHit = true
                val shipSection = fleet.ships.asSequence()
                    .flatMap { it.sections.asSequence() }
                    .find { it.coordinate == sector.coordinate }
                if (shipSection != null) {
                    shipSection.isAlive = false
                    val ship = shipSection.getShip()
                    if (ship.isAlive()) {
                        HitResponse(target)
                    } else {
                        if (fleet.isDefeated()) {
                            fleet.isAlive = false
                            DefeatResponse(target, ship)
                        } else {
                            fleet.destroyShip(ship)
                            ShipDestroyedResponse(target, ship)
                        }
                    }
                } else {
                    MissResponse(target)
                }
            }
        } catch (exc: ArrayIndexOutOfBoundsException) {
            ErrorFireResponse(target)

        }))
    }

    private fun showMessage(msg: String) {
        println("\n-->For @$name: $msg\n")
    }

}