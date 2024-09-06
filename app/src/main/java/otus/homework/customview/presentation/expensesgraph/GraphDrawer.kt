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
    private val axisTickHeightPx = 10.dp.px
    private val tickCountX = 10
    private val tickCountY = 5
    private val axisYMaxValue = 10f
    private val gridDotSizePx = 2.dp.px
    private val graphPath = Path()

    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 15f
        style = Paint.Style.FILL
    }

    private val axisTickPaint = Paint().apply {
        color = Color.DKGRAY
        strokeWidth = 5f
        style = Paint.Style.FILL
    }

    private val gridDotPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.FILL
    }

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

    internal fun drawAxis(canvas: Canvas) {
        val xAxisYPos = (view.height - axisPaddingPx).toFloat()
        canvas.drawLine(
            axisPaddingPx.toFloat(), xAxisYPos, (view.width - axisPaddingPx).toFloat(), xAxisYPos, axisPaint
        )
        canvas.drawLine(
            axisPaddingPx.toFloat(),
            axisPaddingPx.toFloat(),
            axisPaddingPx.toFloat(),
            (view.height - axisPaddingPx).toFloat(),
            axisPaint
        )
    }

    internal fun drawTicksOnXAxis(canvas: Canvas) {
        val xAxisYPos = (view.height - axisPaddingPx).toFloat()
        val xAxisXPos = (view.width - axisPaddingPx).toFloat()

        val tickStep = (xAxisXPos - axisPaddingPx) / tickCountX
        var currentX: Float = axisPaddingPx.toFloat() + tickStep

        for (i in 0 until tickCountX) {
            drawAxisXTick(canvas, currentX, xAxisYPos, axisTickHeightPx)
            currentX += tickStep
        }
    }

    private fun drawAxisXTick(canvas: Canvas, tickX: Float, tickStartY: Float, tickHeight: Int) {
        canvas.drawLine(tickX, tickStartY, tickX, tickStartY - tickHeight, axisTickPaint)
    }

    internal fun drawTicksOnYAxis(canvas: Canvas) {
        val yAxisXPos = axisPaddingPx.toFloat()
        val yAxisYPos = (view.height - axisPaddingPx).toFloat()

        val tickStep = (yAxisYPos - axisPaddingPx) / tickCountY
        var currentY: Float = yAxisYPos - tickStep

        for (i in 0 until tickCountY) {
            drawAxisYTick(canvas, yAxisXPos, currentY, axisTickHeightPx)
            currentY -= tickStep
        }
    }

    private fun drawAxisYTick(canvas: Canvas, tickX: Float, tickStartY: Float, tickHeight: Int) {
        canvas.drawLine(tickX, tickStartY, tickX + tickHeight, tickStartY, axisTickPaint)
    }

    internal fun drawDotsAtTickIntersections(canvas: Canvas) {
        val gridDotsX = calculateGridDotsX()
        val gridDotsY = calculateGridDotsY()
        for (x in gridDotsX) {
            for (y in gridDotsY) {
                canvas.drawCircle(x, y, gridDotSizePx.toFloat(), gridDotPaint)
            }
        }
    }

    private fun calculateGridDotsX(): MutableList<Float> {
        val xTickPositions = mutableListOf<Float>()
        val xAxisXPos = (view.width - axisPaddingPx).toFloat()
        val tickStepX = (xAxisXPos - axisPaddingPx) / tickCountX
        var currentX: Float = axisPaddingPx.toFloat() + tickStepX
        for (i in 0 until tickCountX) {
            xTickPositions.add(currentX)
            currentX += tickStepX
        }
        return xTickPositions
    }

    private fun calculateGridDotsY(): MutableList<Float> {
        val yTickPositions = mutableListOf<Float>()
        val yAxisYPos = (view.height - axisPaddingPx).toFloat()
        val tickStepY = (yAxisYPos - axisPaddingPx) / tickCountY
        var currentY: Float = yAxisYPos - tickStepY
        for (i in 0 until tickCountY) {
            yTickPositions.add(currentY)
            currentY -= tickStepY
        }
        return yTickPositions
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