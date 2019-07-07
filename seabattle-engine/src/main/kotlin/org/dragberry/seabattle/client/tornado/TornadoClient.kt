package org.dragberry.seabattle.client.tornado

import javafx.beans.property.BooleanProperty
import javafx.geometry.Insets
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import org.dragberry.seabattle.engine.*
import tornadofx.*
import kotlin.properties.Delegates
import kotlin.random.Random

fun main(args: Array<String>) {
    launch<TornadoClient>(args)
}

class TornadoClient : App(MainView::class, GameStyle::class) {

    override fun createPrimaryScene(view: UIComponent): Scene = Scene(view.root, 320.0, 240.0)

}

class MainView : View() {

    override val root = borderpane {
        center<MenuView>()
    }
}

class MenuView : View() {

    override val root = borderpane {
        useMaxWidth = true
        center = button("Start") {
            action {
                replaceWith<OpponentsView>()
            }
        }
    }

    override fun onDock() {
        println("Docking...")
    }

    override fun onUndock() {
        println("Undocking...")
    }
}

class CreatePlayerView : Fragment() {

    private val panelTitle: String by param()

    private val isRightSection: Boolean by param()

    private val readyCheckBox = CheckBox()

    private var commander by Delegates.observable<Commander?>(null) { _, _, newValue ->
        readyCheckBox.selectedProperty().set(newValue != null)
    }

    override val root = vbox {
        padding = insets(10.0)
        spacing = 10.0
        hbox {
            if (isRightSection) add(readyCheckBox) else label(panelTitle)
            region { hgrow = Priority.ALWAYS }
            if (isRightSection) label(panelTitle) else add(readyCheckBox)
        }
        vbox {
            spacing = 10.0
            button("Local") {
                useMaxWidth = true
                action {
                    commander = Captain(
                        object : CommanderController {
                            override suspend fun getName(): String = "Player"
                            override suspend fun getRole(): Boolean = Random.nextBoolean()
                            override suspend fun getSettings(): BattleSettings = BattleSettings(10, 10, listOf(4, 3, 2, 1))
                            override suspend fun giveOrder(): Coordinate = Coordinate(1, 1)
                        },
                        isHidden = false
                    )
                }
            }
            button("AI") {
                useMaxWidth = true
                action {
                    commander = AICommander()
                }
            }
            button("Remote") {
                useMaxWidth = true
                action {
                    println("Remote commander")
                }
            }
        }
    }
}

class OpponentsView : View() {

    private val player1 = find<CreatePlayerView>(mapOf("panelTitle" to "Player 1", "isRightSection" to false))

    private val player2 = find<CreatePlayerView>(mapOf("panelTitle" to "Player 2", "isRightSection" to true))

    override val root = gridpane {
        val cs1 = ColumnConstraints()
        cs1.percentWidth = 50.0
        val cs2 = ColumnConstraints()
        cs2.percentWidth = 50.0
        columnConstraints.addAll(cs1, cs2)

        row {
            add(player1)
            add(player2)
        }

        row {
            button("Back") {
                useMaxWidth = true
                action {
                    println("Go to main menu")
                    replaceWith<MenuView>()
                }
            }
        }
    }

    override fun onDock() {
        println("Docking...")
    }

    override fun onUndock() {
        println("Undocking...")
    }

}

class GameController : Controller() {

    private val battle: Battle? = null

    private val commander: Commander? = null

    private val enemy: Commander? = null

}

class GameStyle: Stylesheet() {

    companion object {
        val menuButton by cssclass()

        val emptySector by cssclass()
        val shipSector by cssclass()
        val hitSector by cssclass()
        val hitShipSector by cssclass()
    }

    init {
        menuButton {
            maxWidth = infinity

            padding = box(10.px)
            borderInsets += box(5.px)
            backgroundInsets += box(5.px)
        }

        emptySector {
            backgroundColor += c("#99d6ff")
        }
        shipSector {
            backgroundColor += c("#145214")
        }
        hitSector {
            backgroundColor += c("#cc0000")
        }
        hitShipSector {
            backgroundColor += c("#008AE6")
        }
    }
}

class GameView : View() {

    val controller: GameController by inject()

    override val root = borderpane {
        top {
            label("Menu")
        }
        center {
            borderpane {
                left {
                    vbox{
                        for (y in 1..10) {
                            hbox {
                                for (x in 1..10) {
                                   button("$x:$y") {
                                       addClass(GameStyle.emptySector)
                                   }
                                }
                            }
                        }
                    }
                }
                center {
                    vbox {
                        label("Ships:")
                        label("####")
                        label("###")
                        label("###")
                        label("##")
                        label("##")
                        label("##")
                        label("##")
                    }
                }
                right {
                    vbox{
                        for (y in 1..10) {
                            hbox {
                                for (x in 1..10) {
                                    button("$x:$y")
                                }
                            }
                        }
                    }
                }
            }
        }
        bottom {
            label("Status line")
        }
    }
}