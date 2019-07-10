package org.dragberry.seabattle.client.tornado

import javafx.scene.control.Button
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.Battle
import tornadofx.*

class OpponentsView : View() {

    private val controller: GameController by inject()

    private val player1 = find<CreatePlayerView>(mapOf("playerNameDefault" to "Player 1", "isRightSection" to false))

    private val player2 = find<CreatePlayerView>(mapOf("playerNameDefault" to "Player 2", "isRightSection" to true))

    init {
        player1.onCommander {
            controller.commander = it
            startButton.isDisable = it == null || player2.commander == null
        }
        player2.onCommander {
            controller.enemy = it
            startButton.isDisable = it == null || player1.commander == null
        }
    }

    private val startButton = button("Start") {
        useMaxWidth = true
        isDisable = true
        action {
            try {
                replaceWith(find<GameView>())
            } catch (exc : Exception) {
                println("An error has occurred..")
            }
        }
    }

    override val root = gridpane {
        val cs1 = ColumnConstraints()
        cs1.percentWidth = 50.0
        val cs2 = ColumnConstraints()
        cs2.percentWidth = 50.0
        columnConstraints.addAll(cs1, cs2)

        val rc1 = RowConstraints()
        rc1.percentHeight = 70.0
        val rc2 = RowConstraints()
        rc2.percentHeight = 30.0
        rowConstraints.addAll(rc1, rc2)

        row {
            add(player1)
            add(player2)
        }

        row {
            add(vbox {
                padding = insets(10.0, 0.0)
                spacing = 10.0
                useMaxWidth = true
                vgrow = Priority.ALWAYS

                add(startButton)

                button("Back") {
                    useMaxWidth = true
                    action {
                        println("Go to main menu")
                        replaceWith<MenuView>()
                    }
                }
            }, 0, 1, 2, 1)
        }
    }
}