package be.florien.anyflow.data

import java.text.SimpleDateFormat
import java.util.*

object TimeOperations {

    private const val AMPACHE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private val ampacheCompleteFormatter = SimpleDateFormat(AMPACHE_DATE_FORMAT, Locale.getDefault())
    private val ampacheGetFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    var currentTimeUpdater: CurrentTimeUpdater? = null

    fun getCurrentDate(): Calendar {
        var calendar = Calendar.getInstance()
        calendar = currentTimeUpdater?.getCurrentTimeUpdated(calendar) ?: calendar
        return calendar
    }

    fun getCurrentDatePlus(field: Int, increment: Int): Calendar {
        var calendar = Calendar.getInstance().apply {
            add(field, increment)
        }
        calendar = currentTimeUpdater?.getCurrentTimeUpdated(calendar) ?: calendar
        return calendar
    }

    fun getDateFromMillis(millis: Long): Calendar = Calendar.getInstance().apply {
        timeInMillis = millis
    }

    fun getDateFromAmpacheComplete(formatted: String): Calendar = Calendar.getInstance().apply {
        time = ampacheCompleteFormatter.parse(formatted)
                ?: throw IllegalArgumentException("The provided string could not be parsed to an ampache date")
    }

    fun getAmpacheCompleteFormatted(time: Calendar): String = ampacheCompleteFormatter.format(time.time)

    fun getAmpacheGetFormatted(time: Calendar): String = ampacheGetFormatter.format(time.time)

    interface CurrentTimeUpdater {
        fun getCurrentTimeUpdated(current: Calendar): Calendar
    }

}