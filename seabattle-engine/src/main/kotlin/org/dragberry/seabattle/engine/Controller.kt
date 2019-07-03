package org.dragberry.seabattle.engine

interface Controller : NameProvider, BattleSettingsProvider, RoleProvider  {

    suspend fun giveOrder(): Coordinate
}
