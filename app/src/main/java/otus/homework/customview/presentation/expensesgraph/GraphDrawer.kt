package otus.homework.customview.presentation.expensesgraph

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import otus.homework.customview.utils.px
import otus.homework.customview.utils.sp

class GraphDrawer(private val view: ExpensesGraphView) {

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

    private val graphPath = Path()

    internal fun drawPurchaseDots(canvas: Canvas) {
        for ((day, amount) in view.dayToExpenses) {
            val x = day.toFloat()
            val y = view.mapAmountToYAxis(amount.toFloat())

            if (amount != 0) {
                canvas.drawCircle(view.axisToScreenX(x), view.axisToScreenY(y), 15f, Paint().apply {
                    color = Color.RED
                    style = Paint.Style.FILL
                })
            }
        }
    }

    internal fun drawDashedLinesThroughGraphPeaksPoints(canvas: Canvas) {
        for ((_, amount) in view.dayToExpenses) {
            if (amount != 0) {
                val y = view.mapAmountToYAxis(amount.toFloat())
                val screenY = view.axisToScreenY(y)
                canvas.drawLine(
                    view.axisPaddingPx.toFloat(),
                    screenY,
                    (view.width - view.axisPaddingPx).toFloat(),
                    screenY,
                    dashedLinePaint
                )
            }
        }
    }

    internal fun drawTextAboveDashedLines(canvas: Canvas) {
        for ((_, amount) in view.dayToExpenses) {
            if (amount != 0) {
                val y = view.mapAmountToYAxis(amount.toFloat())
                val screenY = view.axisToScreenY(y)
                val text = amount.toString()
                val axisYRightOffset = 30
                val dashedLineAboveOffset = 20
                canvas.drawText(
                    text,
                    view.axisPaddingPx.toFloat() + axisYRightOffset,
                    screenY - dashedLineAboveOffset,
                    amountTextPaint
                )
            }
        }
    }

    internal fun drawGraph(canvas: Canvas) {
        graphPath.reset()
        graphPath.moveTo(view.axisToScreenX(0f), view.axisToScreenY(0f))

        for ((day, amount) in view.dayToExpenses) {
            val x = day.toFloat()
            val y = view.mapAmountToYAxis(amount.toFloat())
            graphPath.lineTo(view.axisToScreenX(x), view.axisToScreenY(y))
        }
        canvas.drawPath(graphPath, graphPaint)
    }

    internal fun drawAxis(canvas: Canvas) {
        val xAxisYPos = (view.height - view.axisPaddingPx).toFloat()
        canvas.drawLine(view.axisPaddingPx.toFloat(), xAxisYPos, (view.width - view.axisPaddingPx).toFloat(), xAxisYPos, axisPaint)
        canvas.drawLine(
            view.axisPaddingPx.toFloat(),
            view.axisPaddingPx.toFloat(),
            view.axisPaddingPx.toFloat(),
            (view.height - view.axisPaddingPx).toFloat(),
            axisPaint
        )
    }

    internal fun drawTicksOnXAxis(canvas: Canvas) {
        val xAxisYPos = (view.height - view.axisPaddingPx).toFloat()
        val xAxisXPos = (view.width - view.axisPaddingPx).toFloat()

        val tickStep = (xAxisXPos - view.axisPaddingPx) / view.tickCountX
        var currentX: Float = view.axisPaddingPx.toFloat() + tickStep

        for (i in 0 until view.tickCountX) {
            drawAxisXTick(canvas, currentX, xAxisYPos, view.axisTickHeightPx)
            currentX += tickStep
        }
    }

    private fun drawAxisXTick(canvas: Canvas, tickX: Float, tickStartY: Float, tickHeight: Int) {
        canvas.drawLine(tickX, tickStartY, tickX, tickStartY - tickHeight, axisTickPaint)
    }

    internal fun drawTicksOnYAxis(canvas: Canvas) {
        val yAxisXPos = view.axisPaddingPx.toFloat()
        val yAxisYPos = (view.height - view.axisPaddingPx).toFloat()

        val tickStep = (yAxisYPos - view.axisPaddingPx) / view.tickCountY
        var currentY: Float = yAxisYPos - tickStep

        for (i in 0 until view.tickCountY) {
            drawAxisYTick(canvas, yAxisXPos, currentY, view.axisTickHeightPx)
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
                canvas.drawCircle(x, y, view.gridDotSizePx.toFloat(), gridDotPaint)
            }
        }
    }

    private fun calculateGridDotsX(): MutableList<Float> {
        val xTickPositions = mutableListOf<Float>()
        val xAxisXPos = (view.width - view.axisPaddingPx).toFloat()
        val tickStepX = (xAxisXPos - view.axisPaddingPx) / view.tickCountX
        var currentX: Float = view.axisPaddingPx.toFloat() + tickStepX
        for (i in 0 until view.tickCountX) {
            xTickPositions.add(currentX)
            currentX += tickStepX
        }
        return xTickPositions
    }

    private fun calculateGridDotsY(): MutableList<Float> {
        val yTickPositions = mutableListOf<Float>()
        val yAxisYPos = (view.height - view.axisPaddingPx).toFloat()
        val tickStepY = (yAxisYPos - view.axisPaddingPx) / view.tickCountY
        var currentY: Float = yAxisYPos - tickStepY
        for (i in 0 until view.tickCountY) {
            yTickPositions.add(currentY)
            currentY -= tickStepY
        }
        return yTickPositions
    }
}