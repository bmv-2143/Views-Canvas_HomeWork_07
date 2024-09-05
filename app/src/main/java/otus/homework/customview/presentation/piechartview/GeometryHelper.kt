package otus.homework.customview.presentation.piechartview

import android.graphics.RectF
import android.view.MotionEvent
import otus.homework.customview.presentation.model.PieChartAngle

object GeometryHelper {

    fun isClickInsidePieChart(event: MotionEvent, pieChartContainer : RectF): Boolean {
        val x = event.x
        val y = event.y

        val centerX = pieChartContainer.centerX()
        val centerY = pieChartContainer.centerY()
        val radius = pieChartContainer.width() / 2

        val distance = Math.sqrt(Math.pow((x - centerX).toDouble(), 2.0) + Math.pow((y - centerY).toDouble(), 2.0))

        return distance <= radius
    }

    fun isClickWithinPieChartAngle(event: MotionEvent, angle: PieChartAngle, pieChartContainer : RectF): Boolean {
        val x = event.x
        val y = event.y

        val centerX = pieChartContainer.centerX()
        val centerY = pieChartContainer.centerY()

        // Calculate the angle of the click relative to the center of the pie chart
        val clickAngle = Math.toDegrees(Math.atan2((y - centerY).toDouble(), (x - centerX).toDouble())).toFloat()

        // Normalize the angle to be within the range [0, 360)
        val normalizedClickAngle = (clickAngle + 360) % 360

        // Check if the normalized click angle is within the start and end angles of the PieChartAngle
        return normalizedClickAngle >= angle.startAngle && normalizedClickAngle <= angle.endAngle
    }

}