package com.ordershieldsdk.auth.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.ordershieldsdk.auth.R

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSignatureChanged: (() -> Unit)? = null

    private val paint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val path = Path()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var hasDrawn = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            canvas = Canvas(bitmap!!)
            canvas!!.drawColor(context.getColor(R.color.white))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                lastTouchX = x
                lastTouchY = y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - lastTouchX)
                val dy = Math.abs(y - lastTouchY)
                if (dx >= 4 || dy >= 4) {
                    path.quadTo(lastTouchX, lastTouchY, (x + lastTouchX) / 2, (y + lastTouchY) / 2)
                    lastTouchX = x
                    lastTouchY = y
                    if (!hasDrawn) {
                        hasDrawn = true
                        onSignatureChanged?.invoke()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(lastTouchX, lastTouchY)
                canvas?.drawPath(path, paint)
                hasDrawn = true
                path.reset()
                onSignatureChanged?.invoke()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        bitmap?.let {
            canvas?.drawColor(context.getColor(R.color.white))
        }
        path.reset()
        hasDrawn = false
        invalidate()
        onSignatureChanged?.invoke()
    }

    fun hasSignature(): Boolean {
        return hasDrawn
    }

    fun getSignatureBitmap(): Bitmap? {
        return bitmap
    }
}

