package org.dragberry.seabattle.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import java.lang.IllegalStateException

abstract class Commander(protected val defaultName: String = "Dummy commander") {

    protected val events = Channel<Event<out Any>>()

    protected var enemy: Commander? = null

    private var tempName: String? = null

    val name: String by lazy { if (tempName != null) tempName as String else throw IllegalStateException("Name hasn't been provided!") }

    private var tempSettings: BattleSettings? = null

    val settings: BattleSettings by lazy { if (tempSettings != null) tempSettings as BattleSettings else throw IllegalStateException("Settings have not been provided!") }

    private var tempIsAggressor: Boolean? = null

    val isAggressor: Boolean by lazy { if (tempIsAggressor != null) tempIsAggressor as Boolean else throw IllegalStateException("IsAggressor has not been provided!") }

    suspend fun shakeHandsWith(enemy: Commander): Boolean {
        this.enemy = enemy

        return coroutineScope {

            tempName = StepExecutor("Name",
                { provideName() },
                { NameEvent(it) }).execute().first

            val settingsResult = StepExecutor("Settings",
                { provideSettings() },
                { SettingsEvent(it) }).execute()

            tempSettings = settingsResult.first
            onSettingsProvided(settings)
            val accepted = settingsResult.first == settingsResult.second

            var rolesResolved = false
            if (accepted) {
                var attempt = 1
                do {
                    val rolesResult = StepExecutor("Roles",
                        { provideRole() },
                        { RoleEvent(it) }).execute()
                    rolesResolved = rolesResult.first != rolesResult.second
                    tempIsAggressor = if (rolesResolved) rolesResult.first else null
                } while (attempt <= 5 && !rolesResolved)
            }

            val initResult = StepExecutor("Initialization",
                { provideInitializationStatus(accepted, rolesResolved) },
                { InitializationEvent(it) }).execute()

            initResult.first && initResult.second
        }
    }

    abstract suspend fun provideName(): String

    abstract suspend fun provideSettings(): BattleSettings

    abstract suspend fun provideRole(): Boolean

    open suspend fun provideInitializationStatus(accepted: Boolean, rolesResolved: Boolean): Boolean {
        return accepted && rolesResolved
    }

    protected open fun onSettingsProvided(settings: BattleSettings) { }

    open fun onEnemyEvent(event: Event<out Any>) { }

    abstract val isAlive: Boolean

    abstract val isHidden: Boolean

    abstract suspend fun awaitOrder(): Coordinate

    open suspend fun attack(): FireResponseEvent {
        val order = FireEvent(awaitOrder())
        events.send(order)
        attack(order)
        val response = enemy!!.events.receive() as FireResponseEvent
        onResponse(response)
        return response
    }

    protected abstract suspend fun onResponse(event: FireResponseEvent)

    protected abstract suspend fun attack(order: FireEvent)

    suspend fun underAttack() {
        underAttack(enemy!!.events.receive() as FireEvent)
    }

    protected abstract suspend fun underAttack(event: FireEvent)

    private inner class StepExecutor<V : Any, E : Event<V>>(
        private val jobName: String,
        private val provideValue: suspend () -> V,
        private val createEvent: (V) -> E
    ) {

        suspend fun execute(): Pair<V, V> {
            return coroutineScope {
                val startTime = System.currentTimeMillis()
                val job = async(Dispatchers.IO) {
                    val value = provideValue()
                    events.send(createEvent(value))
                    value
                }

                @Suppress("UNCHECKED_CAST")
                val enemyJob = async(Dispatchers.IO)  {
                    val event = enemy!!.events.receive() as E
                    onEnemyEvent(event)
                    event.value
                }
                val jobResults = Pair(job.await(), enemyJob.await())
                jobResults
            }
        }
    }

    fun say(msg: String) {
        println("${javaClass.simpleName}-$name:\t$msg")
    }
}