package org.dragberry.seabattle.client.tornado

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ScrollPane
import javafx.scene.layout.ColumnConstraints
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

    private val field = pane()
    private val roundInfo = SimpleStringProperty()
    private val info = pane {
        label(roundInfo)
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
                field.add(label(it.commander.name))
                roundInfo.value = "Round: ${it.round}"
                enemyField.add(label(it.enemyCommander.name))
            }
            controller.play {
                roundInfo.value = "Round: ${it.round}"
            }
        }
    }

    override fun onDock() {
        LoggerDelegate.logger = object : Logger {
            override fun log(msg: String) {
                LoggerDelegate.defaultLogger.log(msg)
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