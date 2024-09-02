package otus.homework.customview.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import otus.homework.customview.utils.TAG
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import otus.homework.customview.utils.sp


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {


    private val yellowPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private val blackPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private val greenStrokePaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val boldRedPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 30f
    }

    private val pieChartContainer = RectF()


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // todo: the view should be always square? If yes, then should use something like min(width, height) here ...

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY ->
                widthSize
                    .also {
                        Log.e(TAG, "onMeasure: EXACTLY: widthSize/heightSize = $widthSize / $heightSize")
                    }

            MeasureSpec.AT_MOST ->
                DEFAULT_WIDTH.coerceAtMost(widthSize)
                    .also {
                        Log.e(TAG, "onMeasure: AT_MOST: widthSize/heightSize = $widthSize / $heightSize")
                    }

            else ->
                DEFAULT_WIDTH.also {
                    Log.e(TAG, "onMeasure: UNSPECIFIED: widthSize/heightSize = $widthSize / $heightSize")
                }
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> DEFAULT_HEIGHT.coerceAtMost(heightSize)
            else -> DEFAULT_HEIGHT
        }

        setMeasuredDimension(width, height)
        Log.e(TAG, "setMeasuredDimension: width/height = $width / $height")
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2
        val centerY = height / 2

        setPieChartContainerRectBounds(centerX, centerY)
        drawPieChart(canvas)
        canvas.drawLine(pieChartContainer.left, 0f, pieChartContainer.left, height.toFloat(), yellowPaint)

        drawTextBelowPieChart(canvas)

        // rect offset
        pieChartContainer.offset(30f, -30f)
        canvas.drawArc(pieChartContainer, 270f, 90f, true, greenStrokePaint)

        canvas.drawLine(0f, centerY.toFloat(), width.toFloat(), centerY.toFloat(), yellowPaint)
        canvas.drawLine(centerX.toFloat(), 0f, centerX.toFloat(), height.toFloat(), yellowPaint)
    }

    private fun setPieChartContainerRectBounds(centerX: Int, centerY: Int) {

        val pieChartSizeWithPadding = width - 32.dp.px

        val left = centerX - pieChartSizeWithPadding / 2
        val top = centerY - pieChartSizeWithPadding / 2
        val right = centerX + pieChartSizeWithPadding / 2
        val bottom = centerY + pieChartSizeWithPadding / 2
        pieChartContainer.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    private fun drawPieChart(canvas: Canvas) {
        canvas.drawRect(pieChartContainer, blackPaint)

        // draw lines at borders of pieChartContainer with yellow paint
        drawBorderLines(canvas)

        canvas.drawArc(pieChartContainer, 0f, 90f, true, blackPaint)
        canvas.drawArc(pieChartContainer, 90f, 90f, true, greenStrokePaint)
        canvas.drawArc(pieChartContainer, 180f, 90f, true, boldRedPaint)
    }

    private fun drawBorderLines(canvas: Canvas) {
        // Draw top border line
        canvas.drawLine(
            pieChartContainer.left,
            pieChartContainer.top,
            pieChartContainer.right,
            pieChartContainer.top,
            yellowPaint
        )

        // Draw bottom border line
        canvas.drawLine(
            pieChartContainer.left,
            pieChartContainer.bottom,
            pieChartContainer.right,
            pieChartContainer.bottom,
            yellowPaint
        )

        // Draw left border line
        canvas.drawLine(
            pieChartContainer.left,
            pieChartContainer.top,
            pieChartContainer.left,
            pieChartContainer.bottom,
            yellowPaint
        )

        // Draw right border line
        canvas.drawLine(
            pieChartContainer.right,
            pieChartContainer.top,
            pieChartContainer.right,
            pieChartContainer.bottom,
            yellowPaint
        )
    }

    private fun drawTextBelowPieChart(canvas: Canvas) {
        val text = "Your Text Here"
        blackPaint.textSize = 32.sp.px.toFloat()
        blackPaint.textAlign = Paint.Align.CENTER

        // Calculate the position for the text
//        val x = pieChartContainer.left
        val x = pieChartContainer.left + (pieChartContainer.right - pieChartContainer.left) / 2
        val y = pieChartContainer.bottom + blackPaint.textSize + 16.dp.px // Adding some padding

        // Draw the text
        canvas.drawText(text, x, y, blackPaint)

        val textWidth = blackPaint.measureText("Hello Ivan!")
    }

    companion object {
        private const val DEFAULT_WIDTH = 600
        private const val DEFAULT_HEIGHT = 600
    }
}