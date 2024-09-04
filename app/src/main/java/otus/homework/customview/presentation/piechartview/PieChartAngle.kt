package otus.homework.customview.presentation.piechartview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

data class PieChartAngle(
    val id: Int,
    val category: String,
    val startAngle: Float,
    val endAngle: Float,
    val color: PieChartColor
) {

    val sweepAngle: Float
        get() = endAngle - startAngle

    fun draw(canvas: Canvas, pieChartContainer: RectF, sectorPaint: Paint) {
        canvas.drawArc(pieChartContainer, startAngle, endAngle - startAngle, true, sectorPaint)
    }
}