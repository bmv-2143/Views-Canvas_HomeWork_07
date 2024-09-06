package otus.homework.customview.presentation.expensesgraph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import otus.homework.customview.utils.sp

class GraphDrawer(
    private val view: ExpensesGraphView,
    private val maxCategoryTotalAmount: Int,
    private val daysToExpenses: Map<Int, Int>,
) {
    private val axisPaddingPx = 32.dp.px
    private val axisYMaxValue = 10f
    private val graphPath = Path()

    private val graphPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val dashedLinePaint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 5f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
    }

    private val amountTextPaint = Paint().apply {
        color = Color.BLACK
        textSize = 16.sp.px.toFloat()
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
    }

    internal fun drawPurchaseDots(canvas: Canvas) {
        for ((day, amount) in daysToExpenses) {
            val x = day.toFloat()
            val y = mapAmountToYAxis(amount.toFloat())

            if (amount != 0) {
                canvas.drawCircle(axisToScreenX(x), axisToScreenY(y), 15f, Paint().apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                })
            }
        }
    }

    internal fun drawDashedLinesThroughGraphPeaksPoints(canvas: Canvas) {
        for ((_, amount) in daysToExpenses) {
            if (amount != 0) {
                val y = mapAmountToYAxis(amount.toFloat())
                val screenY = axisToScreenY(y)
                canvas.drawLine(
                    axisPaddingPx.toFloat(),
                    screenY,
                    (view.width - axisPaddingPx).toFloat(),
                    screenY,
                    dashedLinePaint
                )
            }
        }
    }

    internal fun drawTextAboveDashedLines(canvas: Canvas) {
        for ((_, amount) in daysToExpenses) {
            if (amount != 0) {
                val y = mapAmountToYAxis(amount.toFloat())
                val screenY = axisToScreenY(y)
                val text = amount.toString()
                val axisYRightOffset = 30
                val dashedLineAboveOffset = 20
                canvas.drawText(
                    text,
                    axisPaddingPx.toFloat() + axisYRightOffset,
                    screenY - dashedLineAboveOffset,
                    amountTextPaint
                )
            }
        }
    }

    internal fun drawGraph(canvas: Canvas) {
        graphPath.reset()
        graphPath.moveTo(axisToScreenX(0f), axisToScreenY(0f))

        for ((day, amount) in daysToExpenses) {
            val x = day.toFloat()
            val y = mapAmountToYAxis(amount.toFloat())
            graphPath.lineTo(axisToScreenX(x), axisToScreenY(y))
        }
        canvas.drawPath(graphPath, graphPaint)
    }

    private fun mapAmountToYAxis(currentAmount: Float): Float {
        val maxAmount: Float = maxCategoryTotalAmount.toFloat()
        val scaleFactor = axisYMaxValue / maxAmount
        return currentAmount * scaleFactor
    }

    private fun axisToScreenX(axisX: Float, maxXValue: Float = 31f): Float {
        val drawableWidth = view.width - 2 * axisPaddingPx
        val xScaleFactor = drawableWidth / maxXValue
        return axisPaddingPx + axisX * xScaleFactor
    }

    private fun axisToScreenY(axisY: Float, maxYValue: Float = 10f): Float {
        val drawableHeight = view.height - 2 * axisPaddingPx
        val yScaleFactor = drawableHeight / maxYValue
        return view.height - axisPaddingPx - axisY * yScaleFactor
    }
}