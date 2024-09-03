package otus.homework.customview.presentation.expensesgraph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.data.Payload
import otus.homework.customview.utils.TAG
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import kotlin.math.min

class ExpensesGraphView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val defaultWidthPx = 300.dp.px
    private val defaultHeightPx = 300.dp.px
    private val axisPaddingPx = 16.dp.px
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

    private var payload: Payload? = null

    fun setPayload(payload: Payload) {
        this.payload = payload
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
        // Calculate tick positions on X axis
        val xTickPositions = mutableListOf<Float>()
        val xAxisXPos = (width - axisPaddingPx).toFloat()
        val tickStepX = (xAxisXPos - axisPaddingPx) / tickCountX
        var currentX: Float = axisPaddingPx.toFloat() + tickStepX
        for (i in 0 until tickCountX) {
            xTickPositions.add(currentX)
            currentX += tickStepX
        }

        // Calculate tick positions on Y axis
        val yTickPositions = mutableListOf<Float>()
        val yAxisYPos = (height - axisPaddingPx).toFloat()
        val tickStepY = (yAxisYPos - axisPaddingPx) / tickCountY
        var currentY: Float = yAxisYPos - tickStepY
        for (i in 0 until tickCountY) {
            yTickPositions.add(currentY)
            currentY -= tickStepY
        }

        // Draw dots at intersections of X and Y tick positions
        for (x in xTickPositions) {
            for (y in yTickPositions) {
                canvas.drawCircle(x, y, gridDotSizePx.toFloat(), gridDotPaint)
            }
        }
    }



    private data class Point(val x: Float, val y: Float)
}
