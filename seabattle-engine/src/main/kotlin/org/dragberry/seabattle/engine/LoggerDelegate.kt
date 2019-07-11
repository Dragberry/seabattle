package org.dragberry.seabattle.engine

import kotlin.reflect.KProperty

class LoggerDelegate {

    companion object {
        val defaultLogger =  object : Logger {
            override fun log(msg: String) {
                println(msg)
            }
        }

        var logger: Logger? = null
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger = logger ?: defaultLogger
}