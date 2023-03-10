package fr.jestiz.core.api.commands.parameters.processors

import org.bukkit.command.CommandSender
import kotlin.reflect.KClass

abstract class ParameterProcessor<T> {

    abstract fun get(sender: CommandSender, source: String): T?
    abstract fun tabComplete(sender: CommandSender, source: String): List<String>

}