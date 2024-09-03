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
    private val tickCount = 10

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
        drawAxis(canvas)
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

        val tickStep = (xAxisXPos - axisPaddingPx) / tickCount
        var currentX : Float = axisPaddingPx.toFloat() + tickStep

        for (i in 0 until tickCount) {
            drawAxisXTick(canvas, currentX, xAxisYPos, axisTickHeightPx)
            currentX += tickStep
        }
    }

    private fun drawAxisXTick(canvas: Canvas, tickX: Float, tickStartY: Float, tickHeight: Int) {
        canvas.drawLine(tickX, tickStartY, tickX, tickStartY - tickHeight, axisTickPaint)
    }

}
