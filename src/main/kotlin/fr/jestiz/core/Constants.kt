package fr.jestiz.core

import java.text.SimpleDateFormat
import java.util.*


object Constants {


    const val OFFLINE_PLAYER_EXPIRE_TIME_MINS: Long = 5

    // CONFIGURATION
    const val BAN_NO_REASON_FOUND = "§cno reason was found."
    const val CONFIGURATION_NOT_FOUND = "§cinternal error, configuration was not found. Please report this error."
    val CONFIGURATION_PATH_NOT_FOUND = StringBuilder("§cinternal error, path '%path%' not found in file '%file%'. Please report this error.")

    // TIME
    const val PUNISHMENT_NO_EXPIRE = Long.MIN_VALUE;
    val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy - HH:mm:ss")
    val DURATION_PARSER_REGEX = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)".toRegex();

    // REDIS
    const val PUNISHMENT_CHANNEL = "punishment"
    const val REDIS_UUID_LOOKUP_CHANNEL = "name-to-uuid"
    const val REDIS_NAME_UUID_HSET = "name-to-uuid"

    // UUID
    val CONSOLE_UUID: UUID = UUID.fromString("29f26148-4d55-4b4b-8e07-900fda686a67")

    // PERMISSIONS
    const val PERMISSION_BAN_COMMAND = "core.command.ban"
    const val PERMISSION_GAMEMODE_COMMAND = "core.command.gamemode"

}