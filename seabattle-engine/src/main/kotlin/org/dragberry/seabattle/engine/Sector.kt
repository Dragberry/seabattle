package org.dragberry.seabattle.engine

interface Sector {
    val coordinate: Coordinate
    var isHit: Boolean
    var isOccupied: Boolean
}

data class LocalSector(
    override val coordinate: Coordinate,
    override var isHit: Boolean = false,
    override var isOccupied: Boolean = false,
    var hFreeSize: Int = 0,
    var vFreeSize: Int = 0
) : Sector

data class EnemySector(
    override val coordinate: Coordinate,
    override var isHit: Boolean = false,
    override var isOccupied: Boolean = false
) : Sector