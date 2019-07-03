package org.dragberry.seabattle.engine

interface FleetOwner {

    val fleet: Fleet<LocalSector>

    val enemyFleet: Fleet<EnemySector>
}