package otus.homework.customview.presentation.expensesgraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.utils.TAG
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import otus.homework.customview.utils.sp
import otus.homework.customview.utils.toBundle
import otus.homework.customview.utils.toMap
import kotlin.math.min

class ExpensesGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultWidthPx = 300.dp.px
    private val defaultHeightPx = 300.dp.px
    private val axisPaddingPx = 32.dp.px
    private val axisTickHeightPx = 10.dp.px
    private val tickCountX = 10
    private val tickCountY = 5
    private val axisYMaxValue = 10f
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

    private val dayToExpenses = mutableMapOf<Int, Int>().withDefault { 0 }
    private var maxCategoryTotalAmount = 0

    private val graphPath = Path()

    fun setMaxDailyExpenseOfAllCategories(maxCategoryTotalAmount: Int) {
        this.maxCategoryTotalAmount = maxCategoryTotalAmount
    }

    fun setDaysToExpenses(dayToExpenses: Map<Int, Int>) {
        this.dayToExpenses.clear()
        this.dayToExpenses.putAll(dayToExpenses)
        invalidate()
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
        for ((day, amount) in dayToExpenses) {
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
        for ((_, amount) in dayToExpenses) {
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
        for ((_, amount) in dayToExpenses) {
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

        for ((day, amount) in dayToExpenses) {
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

    private val superStateKey = "superState"
    private val selectedCategoryKey = "dayToExpensesKey"
    private val maxCategoryTotalAmountKey = "maxCategoryTotalAmountKey"

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable(superStateKey, superState)
        bundle.putInt(maxCategoryTotalAmountKey, maxCategoryTotalAmount)
        bundle.putBundle(selectedCategoryKey, dayToExpenses.toBundle())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable(superStateKey))

        // Restore dayToExpenses map
        val dayToExpensesBundle = bundle.getBundle(selectedCategoryKey)
        if (dayToExpensesBundle != null) {
            dayToExpenses.putAll(dayToExpensesBundle.toMap())
        }
        maxCategoryTotalAmount = bundle.getInt(maxCategoryTotalAmountKey)
        invalidate()
    }
}
