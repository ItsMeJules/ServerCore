package fr.jestiz.core.api.commands.executable

import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.CommandHandler
import fr.jestiz.core.api.commands.data.SubCommandData
import fr.jestiz.core.api.commands.parameters.ParameterData
import fr.jestiz.core.configs.Configurations
import fr.jestiz.core.players.PlayerManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.reflect.KType

class ServerSubCommand(val subCommandData: SubCommandData) {

    fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        if (subCommandData.async) {
            Bukkit.getScheduler().runTaskAsynchronously(Core.instance) {
                transformParameters(sender, args)?.let {
                    subCommandData.function.call(subCommandData.commandClass, *it.toTypedArray())
                }
            }
        } else {
            transformParameters(sender, args)?.let {
                subCommandData.function.call(subCommandData.commandClass, *it.toTypedArray())
            }
        }
        return true
    }

    fun tabComplete(sender: CommandSender, label: String, args: Array<out String>): MutableList<String> {
        if (args.size < subCommandData.parameters.size) {
            val paramData = subCommandData.parameters[args.size - 1]

            CommandHandler.parameterTypes[paramData.kTypeParameter]?.let { paramProcessor ->
                return paramProcessor.tabComplete(sender, args[args.size - 1]).toMutableList()
            }
        }
        return mutableListOf()
    }

    private fun transformParameters(sender: CommandSender, args: Array<out String>): MutableList<Any?>? {
        val commandSender: Any = if (!subCommandData.playerOnly) sender else PlayerManager.getOnlinePlayer((sender as Player).uniqueId)
        val transformedParameters: MutableList<Any?> = mutableListOf(commandSender)

        for (i in subCommandData.parameters.indices) {
            val param: ParameterData = subCommandData.parameters[i]
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

    private fun concatParam(params: Array<out String>, index: Int): String {
        val stringBuilder = StringBuilder()

        for (arg in index until params.size)
            stringBuilder.append(params[arg]).append(" ")

        return stringBuilder.toString().trim()
    }

    private fun usage(sender: CommandSender) {
        val builder = StringBuilder(Configurations.getConfigMessage("command.command-usage").replace("%name%", subCommandData.name))

        subCommandData.parameters.forEach {
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