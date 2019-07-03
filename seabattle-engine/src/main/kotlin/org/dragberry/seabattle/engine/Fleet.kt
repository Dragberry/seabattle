package org.dragberry.seabattle.engine

abstract class Fleet<S : Sector>(private val settings: BattleSettings, private val createSector: (Coordinate) -> S) {

    var isAlive: Boolean = true

    val field: List<List<S>> = List(settings.height) { y ->
        List(settings.width) { x -> createSector(Coordinate(x + 1, y + 1)) }
    }

    abstract val ships: MutableList<Ship>

    fun getSector(x: Int, y: Int): S = field[y - 1][x - 1]

    fun getSector(coordinate: Coordinate): S = field[coordinate.y - 1][coordinate.x - 1]

    fun getShip(x: Int, y: Int): Ship? = ships.asSequence().find { it.getSection(x, y) != null }

    fun isDefeated(): Boolean = ships.all { !it.isAlive() }

    fun destroyShip(ship: Ship) = getShipOccupation(ship).forEach { it.isHit = true }

    fun getShipOccupation(ship: Ship): Sequence<Sector> {
        return ship.sections.asSequence().flatMap {
            val x = it.coordinate.x
            val y = it.coordinate.y

            sequence {
                if (x > 1 && y > 1) {
                    yield(getSector(x - 1, y - 1))
                }
                if (y > 1) {
                    yield(getSector(x, y - 1))
                }
                if (x < settings.width && y > 1) {
                    yield(getSector(x + 1, y - 1))
                }
                if (x < settings.width) {
                    yield(getSector(x + 1, y))
                }
                if (x < settings.width && y < settings.height) {
                    yield(getSector(x + 1, y + 1))
                }
                if (y < settings.height) {
                    yield(getSector(x, y + 1))
                }
                if (x > 1 && y < settings.height) {
                    yield(getSector(x - 1, y + 1))
                }
                if (x > 1) {
                    yield(getSector(x - 1, y))
                }
            }
        }
    }
}