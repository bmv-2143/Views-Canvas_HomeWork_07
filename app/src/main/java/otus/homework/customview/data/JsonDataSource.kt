package otus.homework.customview.data

import android.content.Context
import com.google.gson.Gson
import java.io.InputStreamReader

class JsonDataSource {

    fun loadPayloads(context: Context, payloadId: Int): List<Payload> {
        readJsonFile(context, payloadId).let {
            return parseJson(it)
        }
    }

    private fun readJsonFile(context: Context, resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)
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