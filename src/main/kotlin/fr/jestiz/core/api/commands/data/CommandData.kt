package fr.jestiz.core.api.commands.data

import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.annotations.Command
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.api.commands.parameters.ParameterData
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.withNullability


class CommandData(command: Command, val commandClass: Any, val function: KFunction<*>) {

    // Makes it so you can use /plugin:command
    val names = command.names.flatMap {
        var name = it.lowercase(Locale.getDefault())
        listOf(name, "${Core.instance.name}:$name")
    }

    val permission = command.permission
    val description = command.description
    val async = command.async
    val playerOnly = command.playerOnly
    val consoleOnly = command.consoleOnly

    val parameters: MutableList<ParameterData> = ArrayList()

    init {
        for (i in 2 until function.parameters.size) {
            var parameterAnnotation: Parameter? = null

            for (annotation in function.parameters[i].annotations) {
                if (annotation is Parameter) {
                    parameterAnnotation = annotation
                    break
                }
            }

            if (parameterAnnotation == null)
                throw IllegalArgumentException("Function ${function.name} has a parameter without a @Parameter annotation")

            var kTypeParameter = function.parameters[i].type
            if (kTypeParameter.isMarkedNullable)
                kTypeParameter = kTypeParameter.withNullability(false)
            else if (!parameterAnnotation.required)
                throw IllegalArgumentException("Function ${function.name} has a parameter that's not required but argument is not marked nullable!")

            parameters.add(ParameterData(parameterAnnotation, kTypeParameter))
        }
    }

}