package fr.jestiz.core.api.commands

import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.parameters.ParameterData
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.PlayerManager
import org.apache.commons.lang.mutable.Mutable
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import java.util.*
import javax.swing.plaf.multi.MultiToolBarUI
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability


class RegisteredCommand(private val commandData: CommandData) : org.bukkit.command.Command(commandData.names[0]) {

    init {
        CommandHandler.commands.add(this)
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (!validateSender(sender))
            return false

        val commandSender: Any = if (!commandData.playerOnly) sender else PlayerManager.getOnlinePlayer((sender as Player).uniqueId)
        val transformedParameters: MutableList<Any?> = mutableListOf(commandSender)

        for (i in commandData.parameters.indices) {
            val param: ParameterData = commandData.parameters[i]
            val isDefault = i >= args.size

            if (isDefault && param.data.default.isEmpty()) {
                if (param.data.required) {
                    usage(sender)
                    return false
                } else {
                    transformedParameters.add(null)
                    continue
                }
            }

            var givenParam = if (!isDefault) args[i] else param.data.default

            if (param.data.concat && givenParam.trim() != param.data.default.trim())
                givenParam = concatParam(args, i)

            transformedParameters.add(transformParameter(sender, givenParam, param.kTypeParameter) ?: return false)
            if (param.data.concat)
                break
        }

        if (commandData.async) {
            Bukkit.getScheduler().runTaskAsynchronously(Core.instance) {
                commandData.function.call(commandData.commandClass, transformedParameters.toTypedArray())
            }
        } else
            commandData.function.call(commandData.commandClass, *transformedParameters.toTypedArray())
        return true
    }

    override fun tabComplete(sender: CommandSender, label: String, args: Array<out String>): MutableList<String> {
        if (commandData.permission != "" && !sender.hasPermission(commandData.permission))
            return mutableListOf()

        val spaceIndex = label.indexOf(' ')
        val completions = mutableListOf<String>()
        var doneHere = false

        for (alias in commandData.names) {
            var split = alias.split(" ").toTypedArray()[0]

            if (spaceIndex != -1)
                split = alias

            if (StringUtil.startsWithIgnoreCase(split.trim(), label.trim()) || StringUtil.startsWithIgnoreCase(label.trim(), split.trim())) {
                if (spaceIndex == -1 && label.length < alias.length)
                    completions.add("/" + split.lowercase(Locale.getDefault())) // Completes the command
                else if (label.lowercase(Locale.getDefault()).startsWith(alias.lowercase(Locale.getDefault()) + " ") && commandData.parameters.size > 0) {
                    // Completes the params
                    var paramIndex = label.split(" ").size - alias.split(" ").toTypedArray().size

                    // If they didn't hit space, complete the param before it.
                    if (paramIndex == commandData.parameters.size || !label.endsWith(" "))
                        paramIndex -= 1

                    if (paramIndex < 0)
                        paramIndex = 0

                    val paramData = commandData.parameters[paramIndex]
                    val params = label.split(" ")

                    CommandHandler.parameterTypes[paramData.kTypeParameter]?.let { paramProcessor ->
                        paramProcessor.tabComplete(sender, if (label.endsWith(" ")) "" else params[params.size - 1])
                            .forEach { completions.add(it) }
                    }

                    doneHere = true
                    break
                } else {
                    val halfSplitString = split.lowercase(Locale.getDefault())
                        .replaceFirst(alias.split(" ").toTypedArray()[0]
                            .lowercase(Locale.getDefault()), "").trim()
                    val splitString = halfSplitString.split(" ").toTypedArray()
                    val fixedAlias = splitString[splitString.size - 1].trim()
                    val lastArg = if (label.endsWith(" ")) "" else label.split(" ")[label.split(" ").size - 1]

                    if (fixedAlias.length >= lastArg.length)
                        completions.add(fixedAlias)

                    doneHere = true
                }
            }
        }

        if (!doneHere) {
            for (vanillaCompletion in super.tabComplete(sender, label, args) ?: mutableListOf<String>())
                completions.add(vanillaCompletion)
        }

        return completions
    }

    private fun transformParameter(sender: CommandSender, parameter: String, transformTo: KType): Any? {
        if (transformTo.classifier == String::class)
            return parameter

        return CommandHandler.parameterTypes[transformTo]!!.get(sender, parameter)
    }

    private fun validateSender(sender: CommandSender): Boolean {
        if (commandData.consoleOnly && sender is Player) {
            sender.sendMessage(Configurations.getConfigMessage("command.console-only"))
            return false
        }

        if (commandData.playerOnly && sender is ConsoleCommandSender) {
            sender.sendMessage(Configurations.getConfigMessage("command.player-only"))
            return false
        }

        if (commandData.permission != "" && !sender.hasPermission(commandData.permission)) {
            sender.sendMessage(Configurations.getConfigMessage("command.no-permission"))
            return false
        }

        return true
    }

    private fun concatParam(params: Array<out String>, index: Int): String {
        val stringBuilder = StringBuilder()

        for (arg in index until params.size)
            stringBuilder.append(params[arg]).append(" ")

        return stringBuilder.toString().trim()
    }

    private fun usage(sender: CommandSender) {
        val builder = StringBuilder(Configurations.getConfigMessage("command.command-usage").replace("%name%", commandData.names[0]))

        commandData.parameters.forEach {
            builder.append(if (it.data.required) "<" else "[")
                .append(it.data.name)
                .append(if (it.data.concat) ".." else "")
                .append(if (it.data.required) ">" else "]")
                .append(" ")
        }

        builder.trim()
        sender.sendMessage(builder.toString())
    }

}