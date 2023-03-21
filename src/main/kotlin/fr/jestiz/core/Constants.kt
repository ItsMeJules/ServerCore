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
    const val REDIS_PUNISHMENT_CHANNEL = "punishment"
    const val REDIS_UUID_LOOKUP_REQUEST_CHANNEL = "name-to-uuid"
    const val REDIS_UUID_LOOKUP_RESPONSE_CHANNEL = "response_name-to-uuid"
    const val REDIS_SERVER_HEARTBEAT_CHANNEL = "server-heartbeat"
    const val REDIS_PLAYER_UPDATE_CHANNEL = "player-update"
    const val REDIS_BROADCAST_CHANNEL = "broadcast"

    const val REDIS_KEY_NAME_TO_UUID = "name-to-uuid"
    const val REDIS_KEY_UUID_TO_NAME = "uuid-to-name"
    const val REDIS_KEY_PUNISHMENTS_LAST_ID = "last-ids:punishments"
    const val REDIS_KEY_CONNECTED_PLAYERS_LIST = "connected-players"

    const val REDIS_KEY_PLAYER_PUNISHMENTS = "punishments"
    const val REDIS_KEY_PLAYER_PUNISHMENTS_IDS = "punishments-ids"
    const val REDIS_KEY_PLAYER_COINS = "coins"

    // UUID
    val CONSOLE_UUID: UUID = UUID.fromString("29f26148-4d55-4b4b-8e07-900fda686a67")

    // PERMISSIONS
    const val PERMISSION_BAN_COMMAND = "core.command.ban"
    const val PERMISSION_GAMEMODE_COMMAND = "core.command.gamemode"
    const val PERMISSION_COINS_COMMAND = "core.command.coins"

}