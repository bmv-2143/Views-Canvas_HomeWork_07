package otus.homework.customview.data

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject

class JsonDataSource @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    fun loadPayloads(payloadId: Int): List<Payload> {
        readJsonFile(payloadId).let {
            return parseJson(it)
        }
    }

    private fun readJsonFile(resId: Int): String {
        val inputStream = applicationContext.resources.openRawResource(resId)
        val reader = InputStreamReader(inputStream)
        return reader.readText().also {
            reader.close()
        }
    }

    private fun parseJson(jsonString: String): List<Payload> {
        val gson = Gson()
        return gson.fromJson(jsonString, Array<Payload>::class.java).toList()
    }

}