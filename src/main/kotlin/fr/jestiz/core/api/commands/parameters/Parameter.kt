package fr.jestiz.core.api.commands.parameters

/**
 * Defines some informations about the parameter.
 * If the parameter is not required and it's default value is not set,
 * then it can be null.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Parameter(

    val name: String,
    val default: String = "",

    /**
     * Makes it so the rest of arguments in a command
     * are connected together after concatenated is found.
     * Useful when you need to specify a reason message on a ban for example.
     */
    val concat: Boolean = false,
    val required: Boolean = true
)