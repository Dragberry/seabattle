package org.dragberry.seabattle.client.tornado

import javafx.beans.property.SimpleStringProperty
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.*
import tornadofx.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates
import kotlin.random.Random

class CreatePlayerView : Fragment() {

    private val playerNameDefault: String by param()

    private val playerNameLabel = label(playerNameDefault)

    private val isRightSection: Boolean by param()

    private val buttons = mutableListOf<Button>()

    private val readyCheckBox = CheckBox()
    init {
        readyCheckBox.isDisable = true
        readyCheckBox.selectedProperty().addListener { _, _, newValue ->
            val checked = newValue != null && newValue
            if (!checked) {
                commander = null
            }
            buttons.forEach { it.isDisable = checked }
        }
    }

    private var commander by Delegates.observable<Commander?>(null) { _, _, newValue ->
        if (newValue != null) {
            readyCheckBox.isSelected = true
            readyCheckBox.isDisable = false
            buttons.forEach { it.isDisable = true }
        } else {
            readyCheckBox.isSelected = false
            readyCheckBox.isDisable = true
            buttons.forEach { it.isDisable = false }
        }
    }

    override val root = vbox {
        padding = insets(10.0)
        spacing = 10.0
        hbox {
            add(if(isRightSection) readyCheckBox else playerNameLabel)
            region { hgrow = Priority.ALWAYS }
            add(if(isRightSection) playerNameLabel else readyCheckBox)
        }
        vbox {
            spacing = 10.0
            val localBtn = button("Local") {
                useMaxWidth = true
                action {
                    val dialog = find<CaptainCreateDialog>()
                    dialog.openWindow()
                    GlobalScope.launch { commander = dialog.getCommander() }
                }
            }
            buttons.add(localBtn)
            localBtn
            val aiButton = button("AI") {
                useMaxWidth = true
                action {
                    commander = AICommander()
                }
            }
            buttons.add(aiButton)
            aiButton
            val remoteButton = button("Remote") {
                useMaxWidth = true
                action {
                    println("Remote commander")
                }
            }
            buttons.add(remoteButton)
            remoteButton
        }
    }
}

class CaptainCreateDialog : Fragment() {

    private var continuation: Continuation<Commander>? = null

    val name = SimpleStringProperty()

    override val root = form {
        gridpane {
            val cs1 = ColumnConstraints()
            cs1.percentWidth = 50.0
            val cs2 = ColumnConstraints()
            cs2.percentWidth = 50.0
            columnConstraints.addAll(cs1, cs2)

            useMaxWidth = true

            row {
                label("Player name:")
                textfield(name)
            }
            row {
                spacing = 10.0
                button("Confirm") {
                    padding = insets(10.0)
                    useMaxWidth = true
                    action {
                        continuation?.resume(Captain(
                            object : CommanderController {
                                override suspend fun getName(): String = name.get()
                                override suspend fun getRole(): Boolean = Random.nextBoolean()
                                override suspend fun getSettings(): BattleSettings =
                                    BattleSettings(10, 10, listOf(4, 3, 2, 1))

                                override suspend fun giveOrder(): Coordinate =
                                    Coordinate(1, 1)
                            },
                            isHidden = false
                        ))
                        close()
                    }
                }
                button("Cancel") {
                    padding = insets(10.0)
                    useMaxWidth = true
                    action {
                        close()
                    }
                }
            }
        }
    }

    suspend fun getCommander(): Commander {
        return suspendCoroutine { continuation = it }
    }

}