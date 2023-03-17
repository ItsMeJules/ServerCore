package fr.jestiz.core.api.commands

import com.google.common.reflect.ClassPath
import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.parameters.processors.ParameterProcessor
import org.bukkit.command.CommandMap
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.primaryConstructor


object CommandHandler {

    val commands = mutableListOf<ServerCommand>()
    val parameterTypes = mutableMapOf<KType, ParameterProcessor<*>>()

    private val commandMap: CommandMap

    init {
        val commandMapField = Core.instance.server.javaClass.getDeclaredField("commandMap")
        commandMapField.isAccessible = true
        commandMap = (commandMapField[Core.instance.server] as CommandMap)
    }

    /**
     * Registers commands based off a file path
     * @param path Path
     */
    fun registerCommands(path: String) {
        ClassPath.from(Core.instance.javaClass.classLoader).allClasses
            .filter {it.packageName.startsWith(path) }
            .forEach { registerCommands(it.load().kotlin) }
    }

    /**
     * Registers the commands in the class
     * @param commandClass Class
     */
    fun registerCommands(commandClass: KClass<*>) {
        registerCommands(commandClass.primaryConstructor!!.call())
    }

    /**
     * Registers the commands in the class
     * @param commandClass Class
     */
    fun registerCommands(commandClass: Any) {
        commandClass::class.declaredMemberFunctions.forEach { function ->
            function.annotations
                .firstOrNull { it.annotationClass == Command::class}
                ?.let { commandMap.register(Core.instance.name, ServerCommand(CommandData(it as Command, commandClass, function))) }
                ?: return@forEach
        }
    }

    /**
     * Registers processors based off a file path
     * @param path Path
     */
    fun registerParameterProcessors(path: String) {
        ClassPath.from(Core.instance.javaClass.classLoader).allClasses
            .filter { it.packageName.startsWith(path) }
            .filter { it.load().superclass == ParameterProcessor::class.java }
            .forEach { parameter ->
                val parameterProcessor = parameter.load().kotlin.primaryConstructor!!.call() as ParameterProcessor<*>
                parameterTypes[parameterProcessor::class.supertypes.first().arguments.first().type!!] = parameterProcessor
            }
    }

}