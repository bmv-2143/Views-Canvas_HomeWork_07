package otus.homework.customview.data

import android.content.Context
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CustomViewRepository @Inject constructor(@ApplicationContext private val applicationContext: Context) {

    fun loadPayloads(@RawRes payloadId: Int): List<Payload> =
        JsonDataSource().loadPayloads(applicationContext, payloadId)
}