package org.dragberry.seabattle.client.tornado

import javafx.scene.Scene
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

class GameStyle : Stylesheet() {
    companion object {
        val sector by cssclass()
        val sectorHit by cssclass()
        val sectorMiss by cssclass()
    }

    init {
        sector {
            borderWidth += box(1.px)
            borderColor += box(c("dodgerblue"))
            and(hover) {
                borderColor += box(c("red"))
            }
            and(pressed) {
                backgroundColor += c("red")
                borderColor += box(c("red"))
            }
        }

        sectorHit {
            backgroundColor += c("black")
            borderColor += box(c("black"))
        }

        sectorMiss {
            backgroundColor += c("dodgerblue")
        }
    }
}


