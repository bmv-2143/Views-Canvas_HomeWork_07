package otus.homework.customview.utils

import java.util.Calendar
import java.util.Date

object DateUtils {

    fun timestampToDayOfMonth(timestamp: Long): Int {
        val calendar = getCalendar(timestamp)
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    fun getCalendar(payloadTimestamp: Long): Calendar {
        val date = Date(payloadTimestamp * 1000) // Convert seconds to milliseconds
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    fun getHumanReadableDate(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based in Calendar
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        return String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second)
    }

}