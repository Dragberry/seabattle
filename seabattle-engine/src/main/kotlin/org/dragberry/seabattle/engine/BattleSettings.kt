package org.dragberry.seabattle.engine

import java.io.Serializable

data class BattleSettings(
    val width: Int,
    val height: Int,
    val ships: List<Int>
) : Serializable