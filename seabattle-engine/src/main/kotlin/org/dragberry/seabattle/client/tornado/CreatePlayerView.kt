package org.dragberry.seabattle.client.tornado

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Spinner
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.*
import tornadofx.*
import java.beans.PropertyChangeListener
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.LinkedHashMap
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

    var commander by Delegates.observable<Commander?>(null) { _, _, newValue ->
        if (newValue != null) {
            readyCheckBox.isSelected = true
            readyCheckBox.isDisable = false
            buttons.forEach { it.isDisable = true }
        } else {
            readyCheckBox.isSelected = false
            readyCheckBox.isDisable = true
            buttons.forEach { it.isDisable = false }
        }
        onCommander?.invoke(newValue)
    }

    private var onCommander: ((Commander?) -> Unit)? = null

    fun onCommander(action: (Commander?) -> Unit) {
        onCommander = action
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
                    dialog.title = playerNameDefault
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

    private val nameProp = SimpleStringProperty()

    private val widthProp = SimpleIntegerProperty(10)

    private val heightProp = SimpleIntegerProperty(10)

    private val ships = LinkedHashMap<Int, SimpleIntegerProperty>(5)
    init {
        ships[1] = SimpleIntegerProperty(4)
        ships[2] = SimpleIntegerProperty(3)
        ships[3] = SimpleIntegerProperty(2)
        ships[4] = SimpleIntegerProperty(1)
        ships[5] = SimpleIntegerProperty(0)
    }

    override val root = gridpane {
        val cs1 = ColumnConstraints()
        cs1.percentWidth = 25.0
        val cs2 = ColumnConstraints()
        cs2.percentWidth = 25.0
        val cs3 = ColumnConstraints()
        cs3.percentWidth = 25.0
        val cs4 = ColumnConstraints()
        cs4.percentWidth = 25.0
        columnConstraints.addAll(cs1, cs2, cs3, cs4)

        prefWidth = 320.0
        hgap = 10.0
        vgap = 10.0
        padding = insets(10.0)

        row {
            add(label("Player name:"), 0, 0, 2, 1)
            add(textfield(nameProp), 2, 0, 2, 1)
        }
        row {
            add(label("Settings:"), 0, 1, 4, 1)
        }
        row {
            add(label("Width:"), 0, 2, 1, 1)
            add(spinner(3, 20, 10, 1, false, widthProp), 1, 2, 1, 1)
            add(label("Height:"), 2, 2, 1, 1)
            add(spinner(3, 20, 10, 1, false, heightProp), 3, 2, 1, 1)
        }
        row {
            add(label("Ships:"), 0, 3, 4, 1)
        }
        var rowIndex = 4
        ships.forEach { shipSize, prop ->
            row {
                add(label("#".repeat(shipSize)), 0, rowIndex, 2, 1)
                add(spinner(0, 10, prop.value, 1, false, prop), 2, rowIndex++, 2, 1)
            }
        }

        row {
            add(button("Confirm") {
                padding = insets(10.0)
                useMaxWidth = true
                action {
                    continuation?.resume(Captain(
                        object : CommanderController {
                            override suspend fun getName(): String = nameProp.get()
                            override suspend fun getRole(): Boolean = Random.nextBoolean()
                            override suspend fun getSettings(): BattleSettings {
                                return BattleSettings(
                                    widthProp.value,
                                    heightProp.value,
                                    ships
                                        .flatMap { shipSizeProp ->
                                            List(shipSizeProp.value.value) { shipSizeProp.key }
                                        }
                                        .toList()
                                        .sortedDescending())
                            }
                            override suspend fun giveOrder(): Coordinate =
                                Coordinate(1, 1)
                        },
                        isHidden = false
                    ))
                    close()
                }
            }, 0, rowIndex, 2, 1)
            add(button("Cancel") {
                padding = insets(10.0)
                useMaxWidth = true
                action {
                    close()
                }
            }, 2, rowIndex++, 2, 1)
        }
    }

    suspend fun getCommander(): Commander {
        return suspendCoroutine { continuation = it }
    }

}