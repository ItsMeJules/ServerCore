package fr.jestiz.core.api.commands.executable

import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.data.CommandData
import fr.jestiz.core.api.commands.CommandHandler
import fr.jestiz.core.api.commands.parameters.ParameterData
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import kotlin.reflect.KType


class ServerCommand(val commandData: CommandData) : org.bukkit.command.Command(commandData.names[0]) {

    val subCommands = mutableListOf<ServerSubCommand>()

    init {
        CommandHandler.commands.add(this)
        super.setAliases(commandData.names)
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (!validateSender(sender))
            return false

        findSubCommand(args)?.apply { return execute(sender, commandLabel, args) }

        if (commandData.async) {
            Bukkit.getScheduler().runTaskAsynchronously(Core.instance) {
                transformParameters(sender, args)?.let {
                    commandData.function.call(commandData.commandClass, *it.toTypedArray())
                }
            }
        } else {
            transformParameters(sender, args)?.let {
                commandData.function.call(commandData.commandClass, *it.toTypedArray())
            }
        }
        return true
    }

    override fun tabComplete(sender: CommandSender, label: String, args: Array<out String>): MutableList<String> {
        if (commandData.permission != "" && !sender.hasPermission(commandData.permission))
            return mutableListOf()

        if (subCommands.isNotEmpty()) {
            findSubCommand(args)?.apply { return tabComplete(sender, label, args) }

            val completions = mutableListOf<String>()
            for (subCommand in subCommands) {
                val data = subCommand.subCommandData

                if (args.size > data.argsLength)
                    continue

                if (StringUtil.startsWithIgnoreCase(data.subArgs[args.size - 1], args.last()))
                    completions.add(data.subArgs[args.size - 1])
            }

            return completions
        }

        if (args.size < commandData.parameters.size) {
            val paramData = commandData.parameters[args.size - 1]

            CommandHandler.parameterTypes[paramData.kTypeParameter]?.let { paramProcessor ->
                return paramProcessor.tabComplete(sender, args[args.size - 1]).toMutableList()
            }
        }

        return super.tabComplete(sender, label, args)
    }

    private fun findSubCommand(args: Array<out String>): ServerSubCommand? {
        for (subCommand in subCommands) {
            val data = subCommand.subCommandData
            if (args.size < data.argsLength)
                continue

            var i = 0
            while (i < data.argsLength) {
                if (data.subArgs[i] != args[i])
                    break
                i++
            }

            if (i != data.argsLength)
                continue
            return subCommand
        }
        return null
    }


    private fun transformParameters(sender: CommandSender, args: Array<out String>): MutableList<Any?>? {
        val commandSender: Any = if (!commandData.playerOnly) sender else PlayerManager.getOnlinePlayer((sender as Player).uniqueId)
        val transformedParameters: MutableList<Any?> = mutableListOf(commandSender)

        for (i in commandData.parameters.indices) {
            val param: ParameterData = commandData.parameters[i]
            val isDefault = i >= args.size

            if (isDefault && param.data.default.isEmpty()) {
                if (param.data.required) {
                    usage(sender)
                    return null
                } else {
                    transformedParameters.add(null)
                    continue
                }
            }

            var givenParam = if (!isDefault) args[i] else param.data.default

            if (param.data.concat && givenParam.trim() != param.data.default.trim())
                givenParam = concatParam(args, i)

            transformedParameters.add(transformParameter(sender, givenParam, param.kTypeParameter) ?: return null)
            if (param.data.concat)
                break
        }
        return transformedParameters
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