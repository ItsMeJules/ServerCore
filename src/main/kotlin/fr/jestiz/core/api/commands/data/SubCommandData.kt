package fr.jestiz.core.api.commands.data

import fr.jestiz.core.Core
import fr.jestiz.core.api.commands.annotations.SubCommand
import fr.jestiz.core.api.commands.parameters.Parameter
import fr.jestiz.core.api.commands.parameters.ParameterData
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.withNullability

class SubCommandData(subCommand: SubCommand, val commandClass: Any, val function: KFunction<*>) {

    val parentCommand = subCommand.parentCommand
    val name =  subCommand.name
    val permission = subCommand.permission
    val description = subCommand.description
    val async = subCommand.async
    val playerOnly = subCommand.playerOnly
    val consoleOnly = subCommand.consoleOnly

    val subArgs = name.split(" ")
    val argsLength = name.count { it == ' ' } + 1

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