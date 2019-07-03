package org.dragberry.seabattle.engine

interface BattleSettingsProvider {

    suspend fun getSettings(): BattleSettings
}

interface NameProvider {

    suspend fun getName(): String
}

interface RoleProvider {

    suspend fun getRole(): Boolean
}