package otus.homework.customview.presentation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.data.Payload
import otus.homework.customview.utils.TAG
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
        strokeWidth = 20f
        style = Paint.Style.STROKE
    }

    private val sectorPaint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    private val pieChartContainer = RectF()

    private var payloads: List<Payload> = emptyList()

    private val angles: MutableList<PieChartAngle> = mutableListOf()

    private var selectedAngle: PieChartAngle? = null

    fun setPayloads(payloads: List<Payload>) {
        this.payloads = payloads
        calculateAngles()
        invalidate()
    }

    private fun calculateAngles() {
        angles.clear()  // Clear the previous angles
        var startAngle = 0f
        var endAngle = 0f
        var color = PieChartColor.BLUE
        for (payload in payloads) {
            endAngle += getAngleForPayload(payload)
            angles.add(PieChartAngle(payload.id, startAngle, endAngle, color))
            startAngle = endAngle
            color = color.nextColor()
        }
        Log.e(TAG, "Angles: $angles")
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

        if (selectedAngle != null) {
            selectedAngle?.draw(canvas, pieChartContainer, blackPaint)
        }

        selectedAngle?.let {
            canvas.drawArc(pieChartContainer, it.startAngle, it.sweepAngle, true, blackPaint)
            Log.e(TAG, "SELECTED: ${it.startAngle}, ${it.endAngle}, color: ${blackPaint.color}")
        }
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
        for (angle in angles) {
            drawSector(canvas, angle)
        }
    }

    private fun drawSector(canvas: Canvas, angle: PieChartAngle) {
        sectorPaint.color = angle.color.intColor
        canvas.drawArc(pieChartContainer, angle.startAngle, angle.sweepAngle, true, sectorPaint)
        Log.e(TAG, "Draw angle: ${angle.startAngle}, ${angle.endAngle}, color: ${sectorPaint.color}")
        Log.e(TAG, "Draw angle: $angle")
    }

    fun drawTextBelowPieChart(canvas: Canvas, paint: Paint, text: String) {
        paint.textSize = 32.sp.px.toFloat()
        paint.textAlign = Paint.Align.CENTER

        // Calculate the position for the text
        val x = pieChartContainer.left + (pieChartContainer.right - pieChartContainer.left) / 2
        val y = pieChartContainer.bottom + paint.textSize + 16.dp.px // Adding some padding

        canvas.drawText(text, x, y, paint)

        val textWidth = paint.measureText("Hello Ivan!")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isClickInsidePieChart(event)) {
            selectedAngle = getSelectedAngle(event)
            invalidate()
        }
        return true
    }

    private fun getSelectedAngle(event: MotionEvent): PieChartAngle? {
        for (angle in angles) {
            if (isClickWithinPieChartAngle(event, angle)) {
                return angle
            }
        }
        return null
    }

    private fun isClickInsidePieChart(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        val centerX = pieChartContainer.centerX()
        val centerY = pieChartContainer.centerY()
        val radius = pieChartContainer.width() / 2

        val distance = Math.sqrt(Math.pow((x - centerX).toDouble(), 2.0) + Math.pow((y - centerY).toDouble(), 2.0))

        return distance <= radius
    }

    private fun isClickWithinPieChartAngle(event: MotionEvent, angle: PieChartAngle): Boolean {
        val x = event.x
        val y = event.y

        val centerX = pieChartContainer.centerX()
        val centerY = pieChartContainer.centerY()

        // Calculate the angle of the click relative to the center of the pie chart
        val clickAngle = Math.toDegrees(Math.atan2((y - centerY).toDouble(), (x - centerX).toDouble())).toFloat()

        // Normalize the angle to be within the range [0, 360)
        val normalizedClickAngle = (clickAngle + 360) % 360

        // Check if the normalized click angle is within the start and end angles of the PieChartAngle
        return normalizedClickAngle >= angle.startAngle && normalizedClickAngle <= angle.endAngle
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private data class PieChartAngle(
        val id: Int,
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
}