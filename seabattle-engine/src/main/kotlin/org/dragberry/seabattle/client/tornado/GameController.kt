package org.dragberry.seabattle.client.tornado

import org.dragberry.seabattle.engine.Battle
import org.dragberry.seabattle.engine.Commander
import tornadofx.Controller

class GameController : Controller() {

    var battle: Battle? = null

    fun createBattle(commander: Commander, enemy: Commander) {
        battle = Battle(commander, enemy)
    }
}