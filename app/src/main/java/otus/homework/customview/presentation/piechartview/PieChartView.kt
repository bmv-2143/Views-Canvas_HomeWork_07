package otus.homework.customview.presentation.piechartview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.data.Payload
import otus.homework.customview.presentation.piechartview.GeometryHelper.isClickWithinPieChartAngle
import otus.homework.customview.utils.dp
import otus.homework.customview.utils.px
import kotlin.math.min


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val defaultWidthPx = 300.dp.px
    private val defaultHeightPx = 300.dp.px
    private val pieChartPaddingPx = 32.dp.px

    private val selectionPaint = Paint().apply {
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

    private var selectedSector: PieChartAngle? = null
    private val angleCalculator = PieChartAngleCalculator()
    private val pieChartDrawer = PieChartDrawer(pieChartContainer, sectorPaint, selectionPaint)

    private var onCategorySelected : ((Int) -> Unit)? = null

    fun setPayloads(payloads: List<Payload>) {
        this.payloads = payloads
        angles.clear()
        angles.addAll(angleCalculator.calculateAngles(payloads))
        invalidate()
    }

    fun setSelectionListener(onCategorySelected: (Int) -> Unit) {
        this.onCategorySelected = onCategorySelected
    }

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
        pieChartDrawer.setPieChartContainerRectBounds(this, pieChartPaddingPx)
        pieChartDrawer.drawPieChart(canvas, angles)
        pieChartDrawer.drawSelectedSector(canvas, selectedSector)
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (GeometryHelper.isClickInsidePieChart(event, pieChartContainer)) {
            selectedSector = getSelectedAngle(event)
            onCategorySelected?.invoke(selectedSector?.id ?: -1)
            invalidate()
        }

        if (event.action == MotionEvent.ACTION_UP) {
            return super.performClick()
        }

        return true
    }

    private fun getSelectedAngle(event: MotionEvent): PieChartAngle? {
        for (angle in angles) {
            if (isClickWithinPieChartAngle(event, angle, pieChartContainer)) {
                return angle
            }
        }
        return null
    }

    private val superStateKey = "superState"
    private val selectedCategoryKey = "selectedCategoryKey"

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable(superStateKey, superState)

        selectedSector?.let {
            bundle.putInt(selectedCategoryKey, it.id)
        }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable(superStateKey))
        val selectedCategory = bundle.getInt(selectedCategoryKey, SELECTED_CATEGORY_NOT_FOUND)
        selectedSector = angles.find { it.id == selectedCategory }
    }

    companion object {
        private const val SELECTED_CATEGORY_NOT_FOUND = -1
    }
}