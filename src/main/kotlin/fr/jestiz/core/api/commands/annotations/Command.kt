package fr.jestiz.core.api.commands.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Command(

    val names: Array<String>,
    val permission: String = "",
    val async: Boolean = false,
    val description: String = "",
    val consoleOnly: Boolean = false,
    val playerOnly: Boolean = false

)