package org.dragberry.seabattle.engine

class Captain(
    private val controller: CommanderController,
    isHidden: Boolean
) : LocalCommander(controller, controller, controller, isHidden) {

    override val shipPlacementStrategy: ShipPlacementStrategy = RandomShipPlacementStrategy()

    override suspend fun awaitOrder(): Coordinate {
        return controller.giveOrder()
    }

}

