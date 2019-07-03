package org.dragberry.seabattle.engine

import java.io.Serializable

sealed class Response : Serializable

class SystemResponse(var event: SystemEvent) : Response()

sealed class FireResponse(val target: Coordinate, val status: Status) : Response() {

    enum class Status {
        HIT, DESTROYED, MISS, ERROR, DEFEAT, REPEAT
    }
}

class HitResponse(target: Coordinate) : FireResponse(target, Status.HIT)

class MissResponse(target: Coordinate) : FireResponse(target, Status.MISS)

class ErrorFireResponse(target: Coordinate) : FireResponse(target, Status.ERROR)

sealed class ShipHolderResponse(target: Coordinate, val ship: Ship, status: Status) : FireResponse(target, status)

class ShipDestroyedResponse(target: Coordinate, ship: Ship) : ShipHolderResponse(target, ship, Status.DESTROYED)

class DefeatResponse(target: Coordinate,  ship: Ship) : ShipHolderResponse(target, ship, Status.DEFEAT)

class RepeatResponse(target: Coordinate) : FireResponse(target, Status.REPEAT)