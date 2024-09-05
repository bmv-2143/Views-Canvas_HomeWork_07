package otus.homework.customview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import otus.homework.customview.R
import otus.homework.customview.data.CustomViewRepository
import otus.homework.customview.data.Payload
import otus.homework.customview.presentation.expensesgraph.GraphCalculator
import otus.homework.customview.presentation.model.PieChartAngle
import otus.homework.customview.presentation.model.toPieChartAngles
import otus.homework.customview.utils.DateUtils
import javax.inject.Inject

@HiltViewModel
class CustomViewViewModel @Inject constructor(
    private val repository: CustomViewRepository,
    dataLoadDispatcher: CoroutineDispatcher
) : ViewModel() {

    private lateinit var _payloads: List<Payload>

    private val _pieChartAngles = MutableStateFlow<List<PieChartAngle>>(emptyList())
    val pieChartAngles = _pieChartAngles.asStateFlow()

    init {
        viewModelScope.launch(dataLoadDispatcher) {
            _payloads = repository.loadPayloads(R.raw.payload)
            _pieChartAngles.value = _payloads.toPieChartAngles()
        }
    }

    fun getMaxDailyExpenseOfAllCategories(): Int {
        return GraphCalculator.getMaxDailyExpenseOfAllCategories(_payloads)
    }

    fun getDaysToExpenses(payloadCategory: String): Map<Int, Int> {
        val daysToExpenses = mutableMapOf<Int, Int>().withDefault { 0 }

        for (i in 0..31) {
            daysToExpenses[i] = 0
        }

        for (payload in getPayloadsForCategory(payloadCategory)) {
            val day: Int = DateUtils.timestampToDayOfMonth(payload.time)
            val amount = payload.amount
            daysToExpenses[day] = daysToExpenses.getValue(day) + amount
        }

        return daysToExpenses
    }

    private fun getPayloadsForCategory(category: String): List<Payload> {
        return _payloads.filter { it.category == category }
    }
}

