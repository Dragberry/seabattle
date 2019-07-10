package org.dragberry.seabattle.client.tornado

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ScrollPane
import javafx.scene.layout.ColumnConstraints
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tornadofx.*

class GameView : View() {

    private val controller: GameController by inject()

    private val logField = SimpleStringProperty()

    override val root = borderpane {
        top {
            label("Round 1")
        }
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
                    add(label("field 1"), 0, 0, 1, 1)
                    add(label("center"), 1, 0, 1, 1)
                    add(label("field 2"), 2, 0, 1, 1)
                }
            }
        }
        bottom {
            scrollpane {
                vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
                hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
                maxHeight = 100.0
                content  = textarea(logField)
            }
        }
    }

    init {
        start()
    }

    fun start() {
        GlobalScope.launch {
            controller.battle?.start {
                println("Round ${it.round}")
            }
        }
    }
}