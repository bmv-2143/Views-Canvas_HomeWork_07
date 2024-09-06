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
    internal val axisPaddingPx = 32.dp.px
    internal val axisTickHeightPx = 10.dp.px
    internal val tickCountX = 10
    internal val tickCountY = 5
    private val axisYMaxValue = 10f
    internal val gridDotSizePx = 2.dp.px

    private val _dayToExpenses = mutableMapOf<Int, Int>().withDefault { 0 }
    internal val dayToExpenses: Map<Int, Int> get() = _dayToExpenses
    private var maxCategoryTotalAmount = 0
    private val graphDrawer = GraphDrawer(this)

    fun setMaxDailyExpenseOfAllCategories(maxCategoryTotalAmount: Int) {
        this.maxCategoryTotalAmount = maxCategoryTotalAmount
    }

    fun setDaysToExpenses(dayToExpenses: Map<Int, Int>) {
        this._dayToExpenses.clear()
        this._dayToExpenses.putAll(dayToExpenses)
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
        graphDrawer.drawTicksOnXAxis(canvas)
        graphDrawer.drawTicksOnYAxis(canvas)
        graphDrawer.drawAxis(canvas)
        graphDrawer.drawDotsAtTickIntersections(canvas)

        graphDrawer.drawGraph(canvas)
        graphDrawer.drawDashedLinesThroughGraphPeaksPoints(canvas)
        graphDrawer.drawPurchaseDots(canvas)
        graphDrawer.drawTextAboveDashedLines(canvas)
    }

    internal fun mapAmountToYAxis(currentAmount: Float): Float {
        val maxAmount: Float = maxCategoryTotalAmount.toFloat()
        val scaleFactor = axisYMaxValue / maxAmount
        return currentAmount * scaleFactor
    }

    internal fun axisToScreenX(axisX: Float, maxXValue: Float = 31f): Float {
        val drawableWidth = width - 2 * axisPaddingPx
        val xScaleFactor = drawableWidth / maxXValue
        return axisPaddingPx + axisX * xScaleFactor
    }

    internal fun axisToScreenY(axisY: Float, maxYValue: Float = 10f): Float {
        val drawableHeight = height - 2 * axisPaddingPx
        val yScaleFactor = drawableHeight / maxYValue
        return height - axisPaddingPx - axisY * yScaleFactor
    }

    private val superStateKey = "superState"
    private val selectedCategoryKey = "dayToExpensesKey"
    private val maxCategoryTotalAmountKey = "maxCategoryTotalAmountKey"

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val bundle = Bundle()
        bundle.putParcelable(superStateKey, superState)
        bundle.putInt(maxCategoryTotalAmountKey, maxCategoryTotalAmount)
        bundle.putBundle(selectedCategoryKey, _dayToExpenses.toBundle())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable(superStateKey))

        // Restore dayToExpenses map
        val dayToExpensesBundle = bundle.getBundle(selectedCategoryKey)
        if (dayToExpensesBundle != null) {
            _dayToExpenses.putAll(dayToExpensesBundle.toMap())
        }
        maxCategoryTotalAmount = bundle.getInt(maxCategoryTotalAmountKey)
        invalidate()
    }
}
