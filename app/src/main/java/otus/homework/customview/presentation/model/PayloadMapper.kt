package otus.homework.customview.presentation.model

import otus.homework.customview.data.Payload
import otus.homework.customview.presentation.piechartview.PieChartColor

fun List<Payload>.toPieChartAngles() : List<PieChartAngle> {
    val angles = mutableListOf<PieChartAngle>()
    var startAngle = 0f
    var endAngle = 0f
    var color = PieChartColor.BLUE
    val totalAmount = getTotalAmount(this)
    for (payload in this) {
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