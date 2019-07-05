package org.dragberry.seabattle.engine

interface CommanderController : NameProvider, BattleSettingsProvider, RoleProvider  {

    suspend fun giveOrder(): Coordinate
}
