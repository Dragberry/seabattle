package org.dragberry.seabattle.engine

import java.io.Serializable

class Ship(start: Coordinate, size: Int, isVertical: Boolean) : Serializable {

    val sections: List<Section> = List(size) {
        if (isVertical) {
            Section(start.x, start.y + it)
        } else {
            Section(start.x + it, start.y)
        }
    }

    fun isAlive(): Boolean = sections.any { it.isAlive }

    fun getSection(x: Int, y: Int): Section? {
        return sections.asSequence().find { it.coordinate.x == x && it.coordinate.y == y }
    }

    fun contains(x: Int, y: Int): Boolean {
        return getSection(x, y) != null
    }

    inner class Section(x: Int, y: Int, var isAlive: Boolean = true) : Serializable {

        val coordinate: Coordinate = Coordinate(x, y)

        fun getShip(): Ship {
            return this@Ship
        }

        override fun toString(): String {
            return "Section(isAlive = $isAlive, coordinate = $coordinate)"
        }
    }
}