package otus.homework.customview.presentation.expensesgraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.data.Payload
import otus.homework.customview.utils.DateUtils
import otus.homework.customview.utils.TAG
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import otus.homework.customview.utils.sp
import kotlin.math.min

class ExpensesGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultWidthPx = 300.dp.px
    private val defaultHeightPx = 300.dp.px
    private val axisPaddingPx = 32.dp.px
    private val axisTickHeightPx = 10.dp.px
    private val tickCountX = 10
    private val tickCountY = 5
    private val axisYMaxValue = 10f
    private val axisXMaxValue = 31f
    private val gridDotSizePx = 2.dp.px

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

    private var payloads: List<Payload>? = null
    private var payloadCategory: String? = null

    private val payloadsPerCategory = mutableListOf<Payload>()

    private val dateToExpenses = mutableMapOf<Int, Int>().withDefault { 0 }

    private val graphPath = Path()

    fun setPayloads(payloads: List<Payload>) {
        this.payloads = payloads
    }

    fun setPayloadCategory(payloadCategory: String) {
        this.payloadCategory = payloadCategory
        payloadsPerCategory.clear()
        payloadsPerCategory.addAll(getPayloads(payloadCategory))

        initDayToExpenses()

        invalidate()
    }

    private fun initDayToExpenses() {

        for (i in 0..31) {
            dateToExpenses[i] = 0
        }

        for (payload in payloadsPerCategory) {
            val day: Int = DateUtils.timestampToDayOfMonth(payload.time)
            val amount = payload.amount
            dateToExpenses[day] = dateToExpenses.getValue(day) + amount
        }
    }

    fun getPayloads(category: String): List<Payload> {
        return payloads?.filter { it.category == category } ?: emptyList()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(widthSize, heightSize)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize.also {
                Log.e(TAG, "onMeasure: EXACTLY: $widthSize $heightSize")
            }

            MeasureSpec.AT_MOST -> defaultWidthPx.coerceAtMost(size).also {
                Log.e(TAG, "onMeasure: AT_MOST: $widthSize $heightSize")
            }

            else -> defaultWidthPx.also {
                Log.e(TAG, "onMeasure: UNSPECIFIED: $widthSize $heightSize")
            }
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> size

            MeasureSpec.AT_MOST -> defaultHeightPx.coerceAtMost(size)

            else -> defaultHeightPx
        }

        setMeasuredDimension(width, height)
        Log.e(TAG, "setMeasuredDimension: $width $height")
    }

    override fun onDraw(canvas: Canvas) {
        drawTicksOnXAxis(canvas)
        drawTicksOnYAxis(canvas)
        drawAxis(canvas)
        drawDotsAtTickIntersections(canvas)

        drawGraph(canvas)
        drawDashedLinesThroughGraphPeaksPoints(canvas)
        drawPurchaseDots(canvas)
        drawTextAboveDashedLines(canvas)
    }

    private fun drawPurchaseDots(canvas: Canvas) {
        for ((day, amount) in dateToExpenses) {
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

    private fun drawDashedLinesThroughGraphPeaksPoints(canvas: Canvas) {
        for ((_, amount) in dateToExpenses) {
            if (amount != 0) {
                val y = mapAmountToYAxis(amount.toFloat())
                val screenY = axisToScreenY(y)
                canvas.drawLine(
                    axisPaddingPx.toFloat(),
                    screenY,
                    (width - axisPaddingPx).toFloat(),
                    screenY,
                    dashedLinePaint
                )
            }
        }
    }

    private fun drawTextAboveDashedLines(canvas: Canvas) {
        for ((_, amount) in dateToExpenses) {
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

    private fun drawGraph(canvas: Canvas) {
        graphPath.reset()
        graphPath.moveTo(axisToScreenX(0f), axisToScreenY(0f))

        for ((day, amount) in dateToExpenses) {
            val x = day.toFloat()
            val y = mapAmountToYAxis(amount.toFloat())
            graphPath.lineTo(axisToScreenX(x), axisToScreenY(y))
        }
        canvas.drawPath(graphPath, graphPaint)
    }

    fun mapAmountToYAxis(currentAmount: Float): Float {
        // Step 1: Find the maximum amount in the payloads list
        val maxAmount: Float = payloads?.maxOfOrNull { it.amount.toFloat() } ?: 0f

        // Step 2: Calculate the scale factor
        val scaleFactor = axisYMaxValue / maxAmount

        // Step 3: Map each amount to the y-axis using the scale factor
        return currentAmount * scaleFactor
    }

    fun mapAmountToYAxis(payloads: List<Payload>): List<Float> {
        // Step 1: Find the maximum amount in the payloads list
        val maxAmount: Float = payloads.maxOfOrNull { it.amount.toFloat() } ?: 0f

        // Step 2: Calculate the scale factor
        val scaleFactor = axisYMaxValue / maxAmount

        // Step 3: Map each amount to the y-axis using the scale factor
        return payloads.map { it.amount * scaleFactor }
    }

    private fun drawAxis(canvas: Canvas) {
        val xAxisYPos = (height - axisPaddingPx).toFloat()
        canvas.drawLine(axisPaddingPx.toFloat(), xAxisYPos, (width - axisPaddingPx).toFloat(), xAxisYPos, axisPaint)
        canvas.drawLine(
            axisPaddingPx.toFloat(),
            axisPaddingPx.toFloat(),
            axisPaddingPx.toFloat(),
            (height - axisPaddingPx).toFloat(),
            axisPaint
        )
    }

    private fun drawTicksOnXAxis(canvas: Canvas) {
        val xAxisYPos = (height - axisPaddingPx).toFloat()
        val xAxisXPos = (width - axisPaddingPx).toFloat()

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

    private fun drawTicksOnYAxis(canvas: Canvas) {
        val yAxisXPos = axisPaddingPx.toFloat()
        val yAxisYPos = (height - axisPaddingPx).toFloat()

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

    private fun axisToScreenCoordinates(
        axisX: Float,
        axisY: Float,
        maxXValue: Float = 31f,
        maxYValue: Float = 10f
    ): Point {

        val drawableWidth = width - 2 * axisPaddingPx
        val drawableHeight = height - 2 * axisPaddingPx

        val xScaleFactor = drawableWidth / maxXValue
        val yScaleFactor = drawableHeight / maxYValue

        val screenX = axisPaddingPx + axisX * xScaleFactor
        val screenY = height - axisPaddingPx - axisY * yScaleFactor

        return Point(screenX, screenY)
    }

    private fun axisToScreenX(axisX: Float, maxXValue: Float = 31f): Float {
        val drawableWidth = width - 2 * axisPaddingPx
        val xScaleFactor = drawableWidth / maxXValue
        return axisPaddingPx + axisX * xScaleFactor
    }

    private fun axisToScreenY(axisY: Float, maxYValue: Float = 10f): Float {
        val drawableHeight = height - 2 * axisPaddingPx
        val yScaleFactor = drawableHeight / maxYValue
        return height - axisPaddingPx - axisY * yScaleFactor
    }

    fun drawDots(canvas: Canvas, xStep: Float, yStep: Float, paint: Paint) {
        val maxX = axisXMaxValue
        val maxY = axisYMaxValue

        var x = 0f
        while (x <= maxX) {
            var y = 0f
            while (y <= maxY) {
                val screenX = axisToScreenX(x, maxX)
                val screenY = axisToScreenY(y, maxY)
                canvas.drawPoint(screenX, screenY, paint)
                y += yStep
            }
            x += xStep
        }
    }

    private fun drawDotsAtTickIntersections(canvas: Canvas) {
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
        val xAxisXPos = (width - axisPaddingPx).toFloat()
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
        val yAxisYPos = (height - axisPaddingPx).toFloat()
        val tickStepY = (yAxisYPos - axisPaddingPx) / tickCountY
        var currentY: Float = yAxisYPos - tickStepY
        for (i in 0 until tickCountY) {
            yTickPositions.add(currentY)
            currentY -= tickStepY
        }
        return yTickPositions
    }


    private data class Point(val x: Float, val y: Float)

}
