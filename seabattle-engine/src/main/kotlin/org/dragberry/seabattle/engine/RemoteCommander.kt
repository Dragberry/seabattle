package org.dragberry.seabattle.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.dragberry.seabattle.log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

abstract class RemoteCommander : Commander() {

    protected abstract val socket: Socket

    private val output: ObjectOutputStream by lazy { ObjectOutputStream(socket.getOutputStream()) }

    private val input: ObjectInputStream by lazy { ObjectInputStream(socket.getInputStream()) }

    override suspend fun provideName(): String {
        return withContext(Dispatchers.IO) { (input.readObject() as NameEvent).value }
    }

    override suspend fun provideSettings(): BattleSettings {
        return withContext(Dispatchers.IO) { (input.readObject() as SettingsEvent).value }
    }

    override suspend fun provideRole(): Boolean {
        return withContext(Dispatchers.IO) { (input.readObject() as RoleEvent).value }
    }

    override suspend fun provideInitializationStatus(accepted: Boolean, rolesResolved: Boolean): Boolean {
        return withContext(Dispatchers.IO) { (input.readObject() as InitializationEvent).value }
    }

    override fun onEnemyEvent(event: Event<out Any>) {
        when (event) {
            is NameEvent -> log("$defaultName:\tSending enemy name")
            is SettingsEvent -> log("$defaultName:\tSending enemy settings")
            is RoleEvent -> log("$name:\tSending enemy role")
            is InitializationEvent -> log("$defaultName:\tSending initialization status")
            else -> "Unknown Event: ${event.javaClass.name}"
        }
        output.writeObject(event)
        output.flush()
    }

    override var isAlive: Boolean = true

    override val isHidden: Boolean = true

    override suspend fun awaitOrder(): Coordinate {
        return withContext(Dispatchers.IO) { (input.readObject() as FireEvent).value }
    }

    override suspend fun attack(order: FireEvent) {
    }

    override suspend fun onResponse(event: FireResponseEvent) {
        output.writeObject(event)
        output.flush()
    }

    override suspend fun underAttack(event: FireEvent) {
        output.writeObject(event)
        output.flush()
        events.send(withContext(Dispatchers.IO) {
            input.readObject() as FireResponseEvent
        })
    }
}

class RemoteServerCommander(port: Int) : RemoteCommander() {

    override val socket: Socket

    init {
        println("Starting server...")
        val serverSocket = ServerSocket(port, 0)
        println("Server started")
        socket = serverSocket.accept()
        println("Connection accepted!")
    }

}

class RemoteClientCommander(host: String, port: Int) : RemoteCommander() {

    override val socket: Socket

    init {
        println("Connecting...")
        socket = Socket(host, port)
        println("Connection has been established")
    }
}