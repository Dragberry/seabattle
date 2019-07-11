package org.dragberry.seabattle.client.tornado

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ScrollPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Pane
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx as Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.Logger
import org.dragberry.seabattle.engine.LoggerDelegate
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

    private val field = stackpane()
    private val roundInfo = SimpleStringProperty()
    private val info = vbox {
        useMaxWidth = true
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
            gridpane {
                val cs1 = ColumnConstraints()
                cs1.percentWidth = 40.0
                val cs2 = ColumnConstraints()
                cs2.percentWidth = 20.0
                val cs3 = ColumnConstraints()
                cs3.percentWidth = 40.0
                columnConstraints.addAll(cs1, cs2, cs3)
                row {
                    add(field, 0, 0, 1, 1)
                    add(info, 1, 0, 1, 1)
                    add(enemyField, 2, 0, 1, 1)
                }
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
                with(primaryStage) {
                    width = it.settings.width * 25.0 * 2 / 0.8
                    height = it.settings.height * 25.0 + 50
                }
                field.add(createField(it.settings.width, it.settings.height))
                roundInfo.value = "Round: ${it.round}"
                enemyField.add(createField(it.settings.width, it.settings.height))
            }
            controller.play {
                roundInfo.value = "Round: ${it.round}"
            }
        }
    }

    private fun createField(width: Int, height: Int): Pane {
        return pane {
            for (row in 0..height) {
                for (column in 0..width) {
                    add(pane {
                        val width = 25.0
                        val height = 25.0
                        val x = column * height
                        val y = row * width
                        layoutX = x
                        layoutY = y
                        prefWidth = width
                        prefHeight = height
                        style = "-fx-border-color: dodgerblue;" +
                                "-fx-border-width: 1px;"
                    })
                }
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