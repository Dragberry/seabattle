package org.dragberry.seabattle.client.tornado

import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import javafx.scene.layout.RowConstraints
import tornadofx.*

class OpponentsView : View() {

    private val player1 = find<CreatePlayerView>(mapOf("playerNameDefault" to "Player 1", "isRightSection" to false))

    private val player2 = find<CreatePlayerView>(mapOf("playerNameDefault" to "Player 2", "isRightSection" to true))

    override val root = gridpane {
        val cs1 = ColumnConstraints()
        cs1.percentWidth = 50.0
        val cs2 = ColumnConstraints()
        cs2.percentWidth = 50.0
        columnConstraints.addAll(cs1, cs2)

        val rc1 = RowConstraints()
        rc1.percentHeight = 80.0
        val rc2 = RowConstraints()
        rc2.percentHeight = 20.0
        rowConstraints.addAll(rc1, rc2)

        row {
            add(player1)
            add(player2)
        }

        row {
            add(vbox {
                padding = insets(10.0)
                useMaxWidth = true
                button("Back") {
                    useMaxWidth = true
                    vgrow = Priority.ALWAYS
                    action {
                        println("Go to main menu")
                        replaceWith<MenuView>()
                    }
                }
            }, 0, 1, 2, 1)
        }
    }
}