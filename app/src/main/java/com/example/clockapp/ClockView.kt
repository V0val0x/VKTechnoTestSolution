package com.example.clockapp

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import java.lang.ref.WeakReference
import java.util.*

private class ClockHandler(view: ClockView) : Handler() {
    private val weakReference: WeakReference<ClockView> = WeakReference(view)

    override fun handleMessage(msg: Message) {
        if (msg.what == MSG_UPDATE_CLOCK && weakReference.get() != null) {
            weakReference.get()?.updateClock()
            sendEmptyMessageDelayed(MSG_UPDATE_CLOCK, 1000L)
        }
    }

    companion object {
        const val MSG_UPDATE_CLOCK = 1
    }
}

class ClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val PAINT_WIDTH = 8f
    private val LONG_POINTER_LENGTH = 120f
    private val SHORT_POINTER_LENGTH = 80f
    private val CENTER_CIRCLE_RADIUS = 20f
    private val TICK_MARK_LENGTH = 30f
    private val TICK_MARK_WIDTH = 5f

    private var widthSize = 0
    private var heightSize = 0
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    private val calendar = Calendar.getInstance()
    private var hour = 0
    private var minute = 0
    private var second = 0

    fun setTime(hour: Int, minute: Int, second: Int) {
        this.hour = hour
        this.minute = minute
        this.second = second
        invalidate()
    }

    private val longPointerPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = PAINT_WIDTH
        strokeCap = Paint.Cap.ROUND
    }

    private val shortPointerPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = PAINT_WIDTH / 2
        strokeCap = Paint.Cap.ROUND
    }

    private val tickMarkPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = TICK_MARK_WIDTH
        strokeCap = Paint.Cap.ROUND
    }

    private val centerCirclePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val tickMarkPath = Path()

    private val handler = ClockHandler(this)

    init {
        initTickMarkPath()

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockView)
        longPointerPaint.color =
            typedArray.getColor(R.styleable.ClockView_longPointerColor, Color.BLACK)
        shortPointerPaint.color =
            typedArray.getColor(R.styleable.ClockView_shortPointerColor, Color.GRAY)
        tickMarkPaint.color =
            typedArray.getColor(R.styleable.ClockView_tickMarkColor, Color.BLACK)
        centerCirclePaint.color =
            typedArray.getColor(R.styleable.ClockView_centerCircleColor, Color.RED)
        typedArray.recycle()

        updateClock()
    }

    private fun initTickMarkPath() {
        tickMarkPath.moveTo(centerX, centerY - radius)
        tickMarkPath.lineTo(centerX, centerY - radius + TICK_MARK_LENGTH)
    }

    fun updateClock() {
        calendar.timeInMillis = System.currentTimeMillis()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        second = calendar.get(Calendar.SECOND)
        handler.sendEmptyMessageDelayed(0, 1000 - calendar.get(Calendar.MILLISECOND).toLong())
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        widthSize = MeasureSpec.getSize(widthMeasureSpec)
        heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSize > heightSize) {
            widthSize = heightSize
        } else {
            heightSize = widthSize
        }

        centerX = (widthSize / 2).toFloat()
        centerY = (heightSize / 2).toFloat()
        radius = (widthSize / 2 - PAINT_WIDTH / 2).toFloat()

        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw tick marks
        for (i in 0..11) {
            canvas?.save()
            canvas?.rotate(30f * i, centerX, centerY)
            canvas?.drawPath(tickMarkPath, tickMarkPaint)
            canvas?.restore()
        }

        // Draw hour pointer
        val hourRadians = Math.PI / 6 * (hour + minute / 60.0)
        canvas?.drawLine(
            centerX,
            centerY,
            (centerX + Math.sin(hourRadians) * SHORT_POINTER_LENGTH).toFloat(),
            (centerY - Math.cos(hourRadians) * SHORT_POINTER_LENGTH).toFloat(),
            shortPointerPaint
        )

        // Draw minute pointer
        val minuteRadians = Math.PI / 30 * (minute + second / 60.0)
        canvas?.drawLine(
            centerX,
            centerY,
            (centerX + Math.sin(minuteRadians) * LONG_POINTER_LENGTH).toFloat(),
            (centerY - Math.cos(minuteRadians) * LONG_POINTER_LENGTH).toFloat(),
            longPointerPaint
        )

        // Draw second pointer
        val secondRadians = Math.PI / 30 * second
        canvas?.drawLine(
            centerX,
            centerY,
            (centerX + Math.sin(secondRadians) * LONG_POINTER_LENGTH).toFloat(),
            (centerY - Math.cos(secondRadians) * LONG_POINTER_LENGTH).toFloat(),
            longPointerPaint
        )

        // Draw center circle
        canvas?.drawCircle(centerX, centerY, CENTER_CIRCLE_RADIUS, centerCirclePaint)
    }

    private class ClockHandler(view: ClockView) : Handler() {
        private val weakReference = WeakReference(view)

        override fun handleMessage(msg: Message) {
            val view = weakReference.get()
            view?.updateClock()
        }
    }
}

