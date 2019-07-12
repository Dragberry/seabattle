package org.dragberry.seabattle.client.tornado

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ScrollPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Pane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx as Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.*
import tornadofx.*

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

    private val field = pane()
    private val roundInfo = SimpleStringProperty()
    private val info = vbox {
        prefWidth = 100.0
        spacing = 10.0
        label(roundInfo)
        button("Pause") {
            useMaxWidth = true
        }
        button("Stop") {
            useMaxWidth = true
        }
    }

    private val enemyField = pane()
    override val root = borderpane {
        center {
            hbox {
                spacing = 10.0
                add(field)
                add(info)
                add(enemyField)
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
                field.add(FieldPane((it.commander as LocalCommander).fleet))
                roundInfo.value = "Round: ${it.round}"
                enemyField.add(FieldPane((it.enemyCommander as LocalCommander).fleet))
                with(primaryStage) {
                    minWidth = it.settings.width * 25.0 * 2 + 100.0 + 36.0
                    minHeight = it.settings.height * 25.0 + 40 + 50
                    maxWidth = minWidth
                    maxHeight = minHeight
                }
            }
            controller.play {
                roundInfo.value = "Round: ${it.round}"
            }
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

class FieldPane(fleet: Fleet<out Sector>) : Pane() {

    init {
        fleet.field.withIndex().forEach { y ->
            y.value.withIndex().forEach { x ->
                children.add(Cell(x.value))
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