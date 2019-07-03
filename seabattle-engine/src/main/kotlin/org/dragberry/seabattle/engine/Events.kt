package org.dragberry.seabattle.engine

import java.io.Serializable

sealed class Event<T>(val value: T) : Serializable

class ExitEvent : Event<Unit>(Unit)

class NameEvent(name: String) : Event<String>(name)

class SettingsEvent(settings: BattleSettings) : Event<BattleSettings>(settings)

class RoleEvent(isAggressor: Boolean) : Event<Boolean>(isAggressor)

class InitializationEvent(initialized: Boolean) : Event<Boolean>(initialized)

class FireEvent(target: Coordinate): Event<Coordinate>(target)

class FireResponseEvent(response: Response): Event<Response>(response)