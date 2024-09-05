package otus.homework.customview.presentation.expensesgraph

import otus.homework.customview.data.Payload
import otus.homework.customview.utils.DateUtils

object GraphCalculator {

    fun getMaxDailyExpenseOfAllCategories(payloads: List<Payload>): Int {
        val categoryToDayToExpenses = mutableMapOf<String, MutableMap<Int, Int>>().withDefault { mutableMapOf() }

        for (payload in payloads) {
            val category = payload.category
            val day = DateUtils.timestampToDayOfMonth(payload.time)
            val dayToExpenses = categoryToDayToExpenses.getOrPut(category) { mutableMapOf() }.withDefault { 0 }
            dayToExpenses[day] = dayToExpenses.getValue(day) + payload.amount
            categoryToDayToExpenses[category] = dayToExpenses
        }

        val categoryToMaxDailyExpense = mutableMapOf<String, Int>()
        for ((category, dayToExpenses) in categoryToDayToExpenses) {
            categoryToMaxDailyExpense[category] = dayToExpenses.values.maxOrNull() ?: 0
        }

        return categoryToMaxDailyExpense.maxOf { it.value }
    }

}