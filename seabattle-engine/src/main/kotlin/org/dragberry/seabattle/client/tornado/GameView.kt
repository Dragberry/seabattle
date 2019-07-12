package org.dragberry.seabattle.client.tornado

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Pane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx as Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.*
import tornadofx.*
import kotlin.system.exitProcess

class GameView : View() {

    private val controller: GameController by inject()

    private val logField = SimpleStringProperty("")

    private val logPane = scrollpane {
        vvalue = 1.0
        vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        maxHeight = 50.0
        content  = label(logField)
    }

    private val fieldPane = pane()
    private var field: FieldPane? = null
    private val roundInfo = SimpleStringProperty()
    private val info = vbox {
        prefWidth = 100.0
        spacing = 10.0
        label(roundInfo)
        button("Pause") {
            useMaxWidth = true
            action {
                controller.logger.log("Paused")
            }
        }
        button("Stop") {
            useMaxWidth = true
            action {
                controller.logger.log("Game stopped")
            }
        }
    }

    private val enemyFieldPane = pane()
    private var enemyField: FieldPane? = null
    override val root = borderpane {
        center {
            hbox {
                spacing = 10.0
                add(fieldPane)
                add(info)
                add(enemyFieldPane)
            }
        }
        bottom {
            add(logPane)
        }
    }

    init {
        start()
    }

    fun start() {
        controller.createBattle()
        GlobalScope.launch {
            controller.initializeBattle {
                roundInfo.value = "Round: ${it.round}"

                val fieldPane = FieldPane((it.commander as LocalCommander).fleet)
                this@GameView.field = fieldPane
                this@GameView.fieldPane.add(fieldPane)

                val enemyFieldPane = FieldPane((it.enemyCommander as LocalCommander).fleet)
                this@GameView.enemyField = enemyFieldPane
                this@GameView.enemyFieldPane.add(enemyFieldPane)

                with(primaryStage) {
                    minWidth = it.settings.width * 25.0 * 2 + 100.0 + 36.0
                    minHeight = it.settings.height * 25.0 + 40 + 50
                    maxWidth = minWidth
                    maxHeight = minHeight
                }
            }

            controller.play(
                onEveryStep = {
                    roundInfo.value = "Round: ${it.round}"
                    field?.update()
                    enemyField?.update()
                },
                onResponse = {
                    when (it) {
                        is DefeatResponse -> replaceWith<OpponentsView>()
                        is SystemResponse -> {
                            when (it.event) {
                                SystemEvent.EXIT -> {
                                    replaceWith<OpponentsView>()
                                }
                                SystemEvent.TIMEOUT -> exitProcess(0)
                                SystemEvent.RESTART -> exitProcess(0)
                            }
                        }
                    }
                }
            )
        }
    }

    override fun onDock() {
        LoggerDelegate.logger = object : Logger {
            override fun logMessage(msg: String) {
                LoggerDelegate.defaultLogger.log(msg, false)
                GlobalScope.launch(Dispatchers.Main) {
                    logField.value += "\n$msg"
                    logPane.vvalue = 1.0
                }
            }
        }
    }

    override fun onUndock() {
        LoggerDelegate.logger = null
    }
}

class FieldPane(private val fleet: Fleet<out Sector>) : Pane() {

    private val map = mutableMapOf<Coordinate, Cell>()

    init {
        fleet.field.forEach { row ->
            row.forEach { sector ->
                val cell = Cell(sector)
                map[sector.coordinate] = cell
                children.add(cell)
            }
        }
    }

    fun update() {
        fleet.field.forEach { row ->
            row.forEach { sector ->
                val cell = map[sector.coordinate]
                if (cell != null) {
                    if (sector.isHit && sector.isOccupied) {
                        cell.addClass(GameStyle.sectorHit)
                    } else if (sector.isHit) {
                        cell.addClass(GameStyle.sectorMiss)
                    } else {
                        cell.removeClass(GameStyle.sectorHit, GameStyle.sectorMiss)
                    }
                }
            }
        }
    }
}

class Cell(val sector: Sector, cellSize: Double = 25.0) : Pane() {
    init {
        prefWidth = cellSize
        prefHeight = cellSize
        layoutX = (sector.coordinate.x - 1) * cellSize
        layoutY = (sector.coordinate.y - 1) * cellSize
        addClass(GameStyle.sector)
        onHover {
            println("onHover: ${sector.coordinate.x}-${sector.coordinate.y}")
        }
        setOnMouseClicked {
            println("clicked: ${sector.coordinate.x}-${sector.coordinate.y}")
        }
    }
}