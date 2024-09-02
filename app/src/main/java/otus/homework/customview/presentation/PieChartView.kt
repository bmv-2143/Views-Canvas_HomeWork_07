package otus.homework.customview.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import otus.homework.customview.data.Payload
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import otus.homework.customview.utils.sp
import kotlin.math.min


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val defaultWidthPx = 300.dp.px
    private val defaultHeightPx = 300.dp.px
    private val pieChartPaddingPx = 32.dp.px

    private val blackPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private val sectorPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private var currentColor = PieChartColor.BLUE
    private val pieChartContainer = RectF()

    private var payloads: List<Payload> = emptyList()

    fun setPayloads(payloads: List<Payload>) {
        this.payloads = payloads
        invalidate()
    }

    private fun getTotalAmount(): Int = payloads.sumOf { it.amount }

    private fun getAngleForPayload(payload: Payload): Float = (payload.amount.toFloat() / getTotalAmount()) * 360f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val size = min(widthSize, heightSize)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> defaultWidthPx.coerceAtMost(size)
            else -> defaultWidthPx
        }

        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> defaultHeightPx.coerceAtMost(size)
            else -> defaultHeightPx
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        setPieChartContainerRectBounds()
        drawPieChart(canvas)
        drawTextBelowPieChart(canvas, blackPaint, "Payload name")
    }

    private fun setPieChartContainerRectBounds() {
        val centerX = width / 2
        val centerY = height / 2
        val pieChartSizeWithPadding = measuredWidth - pieChartPaddingPx
        val left = centerX - pieChartSizeWithPadding / 2
        val top = centerY - pieChartSizeWithPadding / 2
        val right = centerX + pieChartSizeWithPadding / 2
        val bottom = centerY + pieChartSizeWithPadding / 2
        pieChartContainer.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    private fun drawPieChart(canvas: Canvas) {
        var startAngle = 0f

        for (payload in payloads) {
            drawSector(canvas, payload, startAngle)
            currentColor = currentColor.nextColor()
            startAngle += getAngleForPayload(payload)
        }
    }

    private fun drawSector(canvas: Canvas, payload: Payload, startAngle: Float) {
        val angle = getAngleForPayload(payload)
        sectorPaint.color = currentColor.intColor
        canvas.drawArc(pieChartContainer, startAngle, angle, true, sectorPaint)
    }

    fun drawTextBelowPieChart(canvas: Canvas, paint : Paint, text : String) {
        paint.textSize = 32.sp.px.toFloat()
        paint.textAlign = Paint.Align.CENTER

        // Calculate the position for the text
//        val x = pieChartContainer.left
        val x = pieChartContainer.left + (pieChartContainer.right - pieChartContainer.left) / 2
        val y = pieChartContainer.bottom + paint.textSize + 16.dp.px // Adding some padding

        // Draw the text
        canvas.drawText(text, x, y, paint)

        val textWidth = paint.measureText("Hello Ivan!")
    }

}