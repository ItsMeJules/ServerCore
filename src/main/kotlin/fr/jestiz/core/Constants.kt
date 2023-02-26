package fr.jestiz.core

import java.text.SimpleDateFormat

object Constants {

    const val BAN_NO_REASON_FOUND = "no reason was found."
    const val CONFIGURATION_NOT_FOUND = "internal error, configuration was not found. Please report this error."
    const val PUNISHMENT_NO_EXPIRE = Long.MIN_VALUE;

    val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss")

    const val PUNISHMENT_CHANNEL = "punishment"
}