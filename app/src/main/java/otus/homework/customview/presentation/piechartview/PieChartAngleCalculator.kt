package otus.homework.customview.presentation.piechartview

import otus.homework.customview.data.Payload

class PieChartAngleCalculator {

    fun calculateAngles(payloads: List<Payload>): List<PieChartAngle> {
        val angles = mutableListOf<PieChartAngle>()
        var startAngle = 0f
        var endAngle = 0f
        var color = PieChartColor.BLUE
        val totalAmount = getTotalAmount(payloads)
        for (payload in payloads) {
            endAngle += getAngleForPayload(payload, totalAmount)
            angles.add(PieChartAngle(payload.id, payload.category, startAngle, endAngle, color))
            startAngle = endAngle
            color = color.nextColor()
        }
        return angles
    }

    private fun getAngleForPayload(payload: Payload, totalAmount: Int): Float {
        return (payload.amount.toFloat() / totalAmount) * 360f
    }

    private fun getTotalAmount(payloads: List<Payload>): Int {
        return payloads.sumOf { it.amount }
    }

}