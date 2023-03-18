package fr.jestiz.core.api.commands.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
/**
 * All args must be of same size
 */
annotation class SubCommand(

    val parentCommand: String,
    val name: String,
    val permission: String = "",
    val async: Boolean = false,
    val description: String = "",
    val consoleOnly: Boolean = false,
    val playerOnly: Boolean = false

)
