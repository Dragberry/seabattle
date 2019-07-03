package org.dragberry.seabattle.engine

import java.io.Serializable

sealed class Action : Serializable

data class FireAction(val target: Coordinate) : Action()

data class SystemAction(val event: SystemEvent) : Action()

enum class SystemEvent {
    TIMEOUT, EXIT, RESTART
}

