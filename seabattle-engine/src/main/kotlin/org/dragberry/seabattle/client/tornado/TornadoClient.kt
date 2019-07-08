package org.dragberry.seabattle.client.tornado

import javafx.scene.Scene
import org.dragberry.seabattle.engine.*
import tornadofx.*

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