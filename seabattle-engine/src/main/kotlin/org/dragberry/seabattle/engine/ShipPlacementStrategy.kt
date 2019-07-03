package org.dragberry.seabattle.engine

import java.lang.IllegalStateException
import kotlin.random.Random

interface ShipPlacementStrategy {

    fun placeShips(settings: BattleSettings, fleet: Fleet<LocalSector>): MutableList<Ship>
}

class RandomShipPlacementStrategy : ShipPlacementStrategy {

    override fun placeShips(settings: BattleSettings, fleet: Fleet<LocalSector>): MutableList<Ship> {
        return settings.ships.asSequence().sortedByDescending { it }.map { shipSize ->
            val freeSpace = calculateFreeSpaces(settings, fleet)
            var ship: Ship? = null
            val isVertical = Random.nextBoolean()
            val places: List<Sector>? = freeSpace[isVertical]?.get(shipSize) ?: freeSpace[!isVertical]?.get(shipSize)
            if (places != null && places.isNotEmpty()) {
                val sector = places[Random.nextInt(places.size)]
                ship = Ship(sector.coordinate, shipSize, isVertical)
                ship.sections.forEach { fleet.getSector(it.coordinate).isOccupied = true }
            }
            ship ?: throw IllegalStateException("Unable to place all ships!")
        }.toMutableList()
    }

    private fun calculateFreeSpaces(settings: BattleSettings, fleet: Fleet<LocalSector>):  MutableMap<Boolean, MutableMap<Int, MutableList<Sector>>> {
        val availablePlaces: MutableMap<Boolean, MutableMap<Int, MutableList<Sector>>> = mutableMapOf()
        val maxX = settings.width
        val maxY = settings.height
        for (y in maxY downTo 1) {
            for (x in maxX downTo 1) {
                val sector = fleet.getSector(x, y)
                if (sector.isOccupied) {
                    sector.hFreeSize = 0
                    sector.vFreeSize = 0
                } else {
                    val isTopLeftFree = if (y == 1 || x == 1) true else !fleet.getSector(x - 1, y - 1).isOccupied
                    val isTopFree = if (y == 1) true else !fleet.getSector(x, y -1).isOccupied
                    val isTopRightFree = if (y == 1 || x == maxX) true else !fleet.getSector(x + 1, y - 1).isOccupied
                    val isRightFree = if (x == maxX) true else !fleet.getSector(x + 1, y).isOccupied
                    val isBottomRightFree = if (y == maxY || x == maxX) true else !fleet.getSector(x + 1, y + 1).isOccupied
                    val isBottomFree = if (y == maxY) true else !fleet.getSector(x, y + 1).isOccupied
                    val isBottomLeftFree = if (y == maxY || x == 1) true else !fleet.getSector(x - 1, y + 1).isOccupied
                    val isLeftFree = if (x == 1) true else !fleet.getSector(x - 1, y).isOccupied
                    if (isTopLeftFree && isTopFree && isTopRightFree
                        && isRightFree
                        && isBottomRightFree && isBottomFree && isBottomLeftFree
                        && isLeftFree) {
                        sector.hFreeSize = (if (x == maxX) 0 else fleet.getSector(x + 1, y).hFreeSize) + 1
                        sector.vFreeSize = (if (y == maxY) 0 else fleet.getSector(x, y + 1).vFreeSize) + 1
                        val maxShipSize = settings.ships.max() ?: 1
                        addPlaces(availablePlaces.computeIfAbsent(true) { mutableMapOf() }, sector, LocalSector::vFreeSize, maxShipSize)
                        addPlaces(availablePlaces.computeIfAbsent(false) { mutableMapOf() }, sector, LocalSector::hFreeSize, maxShipSize)
                    } else {
                        sector.hFreeSize = 0
                        sector.vFreeSize = 0
                    }
                }
            }
        }
        return availablePlaces
    }

    private fun addPlaces(
        availablePlaces: MutableMap<Int, MutableList<Sector>>,
        sector: LocalSector, counterProvider: (LocalSector) -> Int,
        shipSize: Int
    ) {
        if (counterProvider(sector) >= shipSize) {
            availablePlaces.computeIfAbsent(shipSize) { mutableListOf() }.add(sector)
        }
        if (shipSize > 1) {
            addPlaces(availablePlaces, sector, counterProvider, shipSize - 1)
        }

    }
}