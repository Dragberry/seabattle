package org.dragberry.seabattle.client.console

import kotlinx.coroutines.*
import java.lang.Exception
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.dragberry.seabattle.engine.*
import java.lang.IllegalArgumentException
import kotlin.random.Random
import kotlin.system.exitProcess

private const val QUIT :String = "q"

private val COORDINATE_REGEX = Regex("^([a-zA-Z]?)(\\d{1,2})$")

class ConsoleClient {

    init {
        LoggerDelegate.logger = object : Logger {
            override fun logMessage(msg: String) {
                println(msg)
            }
        }
    }

    private val mutex = Mutex()

    private inner class ConsoleController(private var defaultName: String) : CommanderController {

        private var name: String? = null

        override suspend fun getName(): String {
            name = requestUserAction("What is your name, commander $defaultName?", String::toString)
            return name as String
        }

        override suspend fun getSettings(): BattleSettings {
            return BattleSettings(10, 10, listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1))
        }

        override suspend fun getRole(): Boolean = mutex.withLock { Random.nextBoolean() }

        override suspend fun giveOrder(): Coordinate {
            return requestUserAction("Waiting for your orders, LocalCommander $name!", {
                val rawCoordinates = COORDINATE_REGEX.find(it)?.groupValues
                if (rawCoordinates != null) {
                    val x = rawCoordinates[2].toInt()
                    val y = rawCoordinates[1][0].toUpperCase().toInt() - 64
                    Coordinate(x, y)
                } else {
                    throw IllegalArgumentException("Order <$it> cannot be recognized!")
                }
            })
        }

    }

    interface Game {

        suspend fun commander(): Commander

        suspend fun enemy(): Commander

        suspend fun createCommanders(): Pair<Commander, Commander> {
            return coroutineScope {
                val job = async(Dispatchers.IO) { commander() }
                val enemyJob = async(Dispatchers.IO) { enemy() }
                Pair(job.await(), enemyJob.await())
            }
        }
    }

    fun start() = runBlocking {
        println("Welcome to SeaBattle(tm) Console Version!")
        val gameType = requestUserAction("Please, choose a game type: ",
            listOf(
                Pair(object : Game {
                    override suspend fun commander() =
                        Captain(ConsoleController("#1"), isHidden = false)
                    override suspend fun enemy() =
                        Captain(ConsoleController("#1"), isHidden = false)
                }, "P vs P"),
                Pair(object : Game {
                    override suspend fun commander() =
                        AICommander()
                    override suspend fun enemy() =
                        AICommander()
                }, "AI vs AI"),
                Pair(object : Game {
                    override suspend fun commander() =
                        RemoteServerCommander(requestUserAction("Choose port for Server 1", { it.toInt() }))
                    override suspend fun enemy() =
                        RemoteServerCommander(requestUserAction("Choose port for Server 2", { it.toInt() }))
                }, "Proxy SS game"),
                Pair(object : Game {
                    override suspend fun commander() =
                        RemoteServerCommander(requestUserAction("Choose port for Server", { it.toInt() }))
                    override suspend fun enemy() =
                        RemoteClientCommander("localhost", requestUserAction("Choose port for Client", { it.toInt() }))
                }, "Proxy CS game"),
                Pair(object : Game {
                    override suspend fun commander() =
                        RemoteClientCommander("localhost", requestUserAction("Choose port for Client 1", { it.toInt() }))
                    override suspend fun enemy() =
                        RemoteClientCommander("localhost", requestUserAction("Choose port for Client 2", { it.toInt() }))
                }, "Proxy CC game"),
                Pair(object : Game {
                    override suspend fun commander() =
                        Captain(ConsoleController("#1"), isHidden = false)
                    override suspend fun enemy() =
                        RemoteServerCommander(requestUserAction("Choose port", { it.toInt() }))
                }, "Host game"),
                Pair(object : Game {
                    override suspend fun commander() =
                        Captain(ConsoleController("#1"), isHidden = false)
                    override suspend fun enemy() =
                        RemoteClientCommander("localhost", requestUserAction("Choose port", { it.toInt() }))
                }, "Join game"),
                Pair(object : Game {
                    override suspend fun commander() =
                        AICommander()
                    override suspend fun enemy() =
                        RemoteServerCommander(requestUserAction("Choose port", { it.toInt() }))
                }, "Host as AI game"),
                Pair(object : Game {
                    override suspend fun commander() =
                        AICommander()
                    override suspend fun enemy() =
                        RemoteClientCommander("localhost", requestUserAction("Choose port", { it.toInt() }))
                }, "Join as AI game")

            )).createCommanders()
        val battle = Battle(gameType.first, gameType.second)
        battle.initialize()
        battle.play { battle ->
            println("\t${"====".repeat(battle.settings.width + 1)}  Round\t${battle.round}\t${"====".repeat(battle.settings.width + 1)}")
            val aggressor = battle.roles.aggressor()
            if (aggressor is FleetOwner && !aggressor.isHidden) {
                printHeader(battle)
                printLines(battle, aggressor)
            }
            println("\t${"====".repeat(battle.settings.width + 1)}${"====".repeat(3)}${"====".repeat(battle.settings.width + 1)}")
        }
    }

    private fun printLines(battle: Battle, fleetOwner: FleetOwner) {
        for (y in 1..battle.settings.height) {
            printLine(y, battle.settings, fleetOwner)
        }
    }

    private fun printLine(y: Int, settings: BattleSettings, fleetOwner: FleetOwner) {
        print("\t${(y + 64).toChar()}")
        for (x in 1..settings.width) {
            print("\t${drawPoint(x, y, fleetOwner.fleet)}")
        }
        print("\t\t|\t")
        print("\t${(y + 64).toChar()}")
        for (x in 1..settings.width) {
            print("\t${drawEnemyPoint(x, y, fleetOwner.enemyFleet)}")
        }
        println()
    }

    private fun drawPoint(x: Int, y: Int, fleet: Fleet<LocalSector>): String {
        val shipSection = fleet.getShip(x, y)?.getSection(x, y)
        return if (shipSection != null) {
            if (shipSection.isAlive) shipSection.getShip().sections.size.toString() else "X"
        } else {
            if (fleet.getSector(x, y).isHit) "." else " "
        }
    }

    private fun drawEnemyPoint(x: Int, y: Int, fleet: Fleet<EnemySector>): String {
        val shipSection = fleet.getShip(x, y)?.getSection(x, y)
        return if (shipSection != null) {
            if (shipSection.isAlive) shipSection.getShip().sections.size.toString() else "X"
        } else {
            val sector = fleet.getSector(x, y)
            if (sector.isOccupied) "X" else if (sector.isHit) "." else " "
        }
    }

    private fun printHeader(battle: Battle) {
        print("\t")
        for (x in 1..battle.settings.width) {
            print("\t$x")
        }
        print("\t\t|\t\t")
        for (x in 1..battle.settings.width) {
            print("\t$x")
        }
        println()
    }

    private suspend fun <T> requestUserAction(
        title: String,
        transform: (String) -> T,
        options: List<Pair<T, String>>? = null,
        onErrorString: String = "Invalid input, please try again",
        isExitAvailable: Boolean = true
    ): T {
            println(title)
            options?.forEachIndexed { index, entry ->
                println("\t${index + 1}. ${entry.second}")
            }
            do {
                val input = withContext(Dispatchers.IO) { readLine() ?: "" }
                if (isExitAvailable && QUIT.equals(input, true)) {
                    exit()
                }
                try {
                    return transform(input)
                } catch (exc: Exception) {
                    println(onErrorString)
                    continue
                }
            } while (true)
    }

    private suspend fun <T> requestUserAction(
        title: String,
        transform: (String) -> T,
        onErrorString: String = "Invalid input, please try again",
        isExitAvailable: Boolean = true): T {
        return mutex.withLock {
            requestUserAction(title, transform, null, onErrorString, isExitAvailable)
        }
    }

    private suspend fun <T> requestUserAction(
        title: String,
        options: List<Pair<T, String>>,
        onErrorString: String = "Invalid input, please try again",
        isExitAvailable: Boolean = true): T {
        return mutex.withLock {
            requestUserAction(title, { options[it.toInt() - 1].first }, options, onErrorString, isExitAvailable)
        }
    }

    private fun exit() {
        exitProcess(0)
    }

}