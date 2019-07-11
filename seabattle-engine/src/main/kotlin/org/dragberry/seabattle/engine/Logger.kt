package org.dragberry.seabattle.engine

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface Logger {

    fun logMessage(msg: String)

    fun log(msg: String, timeRequired: Boolean = true) {
        logMessage("${if(timeRequired) LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss:\t")) else "" }$msg")
    }
}