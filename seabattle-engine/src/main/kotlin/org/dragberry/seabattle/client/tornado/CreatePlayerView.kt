package org.dragberry.seabattle.client.tornado

import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.layout.Priority
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.dragberry.seabattle.engine.*
import tornadofx.*
import kotlin.properties.Delegates

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
            add(localBtn)
            val aiButton = button("AI") {
                useMaxWidth = true
                action {
                    commander = AICommander()
                }
            }
            buttons.add(aiButton)
            add(aiButton)
            val remoteButton = button("Remote") {
                useMaxWidth = true
                action {
                    println("Remote commander")
                }
            }
            buttons.add(remoteButton)
            add(remoteButton)
        }
    }
}

