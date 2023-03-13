package fr.jestiz.core.time

import com.google.common.primitives.Longs
import fr.jestiz.core.Constants
import java.util.*
import java.util.concurrent.TimeUnit

class DurationParser(private val toParse: String) {

    val millisTime: Long

    init {
        millisTime = when (toParse.uppercase(Locale.getDefault())) {
            "EVER", "PERMANENT",
            "FOREVER", "PERM" -> Long.MAX_VALUE
            else -> toMillis()
        }
    }

    private fun toMillis(): Long {
        val matches = toParse.split(Constants.DURATION_PARSER_REGEX)
        var millisTime: Long = 0

        if (matches.size <= 1)
            return -1

        for (i in matches.indices step 2) {
            val longAmount = Longs.tryParse(matches[i]) ?: return -1;
            val unitChar = matches[i + 1]

            millisTime += when (unitChar) {
                "s" -> TimeUnit.SECONDS.toMillis(longAmount)
                "m" -> TimeUnit.MINUTES.toMillis(longAmount)
                "h" -> TimeUnit.HOURS.toMillis(longAmount)
                "d" -> TimeUnit.DAYS.toMillis(longAmount)
                "M" -> TimeUnit.DAYS.toMillis(30 * longAmount)
                "y" -> TimeUnit.DAYS.toMillis(365 * longAmount)
                else -> return -1
            }
        }
        return millisTime
    }

}